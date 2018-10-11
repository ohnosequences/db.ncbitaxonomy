package ohnosequences.db.ncbitaxonomy

import com.amazonaws.services.s3.AmazonS3ClientBuilder
import ohnosequences.s3.request
import ohnosequences.files.{directory, file, gzip, read, remote, tar, utils}
import java.net.URL
import org.scalatest.Assertions.fail
import ohnosequences.files.{Error => FileError}
import ohnosequences.s3.{Error => S3Error}
import ohnosequences.forests.{IOError => SerializationError}
import ohnosequences.db.ncbitaxonomy.{TaxTree, io}

package object test {
  type File  = ohnosequences.files.File
  type Lines = ohnosequences.files.Lines

  private val partSize5MiB = 5 * 1024 * 1024

  private def failIfFileError[X]: FileError + X => X =
    _ match {
      case Right(result) => result
      case Left(error)   => fail(error.msg)
    }

  private def failIfRequestError[X]: S3Error + X => X =
    _ match {
      case Right(result) => result
      case Left(error)   => fail(error.msg)
    }

  private def failIfSerializatoinError[X]: SerializationError + X => X =
    _ match {
      case Right(result) => result
      case Left(error)   => fail(error.msg)
    }

  private val s3Client = AmazonS3ClientBuilder.defaultClient()

  private[test] def downloadFromS3(s3Obj: S3Object, file: File) =
    failIfRequestError {
      request.getCheckedFile(s3Client)(s3Obj, file)
    }

  private[test] def downloadFromURL(url: URL, file: File) =
    failIfFileError {
      remote.download(url, file)
    }

  private[test] def uploadTo(file: File, s3Obj: S3Object) =
    failIfRequestError {
      request.paranoidPutFile(s3Client)(file, s3Obj, partSize5MiB)(
        // Defined in main/src/package.scala
        hashingFunction
      )
    }

  private[test] def uncompressAndExtractTo(input: File, outputDir: File) = {
    val tarFile = new File(input.toString ++ ".tar")

    failIfFileError {
      tar.extract(
        failIfFileError { gzip.uncompress(input, tarFile) },
        outputDir
      )
    }
  }

  private[test] def objectExists(s3Obj: S3Object) =
    failIfRequestError {
      request.objectExists(s3Client)(s3Obj)
    }

  private[test] def createDirectory(path: File) =
    failIfFileError {
      directory.createDirectory(path)
    }

  private[test] def validFile(file: File) =
    utils.checkValidFile(file).isRight

  private[test] def readLinesWith[A](file: File)(f: Lines => A) =
    failIfFileError {
      read.withLines(file)(f)
    }

  private[test] def uploadIfNotExists(file: File, s3Obj: S3Object) =
    if (!objectExists(s3Obj)) {
      println(s"Uploading $file to $s3Obj")
      uploadTo(file, s3Obj)
    } else
      println(s"S3 object $s3Obj exists; skipping upload")

  private[test] def generateTree(nodesFile: File, namesFile: File) =
    failIfFileError {
      io.generateTaxTree(nodesFile, namesFile)
    }

  private[test] def dumpTreeTo(tree: TaxTree, dataFile: File, shapeFile: File) =
    failIfFileError {
      io.dumpTaxTreeToFiles(tree, dataFile, shapeFile)
    }

  private[test] def readTreeFrom(dataFile: File, shapeFile: File) =
    failIfSerializatoinError {
      failIfFileError {
        io.readTaxTreeFromFiles(dataFile, shapeFile)
      }
    }

  /**
    * Auxiliary method that downloads a file if it does not exists locally
    */
  private[test] def downloadFromS3IfNotExists(s3Object: S3Object,
                                              file: File): File =
    if (!validFile(file))
      downloadFromS3(s3Object, file)
    else
      file

  private[test] def recursiveDeleteDirectory(dir: File) =
    failIfFileError {
      directory.recursiveDeleteDirectory(dir)
    }

  private[test] def deleteFile(f: File) =
    file.deleteFile(f) match {
      case Left(err: FileError.FileNotFound) =>
      case Left(err)                         => fail(err.msg)
      case Right(result) =>
        if (!result)
          fail("File not erased")
    }

  private[test] def move(source: File, destination: File) =
    failIfFileError {
      file.move(source, destination)
    }

  def getNodesFile(version: Version): File =
    downloadFromS3IfNotExists(nodes(version), data.nodesLocalFile(version))

  def getNamesFile(version: Version): File =
    downloadFromS3IfNotExists(names(version), data.namesLocalFile(version))

  def getTreeDataFile(version: Version): File =
    downloadFromS3IfNotExists(treeData(version),
                              data.treeDataLocalFile(version))

  def getTreeShapeFile(version: Version): File =
    downloadFromS3IfNotExists(treeShape(version),
                              data.treeShapeLocalFile(version))
}

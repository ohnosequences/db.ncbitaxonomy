package ohnosequences.db.ncbitaxonomy

import com.amazonaws.services.s3.AmazonS3ClientBuilder
import ohnosequences.s3.request
import ohnosequences.files.{directory, gzip, read, remote, tar, utils}
import java.net.URL
import org.scalatest.Assertions.fail
import ohnosequences.files.{Error => FileError}
import ohnosequences.s3.{Error => S3Error}

package object test {
  type File  = ohnosequences.files.File
  type Lines = ohnosequences.files.Lines

  private val partSize5MiB = 5 * 1024 * 1024

  private def getFileOrFail[X]: FileError + X => X =
    _ match {
      case Right(result) => result
      case Left(error)   => fail(error.msg)
    }

  private def getRequestOrFail[X]: S3Error + X => X =
    _ match {
      case Right(result) => result
      case Left(error)   => fail(error.msg)
    }

  private val s3Client = AmazonS3ClientBuilder.defaultClient()

  private[test] def downloadFromS3(s3Obj: S3Object, file: File) =
    getRequestOrFail {
      request.getCheckedFile(s3Client)(s3Obj, file)
    }

  private[test] def downloadFromURL(url: URL, file: File) =
    getFileOrFail {
      remote.download(url, file)
    }

  private[test] def uploadTo(file: File, s3Obj: S3Object) =
    getRequestOrFail {
      request.paranoidPutFile(s3Client)(file, s3Obj, partSize5MiB)(
        // Defined in main/src/package.scala
        hashingFunction
      )
    }

  private[test] def uncompressAndExtractTo(input: File, outputDir: File) = {
    val tarFile = new File(input.toString ++ ".tar")

    getFileOrFail {
      tar.extract(
        getFileOrFail { gzip.uncompress(input, tarFile) },
        outputDir
      )
    }
  }

  private[test] def objectExists(s3Obj: S3Object) =
    getRequestOrFail {
      request.objectExists(s3Client)(s3Obj)
    }

  private[test] def createDirectory(path: File) =
    getFileOrFail {
      directory.createDirectory(path)
    }

  private[test] def validFile(file: File) =
    utils.checkValidFile(file).isRight

  private[test] def readLines(file: File) =
    getFileOrFail {
      read.withLines(file) { lines =>
        lines
      }
    }
}

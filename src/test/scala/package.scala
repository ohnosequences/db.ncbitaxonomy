package ohnosequences.db.ncbitaxonomy

import ohnosequences.s3.S3Object
import ohnosequences.files.read
import org.scalatest.Assertions.fail
import ohnosequences.db.ncbitaxonomy

package object test {
  type File  = ohnosequences.files.File
  type Lines = ohnosequences.files.Lines

  private def failIfError[X]: Error + X => X =
    _ match {
      case Right(result) => result
      case Left(error)   => fail(error.msg)
    }
  /*
   Henceforth there are a bunch of wrappers to get the result of a
   function or fail if some error arises
   */
  private[test] def downloadFromS3(s3Obj: S3Object, file: File) =
    failIfError {
      helpers.download(s3Obj, file)
    }

  private[test] def readLinesWith[A](file: File)(f: Lines => A) =
    failIfError {
      read.withLines(file)(f).left.map(Error.FileError)
    }

  private[test] def generateTree(nodesFile: File, namesFile: File) =
    failIfError {
      io.generateTaxTree(nodesFile, namesFile)
    }

  private[test] def dumpTreeTo(tree: TaxTree, dataFile: File, shapeFile: File) =
    failIfError {
      io.dumpTaxTreeToFiles(tree, dataFile, shapeFile)
    }

  private[test] def readTreeFrom(dataFile: File, shapeFile: File) =
    failIfError {
      io.readTaxTreeFromFiles(dataFile, shapeFile)
    }

  private[test] def getFileIfDifferent(s3Object: S3Object, file: File): File =
    failIfError {
      for {
        _ <- helpers.createDirectory(file.getParentFile)
        _ <- helpers.getFileIfDifferent(s3Object, file)
      } yield {
        file
      }
    }

  private[test] def deleteDirectory(dir: File) =
    failIfError {
      helpers.deleteDirectory(dir)
    }
}

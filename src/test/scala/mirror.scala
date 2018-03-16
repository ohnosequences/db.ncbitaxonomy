package ohnosequences.db.ncbitaxonomy.test

import ohnosequences.db.ncbitaxonomy.+
import ohnosequences.db.ncbitaxonomy.{names, nodes, sourceFile}
import ohnosequences.db.ncbitaxonomy.test.utils.{
  createDirectory,
  downloadFrom,
  uncompressAndExtractTo,
  uploadTo
}
import ohnosequences.test.ReleaseOnlyTest
import ohnosequences.awstools.s3.S3Object
import org.scalatest.FunSuite
import java.io.File

class Mirror extends FunSuite {
  test("Mirror data from NCBI FTP into ohnosequences S3", ReleaseOnlyTest) {
    val directory = new File("./ncbi-data/")

    val localFile = directory.toPath.resolve("taxdump.tar.gz").toFile
    val namesFile = directory.toPath.resolve("names.dmp").toFile
    val nodesFile = directory.toPath.resolve("nodes.dmp").toFile

    // Create the relative directory ./data
    createDirectoryOrFail(directory)

    // Retrieve original archived, compressed file from NCBI FTP
    downloadFromOrFail(sourceFile, localFile)

    // Uncompress and extract the archive file to get names.dmp and nodes.dmp
    uncompressAndExtractToOrFail(localFile, directory)

    // Upload nodes.dmp and names.dmp to their respective S3 locations
    uploadToOrFail(namesFile, names)
    uploadToOrFail(nodesFile, nodes)
  }

  def getOrFail[E <: Error, X]: E + X => X =
    _ match {
      case Right(x) => x
      case Left(e)  => fail(e.msg)
    }

  def downloadFromOrFail(uri: java.net.URI, file: File) =
    getOrFail {
      downloadFrom(uri, file)
    }

  def uncompressAndExtractToOrFail(input: File, outputDir: File) =
    getOrFail {
      uncompressAndExtractTo(input, outputDir)
    }

  def uploadToOrFail(file: File, s3Object: S3Object) =
    getOrFail {
      uploadTo(file, s3Object)
    }

  def createDirectoryOrFail(directory: File) =
    getOrFail {
      createDirectory(directory)
    }
}

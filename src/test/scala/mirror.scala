package ohnosequences.db.ncbitaxonomy.test

import ohnosequences.db.ncbitaxonomy.+
import ohnosequences.db.ncbitaxonomy.{Version, names, nodes, sourceFile}
import ohnosequences.db.ncbitaxonomy.test.utils.{
  createDirectory,
  downloadFrom,
  uncompressAndExtractTo,
  uploadTo
}
import ohnosequences.test.ReleaseOnlyTest
import ohnosequences.awstools.s3, s3.{S3Object, ScalaS3Client}
import org.scalatest.FunSuite
import java.io.File

class Mirror extends FunSuite {

  test("Mirror data from NCBI FTP into ohnosequences S3", ReleaseOnlyTest) {

    val version = Version.latest

    val directory = new File(s"./ncbi-data/${version.name}")

    val localFile = directory.toPath.resolve("taxdump.tar.gz").toFile
    val namesFile = directory.toPath.resolve("names.dmp").toFile
    val nodesFile = directory.toPath.resolve("nodes.dmp").toFile

    // Create the relative directory
    createDirectoryOrFail(directory)

    // Retrieve original archived, compressed file from NCBI FTP
    downloadFromOrFail(sourceFile, localFile)

    // Uncompress and extract the archive file to get names.dmp and nodes.dmp
    uncompressAndExtractToOrFail(localFile, directory)

    val s3Client = ScalaS3Client(s3.defaultClient)

    val namesObj = names(version)
    val nodesObj = nodes(version)

    // Upload nodes.dmp and names.dmp to their respective S3 locations,
    // only if those objects do not exist.
    // FIXME: Check that the local files and the files in S3 are exactly the
    // same, through a hash (provided by S3 or manually computed from here).
    if (!s3Client.objectExists(namesObj)) {
      println(s"Uploading $namesFile to $namesObj")
      uploadToOrFail(namesFile, namesObj)
    } else
      println(s"S3 object $namesObj exists; skipping upload")

    if (!s3Client.objectExists(nodesObj)) {
      println(s"Uploading $nodesFile to $nodesObj")
      uploadToOrFail(nodesFile, nodesObj)
    } else
      println(s"S3 object $nodesObj exists; skipping upload")
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

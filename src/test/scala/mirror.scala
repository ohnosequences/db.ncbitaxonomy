package ohnosequences.db.ncbitaxonomy.test

import ohnosequences.db.ncbitaxonomy._
import ohnosequences.test.ReleaseOnlyTest

class Mirror extends NCBITaxonomyTest("Mirror") {

  test("Mirror data from NCBI FTP into ohnosequences S3", ReleaseOnlyTest) {

    val version = Version.latest

    val directory = new File(s"./ncbi-data/${version.name}")

    val localFile = directory.toPath.resolve("taxdump.tar.gz").toFile
    val namesFile = directory.toPath.resolve("names.dmp").toFile
    val nodesFile = directory.toPath.resolve("nodes.dmp").toFile

    // Create the relative directory
    createDirectory(directory)

    // Retrieve original archived, compressed file from NCBI FTP
    downloadFromURL(sourceFile, localFile)

    // Uncompress and extract the archive file to get names.dmp and nodes.dmp
    uncompressAndExtractTo(localFile, directory)

    val namesObj = names(version)
    val nodesObj = nodes(version)

    // Upload nodes.dmp and names.dmp to their respective S3 locations,
    // only if those objects do not exist.
    if (!objectExists(namesObj)) {
      println(s"Uploading $namesFile to $namesObj")
      uploadTo(namesFile, namesObj)
    } else
      println(s"S3 object $namesObj exists; skipping upload")

    if (!objectExists(nodesObj)) {
      println(s"Uploading $nodesFile to $nodesObj")
      uploadTo(nodesFile, nodesObj)
    } else
      println(s"S3 object $nodesObj exists; skipping upload")
  }

}

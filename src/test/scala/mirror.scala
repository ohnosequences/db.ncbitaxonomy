package ohnosequences.db.ncbitaxonomy.test

import ohnosequences.db.ncbitaxonomy._
import ohnosequences.test.ReleaseOnlyTest
import ohnosequences.files.utils.checkValidFile

class Mirror extends NCBITaxonomyTest("Mirror") {

  test("Mirror data from NCBI FTP into ohnosequences S3", ReleaseOnlyTest) {

    val version = Version.latest

    val tempDir = new File(s"./ncbi-data/${version.name}")
    val dataDir = data.dataDirectory(version)

    val tempTarFile   = tempDir.toPath.resolve("taxdump.tar.gz").toFile
    val nodesTempFile = tempDir.toPath.resolve("nodes.dmp").toFile
    val namesTempFile = tempDir.toPath.resolve("names.dmp").toFile

    // Create a temp directory to uncompress and untar files from NCBI
    createDirectory(tempDir)

    // Retrieve original archived, compressed file from NCBI FTP
    downloadFromURL(sourceFile, tempTarFile)

    // Uncompress and extract the archive file to get names.dmp and nodes.dmp
    uncompressAndExtractTo(tempTarFile, tempDir)

    val nodesFile = move(nodesTempFile, dataDir)
    val namesFile = move(namesTempFile, dataDir)

    // Delete temp dir
    recursiveDeleteDirectory(tempDir)

    // Get names of objects in S3
    val namesObj = names(version)
    val nodesObj = nodes(version)

    // Upload nodes.dmp and names.dmp to their respective S3 locations,
    // only if those objects do not exist.
    uploadIfNotExists(namesFile, namesObj)
    uploadIfNotExists(nodesFile, nodesObj)
  }

  test("Mirror trees for all versions, if they do not exist", ReleaseOnlyTest) {
    Version.all foreach { version =>
      val dataObj  = treeData(version)
      val shapeObj = treeShape(version)

      if (!objectExists(dataObj) || !objectExists(shapeObj)) {
        val dataFile  = getTreeDataFile(version)
        val shapeFile = getTreeShapeFile(version)

        if (!dataFile.exists || !shapeFile.exists) {
          deleteFile(dataFile)
          deleteFile(shapeFile)

          val nodesFile = getNodesFile(version)
          val namesFile = getNamesFile(version)

          // Generate taxonomy tree and upload data.tree and shape.tree to S3, if they
          // do not exist
          val tree = generateTree(nodesFile, namesFile)
          dumpTreeTo(tree, dataFile, shapeFile)
        }

        uploadIfNotExists(dataFile, dataObj)
        uploadIfNotExists(shapeFile, shapeObj)
      }
    }
  }
}

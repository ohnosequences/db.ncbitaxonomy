package ohnosequences.db.ncbitaxonomy.test

import ohnosequences.db.ncbitaxonomy._
import ohnosequences.test.ReleaseOnlyTest
import ohnosequences.files.utils.checkValidFile
import org.scalatest.DoNotDiscover

@DoNotDiscover
class Mirror extends NCBITaxonomyTest("Mirror") {

  test("Mirror data from NCBI FTP into ohnosequences S3", ReleaseOnlyTest) {

    val version = Version.latest

    // Get names of objects in S3
    val namesObj = names(version)
    val nodesObj = nodes(version)

    // If S3 objects for {names, nodes}.dmp do not exist,
    // download NCBI files and mirror them
    if (!objectExists(namesObj) || !objectExists(nodesObj)) {

      val nodesFile = data.nodesLocalFile(version)
      val namesFile = data.namesLocalFile(version)

      if (!nodesFile.exists || !namesFile.exists) {
        // If any of them does not exists, regenerate them
        deleteFile(nodesFile)
        deleteFile(namesFile)

        val tempDir = new File(s"./ncbi-data/${version.name}")
        val dataDir = data.dataDirectory(version)

        val tempTarFile   = tempDir.toPath.resolve("taxdump.tar.gz").toFile
        val nodesTempFile = tempDir.toPath.resolve("nodes.dmp").toFile
        val namesTempFile = tempDir.toPath.resolve("names.dmp").toFile

        // Clean and create a temp directory to uncompress and untar files from NCBI
        if (tempDir.exists)
          recursiveDeleteDirectory(tempDir)

        createDirectory(tempDir)

        // Retrieve original archived, compressed file from NCBI FTP
        downloadFromURL(sourceFile, tempTarFile)

        // Uncompress and extract the archive file to get names.dmp and nodes.dmp
        uncompressAndExtractTo(tempTarFile, tempDir)

        // Create directory for data if it does not exists
        if (!dataDir.exists)
          createDirectory(dataDir)

        move(nodesTempFile, dataDir)
        move(namesTempFile, dataDir)

        // Delete temp dir
        recursiveDeleteDirectory(tempDir)

        // Upload nodes.dmp and names.dmp to their respective S3 locations,
        // only if those objects do not exist.
        uploadIfNotExists(namesFile, namesObj)
        uploadIfNotExists(nodesFile, nodesObj)
      }
    }
  }

  test("Mirror trees for all versions, if they do not exist", ReleaseOnlyTest) {
    Version.all foreach { version =>
      val dataObj  = treeData(version)
      val shapeObj = treeShape(version)

      // If either data or shape for the taxonomic tree do not exist
      // in S3, regenerate them and mirror them
      if (!objectExists(dataObj) || !objectExists(shapeObj)) {
        // Search for the data and shape files locally
        val dataFile  = data.treeDataLocalFile(version)
        val shapeFile = data.treeShapeLocalFile(version)

        if (!dataFile.exists || !shapeFile.exists) {
          // If any of them does not exists, regenerate them
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

package ohnosequences.db.ncbitaxonomy.test

import ohnosequences.db.ncbitaxonomy._
import ohnosequences.test.ReleaseOnlyTest

class Mirror extends NCBITaxonomyTest("Mirror") {

  test("Mirror data from NCBI FTP into ohnosequences S3", ReleaseOnlyTest) {

    val version = Version.latest

    val directory = new File(s"./ncbi-data/${version.name}")

    val localFile     = directory.toPath.resolve("taxdump.tar.gz").toFile
    val nodesFile     = directory.toPath.resolve("nodes.dmp").toFile
    val namesFile     = directory.toPath.resolve("names.dmp").toFile
    val treeDataFile  = directory.toPath.resolve("data.tree").toFile
    val treeShapeFile = directory.toPath.resolve("shape.tree").toFile

    // Create the relative directory
    createDirectory(directory)

    // Retrieve original archived, compressed file from NCBI FTP
    downloadFromURL(sourceFile, localFile)

    // Uncompress and extract the archive file to get names.dmp and nodes.dmp
    uncompressAndExtractTo(localFile, directory)

    val namesObj = names(version)
    val nodesObj = nodes(version)
    val dataObj  = treeData(version)
    val shapeObj = treeShape(version)
    
    // Upload nodes.dmp and names.dmp to their respective S3 locations,
    // only if those objects do not exist.
    uploadIfNotExists(namesFile, namesObj)
    uploadIfNotExists(nodesFile, nodesObj)

    // Generate taxonomy tree and upload data.tree and shape.tree to S3, if they
    // do not exist
    val tree = generateTree(nodesFile, namesFile)
    dumpTaxTreeTo(tree, treeDataFile, treeShapeFile)

    uploadIfNotExists(treeDataFile, dataObj)
    uploadIfNotExists(treeShapeFile, shapeObj)
  }

}

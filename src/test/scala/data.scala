package ohnosequences.db.ncbitaxonomy.test

import ohnosequences.db.ncbitaxonomy.Version

object data {

  def dataDirectory(version: Version): File =
    new File(s"./data/in/${version}")

  def namesLocalFile(version: Version): File =
    dataDirectory(version).toPath.resolve("names.dmp").toFile

  def nodesLocalFile(version: Version): File =
    dataDirectory(version).toPath.resolve("nodes.dmp").toFile

  def treeDataLocalFile(version: Version): File =
    dataDirectory(version).toPath.resolve("data.tree").toFile

  def treeShapeLocalFile(version: Version): File =
    dataDirectory(version).toPath.resolve("shape.tree").toFile
}

package ohnosequences.db.ncbitaxonomy.test

import ohnosequences.db.ncbitaxonomy.Version

object data {

  def namesLocalFile(version: Version): File =
    new File(s"./data/in/${version}/names.dmp")

  def nodesLocalFile(version: Version): File =
    new File(s"./data/in/${version}/nodes.dmp")

  def treeDataLocalFile(version: Version): File =
    new File(s"./data/in/${version}/data.tree")

  def treeShapeLocalFile(version: Version): File =
    new File(s"./data/in/${version}/shape.tree")
}

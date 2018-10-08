package ohnosequences.db.ncbitaxonomy.test

import ohnosequences.db.ncbitaxonomy.Version

object data {

  def namesLocalFile(version: Version): File =
    new File(s"./data/in/${version}/names.dmp")

  def nodesLocalFile(version: Version): File =
    new File(s"./data/in/${version}/nodes.dmp")
}

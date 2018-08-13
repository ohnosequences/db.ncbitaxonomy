package ohnosequences.db.ncbitaxonomy.test

import ohnosequences.db
import java.io.File

object data {

  def namesLocalFile(dbVersion: db.ncbitaxonomy.Version) =
    new File(s"./data/in/${dbVersion}/names.dmp")

  def nodesLocalFile(dbVersion: db.ncbitaxonomy.Version) =
    new File(s"./data/in/${dbVersion}/nodes.dmp")
}

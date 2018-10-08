package ohnosequences.db.ncbitaxonomy.test

import ohnosequences.db
import java.io.File

object data {

  val namesLocalFile(dbVersion: db.ncbitaxonomy.Version) =
    new File(s"./data/in/${dbVersion}/names.dmp")

  val nodesLocalFile(dbVersion: db.ncbitaxonomy.Version) =
    new File(s"./data/in/${dbVersion}/nodes.dmp")
}

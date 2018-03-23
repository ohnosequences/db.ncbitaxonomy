package ohnosequences.api.ncbitaxonomy.test

import ohnosequences.db
import java.io.File

object data {

  lazy val namesLocalFile =
    new File(s"./data/in/${db.ncbitaxonomy.version}/names.dmp")

  lazy val nodesLocalFile =
    new File(s"./data/in/${db.ncbitaxonomy.version}/nodes.dmp")
}

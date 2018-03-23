package ohnosequences.api.ncbitaxonomy.test

import org.scalatest.FunSuite
import ohnosequences.api.ncbitaxonomy._
import ohnosequences.db
import ohnosequences.api.ncbitaxonomy.test.utils._

class ParseAllNames extends FunSuite {

  def getNamesLines = {
    if (!data.namesLocalFile.exists)
      downloadFrom(db.ncbitaxonomy.names, data.namesLocalFile).left
        .map { e =>
          fail(e.msg)
        }

    retrieveLinesFrom(data.namesLocalFile) match {
      case Right(x) => x
      case Left(e)  => fail(e.msg)
    }
  }

  test("parse all names and access all data") {

    dmp.names.fromLines(getNamesLines) foreach { n =>
      val id   = n.nodeID
      val name = n.name

      // We just want to check whether we can access the values but sbt
      // complaints about the values above being unused, so trick sbt into
      // thinkink we are using them.
      // TODO: Code a proper test instead of this silly trick.
      id + name
    }
  }
}

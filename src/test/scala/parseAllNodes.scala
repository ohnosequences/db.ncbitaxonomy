package ohnosequences.api.ncbitaxonomy.test

import org.scalatest.FunSuite
import ohnosequences.api.ncbitaxonomy._
import ohnosequences.db
import ohnosequences.api.ncbitaxonomy.test.utils._
import ohnosequences.test.ReleaseOnlyTest

class ParseAllNodes extends FunSuite {

  def getNodesLines = {
    if (!data.nodesLocalFile.exists)
      downloadFrom(db.ncbitaxonomy.nodes, data.nodesLocalFile).left
        .map { e =>
          fail(e.msg)
        }

    retrieveLinesFrom(data.nodesLocalFile) match {
      case Right(x) => x
      case Left(e)  => fail(e.msg)
    }
  }

  test("parse all nodes and access all data", ReleaseOnlyTest) {

    dmp.nodes.fromLines(getNodesLines) foreach { node =>
      val id     = node.ID
      val parent = node.parentID
      val rank   = node.rank

      // We just want to check whether we can access the values but sbt
      // complaints about the values above being unused, so trick sbt into
      // thinkink we are using them.
      // TODO: Code a proper test instead of this silly trick.
      id + parent + rank
    }
  }
}

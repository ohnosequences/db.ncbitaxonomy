package ohnosequences.db.ncbitaxonomy.test

import org.scalatest.FunSuite
import ohnosequences.db.ncbitaxonomy._
import ohnosequences.db
import ohnosequences.db.ncbitaxonomy.test.utils._
import ohnosequences.test.ReleaseOnlyTest
import ohnosequences.awstools.s3.S3Object
import java.io.File

class ParseFullTaxonomy extends FunSuite {

  /**
    * Auxiliary method that returns an Iterator[String] from `file`. If `file`
    * does not exist, it is downloaded from `s3Object` before parsing its lines.
    */
  def getLines(s3Object: S3Object, file: File): Iterator[String] = {
    if (!file.exists)
      downloadFromS3(s3Object, file).left
        .map { e =>
          fail(e.msg)
        }

    retrieveLinesFrom(file) match {
      case Right(x) => x
      case Left(e)  => fail(e.msg)
    }
  }

  def getNamesLines(version: Version) =
    getLines(db.ncbitaxonomy.names(version), data.namesLocalFile(version))
  def getNodesLines(version: Version) =
    getLines(db.ncbitaxonomy.nodes(version), data.nodesLocalFile(version))

  test("Parse all names and access all data", ReleaseOnlyTest) {

    Version.all foreach { version =>
      dmp.names.fromLines(getNamesLines(version)) foreach { n =>
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

  test("Parse all nodes and access all data", ReleaseOnlyTest) {

    Version.all foreach { version =>
      dmp.nodes.fromLines(getNodesLines(version)) foreach { node =>
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
}

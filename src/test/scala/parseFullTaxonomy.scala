package ohnosequences.db.ncbitaxonomy.test

import scala.collection.mutable.HashSet
import scala.collection.mutable.HashMap
import ohnosequences.db.ncbitaxonomy._
import ohnosequences.db
import ohnosequences.test.ReleaseOnlyTest
import ohnosequences.files
import java.io.File

class ParseFullTaxonomy extends NCBITaxonomyTest("ParseFullTaxonomy") {

  /**
    * Auxiliary method that returns an Iterator[String] from `file`. If `file`
    * does not exist, it is downloaded from `s3Object` before parsing its lines.
    */
  def getLines(s3Object: S3ObjectId, file: File): Lines = {
    if (!files.utils.checkValidValid(file))
      downloadFromS3(s3Object, file).left.map { error =>
        fail(error.msg)
      }

    // Apply the identity function to the reader to retrieve the lines unmodified
    files.read.withLines(file) { lines => lines } match {
      case Right(lines) => lines
      case Left(error)  => fail(error.msg)
    }
  }

  def getNamesLines(version: Version): Lines =
    getLines(db.ncbitaxonomy.names(version), data.namesLocalFile(version))

  def getNodesLines(version: Version): Lines =
    getLines(db.ncbitaxonomy.nodes(version), data.nodesLocalFile(version))

  test("Parse all names and access all data", ReleaseOnlyTest) {

    Version.all foreach { version =>
      val seen = new HashSet[TaxID]

      parse.names.fromLines(getNamesLines(version)) foreach { sciName =>
        val id   = sciName.nodeID
        val name = sciName.name

        assert { ! seen.contains(id) }
        assert { id > 0 }
        assert { ! name.isEmpty }

        seen += id
      }
    }

  }

  test("Parse all nodes and access all data", ReleaseOnlyTest) {

    Version.all foreach { version =>
      val nonOrphan = new HashMap[TaxID, TaxID]

      dmp.nodes.fromLines(getNodesLines(version)) foreach { node =>
        val id     = node.id
        val parent = node.parentID
        val rank   = node.rank

        assert { id > 0 }
        assert { parent > 0 }

        // Each node should have only a parent
        assert { ! nonOrphan.contains(id) }
        nonOrphan += (id -> parent)

        // Rank should exist for all nodes
        assert { rank != Rank.RankError }
      }
    }
  }
}

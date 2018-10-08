package ohnosequences.db.ncbitaxonomy.test

import scala.collection.mutable.HashSet
import scala.collection.mutable.HashMap
import ohnosequences.db.ncbitaxonomy._
import ohnosequences.db
import ohnosequences.test.ReleaseOnlyTest

class ParseFullTaxonomy extends NCBITaxonomyTest("ParseFullTaxonomy") {

  /**
    * Auxiliary method that returns an Iterator[String] from `file`. If `file`
    * does not exist, it is downloaded from `s3Object` before parsing its lines.
    */
  def getLines(s3Object: S3Object, file: File): Lines = {
    if (!validFile(file))
      downloadFromS3(s3Object, file)

    readLines(file)
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

        assert { !seen.contains(id) }
        assert { id > 0 }
        assert { !name.isEmpty }

        seen += id
      }
    }

  }

  test("Parse all nodes and access all data", ReleaseOnlyTest) {

    Version.all foreach { version =>
      val nonOrphan = new HashMap[TaxID, TaxID]

      parse.nodes.fromLines(getNodesLines(version)) foreach { node =>
        val id     = node.id
        val parent = node.parentID
        val rank   = node.rank

        assert { id > 0 }
        assert { parent > 0 }

        // Each node should have only a parent
        assert { !nonOrphan.contains(id) }
        nonOrphan += (id -> parent)

        // Rank should exist for all nodes
        assert { rank != Rank.RankError }
      }
    }
  }
}

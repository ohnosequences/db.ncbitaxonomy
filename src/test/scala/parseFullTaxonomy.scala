package ohnosequences.db.ncbitaxonomy.test

import scala.collection.mutable.HashSet
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

        // Ensure only one name per id
        assert { seen.add(id) }
        assert { id > 0 }
        // Ensure name is not empty
        assert { !name.isEmpty }
      }
    }
  }

  test("All nodes can be parsed for all versions", ReleaseOnlyTest) {

    Version.all foreach { version =>
      val nodes = parse.nodes.fromLines(getNodesLines(version))

      nodes foreach { maybeNode =>
        assert { !maybeNode.isEmpty }
      }
    }
  }

  test("Check that there is a name for each node", ReleaseOnlyTest) {

    Version.all.foreach { version =>
      val nodes = parse.nodes.fromLines(getNodesLines(version))
      val names = parse.names.fromLines(getNamesLines(version))

      val withName = new HashSet[TaxID]

      names.foreach {
        case ScientificName(id, name) =>
          withName += id
      }

      nodes.foreach {
        case Some(Node(id, _, _)) =>
          assert { withName.contains(id) }
        case None => // already checked that this case should not arise
      }
    }
  }

  test("Ids are all positive for nodes", ReleaseOnlyTest) {

    Version.all foreach { version =>
      parse.nodes.fromLines(getNodesLines(version)) foreach {
        case Some(Node(id, parent, rank)) =>
          assert { id > 0 }
          assert { parent > 0 }
        case None => // already checked that this case should not arise
      }
    }
  }

  // Ensures we can turn data into a tree, although we should check that
  // number of nodes in the tree matches number of read nodes
  test("There is no node with more than a parent", ReleaseOnlyTest) {

    Version.all foreach { version =>
      val nonOrphan = new HashSet[TaxID]

      parse.nodes.fromLines(getNodesLines(version)) foreach {
        case Some(Node(id, _, _)) =>
          assert { nonOrphan.add(id) }
        case None => // already checked that this case should not arise
      }
    }
  }
}

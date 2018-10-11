package ohnosequences.db.ncbitaxonomy.test

import scala.collection.mutable.HashSet
import ohnosequences.db.ncbitaxonomy._
import ohnosequences.db
import ohnosequences.test.ReleaseOnlyTest
import org.scalatest.DoNotDiscover

@DoNotDiscover
class CheckFullTaxonomy extends NCBITaxonomyTest("ParseFullTaxonomy") {

  test("Parse all names and access all data", ReleaseOnlyTest) {

    Version.all foreach { version =>
      val seen = new HashSet[TaxID]

      readLinesWith(getNamesFile(version)) { lines: Lines =>
        val names = parse.names.fromLines(lines)

        names foreach { sciName =>
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
  }

  test("All nodes can be parsed for all versions", ReleaseOnlyTest) {

    Version.all foreach { version =>
      readLinesWith(getNodesFile(version)) { lines: Lines =>
        val nodes = parse.nodes.fromLines(lines)

        nodes foreach { maybeNode =>
          assert { !maybeNode.isEmpty }
        }
      }
    }
  }

  test("Check that there is a name for each node", ReleaseOnlyTest) {

    Version.all.foreach { version =>
      readLinesWith(getNodesFile(version)) { nodeLines: Lines =>
        val nodes = parse.nodes.fromLines(nodeLines)

        readLinesWith(getNamesFile(version)) { nameLines: Lines =>
          val names = parse.names.fromLines(nameLines)

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
    }
  }

  test("Ids are all positive for nodes", ReleaseOnlyTest) {

    Version.all foreach { version =>
      readLinesWith(getNodesFile(version)) { nodeLines =>
        val nodes = parse.nodes.fromLines(nodeLines)

        nodes.foreach {
          case Some(Node(id, parent, rank)) =>
            assert { id > 0 }
            assert { parent > 0 }
          case None => // already checked that this case should not arise
        }
      }
    }
  }

  // Ensures we can turn data into a tree, although we should check that
  // number of nodes in the tree matches number of read nodes
  test("There is no node with more than a parent", ReleaseOnlyTest) {

    Version.all foreach { version =>
      val nonOrphan = new HashSet[TaxID]

      readLinesWith(getNodesFile(version)) { nodeLines =>
        val nodes = parse.nodes.fromLines(nodeLines)

        nodes.foreach {
          case Some(Node(id, _, _)) =>
            assert { nonOrphan.add(id) }
          case None => // already checked that this case should not arise
        }
      }
    }
  }

  test(
    "Data and shape files can be parsed into a tree with proper number of nodes",
    ReleaseOnlyTest) {

    Version.all foreach { version =>
      val treeData  = getTreeDataFile(version)
      val shapeData = getTreeShapeFile(version)
      val numNodes  = readLinesWith(getNodesFile(version)) { _.length }

      val tree = readTreeFrom(treeData, shapeData)

      assert { tree.numNodes == numNodes }
    }
  }
}

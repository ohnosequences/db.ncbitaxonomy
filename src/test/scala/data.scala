package ohnosequences.db.ncbitaxonomy.test

import ohnosequences.db.ncbitaxonomy, ncbitaxonomy._, io.defaultFormat
import ohnosequences.forests.{NonEmptyTree, random}
import scala.collection.mutable.StringBuilder
import java.io.File.createTempFile

object data {

  def getNodesFile(version: Version): File =
    getFileIfDifferent(
      ncbitaxonomy.data.nodes(version),
      ncbitaxonomy.data.local.nodes(version)
    )

  def getNamesFile(version: Version): File =
    getFileIfDifferent(
      ncbitaxonomy.data.names(version),
      ncbitaxonomy.data.local.names(version)
    )

  def getTreeData(version: Version): File =
    getFileIfDifferent(
      ncbitaxonomy.data.treeData(version),
      ncbitaxonomy.data.local.treeData(version)
    )

  def getTreeShape(version: Version): File =
    getFileIfDifferent(
      ncbitaxonomy.data.treeShape(version),
      ncbitaxonomy.data.local.treeShape(version)
    )

  val numInstances: Int = 100

  val maxDepth: Int = 10

  val maxSiblingsPerFamily: Int = 5

  val allRanks: Array[Rank] = Rank.all.toArray

  // Random tax trees
  val taxTrees: Array[TaxTree] = {
    def nextString: String = {
      val length      = random.intIn(0, 100)
      val siblingsSep = defaultFormat.siblingsSep
      val familySep   = defaultFormat.familySep

      @annotation.tailrec
      def fill(result: StringBuilder, i: Int): String =
        if (i < length) {
          // Readable chars
          val randomChar = random.intIn(32, 127).toChar
          fill(result.append(randomChar), i + 1)
        } else {
          // Assume ;;; and /// cannot appear in a node description
          result.toString
            .replaceAll(siblingsSep, "")
            .replaceAll(familySep, "")
        }

      fill(new StringBuilder, 0)
    }

    val numRanks = allRanks.length

    def nextRank: Rank = {
      val toPick = random.intIn(0, numRanks)
      allRanks(toPick)
    }

    def nextTaxNode: TaxNode =
      TaxNode(random.intIn(0, 10), nextRank, nextString)

    Array.tabulate(numInstances) { i =>
      val forestDepth = random.intIn(0, maxDepth + 1)
      val children =
        random.genForest(_ => nextTaxNode, forestDepth, maxSiblingsPerFamily)
      val root = nextTaxNode

      new NonEmptyTree(root, children)
    }
  }

  val dataTempFile: Array[File] = Array.tabulate(numInstances) { i =>
    val file = createTempFile(s"treeData-${i}", ".tmp")
    file.deleteOnExit()
    file
  }

  val shapeTempFile: Array[File] = Array.tabulate(numInstances) { i =>
    val file = createTempFile(s"treeShape-${i}", ".tmp")
    file.deleteOnExit()
    file
  }

}

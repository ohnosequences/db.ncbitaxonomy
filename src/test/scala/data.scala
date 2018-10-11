package ohnosequences.db.ncbitaxonomy.test

import ohnosequences.db.ncbitaxonomy._, io.defaultFormat
import ohnosequences.forests.{NonEmptyTree, random}
import scala.collection.mutable.StringBuilder
import java.io.File.createTempFile

object data {
  import Rank._

  val numInstances: Int         = 100
  val maxDepth: Int             = 10
  val maxSiblingsPerFamily: Int = 5
  val allRanks: Array[Rank] = Array(
    Superkingdom,
    Kingdom,
    Subkingdom,
    Superphylum,
    Phylum,
    Subphylum,
    Superclass,
    Class,
    Subclass,
    Infraclass,
    Cohort,
    Superorder,
    Order,
    Suborder,
    Infraorder,
    Parvorder,
    Superfamily,
    Family,
    Subfamily,
    Tribe,
    Subtribe,
    Genus,
    Subgenus,
    SpeciesGroup,
    SpeciesSubgroup,
    Species,
    Subspecies,
    Varietas,
    Forma,
    NoRank
  )

  def dataDirectory(version: Version): File =
    new File(s"./data/in/${version.name}")

  def namesLocalFile(version: Version): File =
    dataDirectory(version).toPath.resolve("names.dmp").toFile

  def nodesLocalFile(version: Version): File =
    dataDirectory(version).toPath.resolve("nodes.dmp").toFile

  def treeDataLocalFile(version: Version): File =
    dataDirectory(version).toPath.resolve("data.tree").toFile

  def treeShapeLocalFile(version: Version): File =
    dataDirectory(version).toPath.resolve("shape.tree").toFile

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
    createTempFile(s"treeData-${i}", ".tmp")
  }

  val shapeTempFile: Array[File] = Array.tabulate(numInstances) { i =>
    createTempFile(s"treeShape-${i}", ".tmp")
  }

}

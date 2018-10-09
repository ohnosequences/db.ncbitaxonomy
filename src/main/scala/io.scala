package ohnosequences.db.ncbitaxonomy

import ohnosequences.forests.Tree
import scala.collection.mutable.{ArrayBuffer, HashMap}
import ohnosequences.files.{read, Lines, File, Error => FileError}

case object io {

  private final case class RankMap(
      root: Option[IdWithRank],
      children: HashMap[TaxID, Array[IdWithRank]]
  )

  private type NamesMap = HashMap[TaxID, String]

  final case class TreeMap(
      root: Option[TaxNode],
      children: HashMap[TaxID, Array[TaxNode]]
  )

  private final case class IdWithRank(id: TaxID, rank: Rank)

  // Return a RankMap
  private def generateRanksMap(nodesLines: Lines): RankMap = {
    val children = new HashMap[TaxID, ArrayBuffer[IdWithRank]]

    val nodes = parse.nodes.fromLines(nodesLines)

    val root = nodes.foldLeft(Option.empty[IdWithRank]) {
      (maybeRoot, maybeNode) =>
        maybeNode match {
          case Some(Node(id, parent, rank)) =>
            val result = IdWithRank(id, rank)

            if (children.isDefinedAt(parent)) {
              children(parent) += result
              maybeRoot
            } else if (id == parent) {
              Some(result)
            } else {
              children += (parent -> ArrayBuffer(result))
              maybeRoot
            }
          case None =>
            maybeRoot
        }
    }

    val childrenMap = children.map {
      case (parent, descendants) =>
        (parent, descendants.toArray)
    }

    new RankMap(root, childrenMap)
  }

  private def generateNamesMap(namesLines: Lines): NamesMap = {
    val result = new NamesMap

    val names = parse.names.fromLines(namesLines)

    names.foreach {
      case ScientificName(id, name) =>
        if (!result.isDefinedAt(id))
          result += (id -> name)
    }

    result
  }

  def generateTreeMap(nodesFile: File, namesFile: File): FileError + TreeMap = {
    val ranksResult = read.withLines(nodesFile) { lines =>
      generateRanksMap(lines)
    }

    ranksResult.flatMap { ranks =>
      val namesResult = read.withLines(namesFile) { lines =>
        generateNamesMap(lines)
      }

      namesResult.map { names =>
        val root = ranks.root.flatMap {
          case IdWithRank(id, rank) =>
            names.get(id).map { name =>
              TaxNode(id, rank, name)
            }
        }

        val children =
          if (!root.isEmpty) {
            ranks.children.map {
              case (id, descendants) =>
                val newDescendants = descendants.map {
                  case IdWithRank(id, rank) =>
                    names.get(id).map { name =>
                      TaxNode(id, rank, name)
                    }
                }.flatten

                (id, newDescendants)
            }
          } else {
            HashMap.empty[TaxID, Array[TaxNode]]
          }

        new TreeMap(root, children)
      }
    }
  }

  def treeMapToTaxTree(tree: TreeMap): TaxTree = {
    val root        = tree.root
    val children    = tree.children
    val init: TaxID = -1

    val values = { parent: TaxID =>
      if (parent == init) {
        root match {
          case None =>
            Array.empty[TaxNode]
          case Some(value) =>
            Array(value)
        }
      } else
        children.get(parent).getOrElse(Array.empty[TaxNode])
    }

    val next = { current: TaxID =>
      if (current == init) {
        root match {
          case None =>
            Array.empty[TaxID]
          case Some(taxNode) =>
            Array(taxNode.id)
        }
      } else
        children
          .get(current)
          .fold(Array.empty[TaxID]) { nodes =>
            nodes.map { node =>
              node.id
            }
          }
    }

    Tree.unfold(values, next)(init)
  }

  def generateTaxTree(nodesFile: File, namesFile: File): FileError + TaxTree =
    generateTreeMap(nodesFile: File, namesFile: File).map { tree =>
      treeMapToTaxTree(tree)
    }

}

package ohnosequences.db.ncbitaxonomy

import ohnosequences.forests.Tree
import scala.collection.mutable.{ArrayBuffer, Map => MutableMap}
import ohnosequences.files.Lines

case object io {

  final case class TreeMap(
      root: Option[TaxNode],
      children: MutableMap[TaxID, Array[TaxNode]]
  )

  // Return a TreeMap
  def generateNodesMap(lines: Lines): TreeMap = {
    val children = MutableMap[TaxID, ArrayBuffer[TaxNode]]()
    val nodes = lines.map { line =>
      parse.node.fromLine(line)
    }

    val root = nodes.foldLeft(Option.empty[TaxNode]) { (maybeRoot, node) =>
      val parent  = node.parentID
      val id      = node.id
      val rank    = node.rank
      val taxNode = TaxNode(id, rank)

      if (children.isDefinedAt(parent)) {
        children(parent) += taxNode
        maybeRoot
      } else if (id == parent) {
        Some(taxNode)
      } else {
        children += (parent -> ArrayBuffer(taxNode))
        maybeRoot
      }
    }

    val childrenMap = children.map {
      case (parent, descendants) =>
        (parent, descendants.toArray)
    }

    new TreeMap(root, childrenMap)
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

}

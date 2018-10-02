package ohnosequences.db.ncbitaxonomy

import ohnosequences.forests.Tree
import scala.collection.mutable.{ArrayBuffer, Map => MutableMap}

case object io {

  final case class TreeMap(
      root: Option[TaxNode],
      children: MutableMap[TaxID, Array[TaxNode]]
  )

  // Return a TreeMap
  @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
  def generateNodesMap(lines: Iterator[String]): TreeMap = {

    val children = MutableMap[TaxID, ArrayBuffer[TaxNode]]()
    val nodes    = dmp.nodes.fromLines(lines)

    val root = nodes.foldLeft(Option.empty[TaxNode]) { (maybeRoot, node) =>
      val parent  = node.parentID
      val id      = node.ID
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
    val root     = tree.root
    val children = tree.children
    val init     = Option.empty[TaxID]

    val values = { gen: Option[TaxID] =>
      gen.fold(root match {
        case None =>
          Array.empty[TaxNode]
        case Some(value) =>
          Array(value)
      }) { parent =>
        children.get(parent).getOrElse(Array.empty[TaxNode])
      }
    }

    val next = { gen: Option[TaxID] =>
      // if gen == None, output root
      gen.fold(Array(root.map { _.id })) { parent =>
        val maybeDescendants = children.get(parent)
        maybeDescendants.fold(Array.empty[Option[TaxID]]) { descendants =>
          descendants.map { child =>
            Some(child.id)
          }
        }
      }
    }

    Tree.unfold(values, next)(init)
  }

}

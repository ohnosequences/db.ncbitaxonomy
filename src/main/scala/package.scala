package ohnosequences.db

import ohnosequences.forests._

package object ncbitaxonomy {

  type +[A, B] = Either[A, B]
  type TaxID   = Int
  type TaxTree = Tree[TaxNode]

  val treeDataFile = "data.tree"

  val treeShapeFile = "shape.tree"

  val namesFile = "names.dmp"

  val nodesFile = "nodes.dmp"
}

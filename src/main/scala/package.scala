package ohnosequences.db

import ohnosequences.s3._ // s3Object, s3Folder

import ohnosequences.forests._

package object ncbitaxonomy {

  type +[A, B] = Either[A, B]
  type TaxID   = Int
  type TaxTree = Tree[TaxNode]

  val treeDataFile: String = "data.tree"

  val treeShapeFile: String = "shape.tree"

  val namesFile: String = "names.dmp"

  val nodesFile: String = "nodes.dmp"
}

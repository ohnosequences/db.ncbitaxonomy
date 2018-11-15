package ohnosequences.db

import ohnosequences.forests._

package object ncbitaxonomy {

  type +[A, B] = Either[A, B]
  type TaxID   = Int
  type TaxTree = Tree[TaxNode]

}

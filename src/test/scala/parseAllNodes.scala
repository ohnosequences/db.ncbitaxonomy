package com.bio4j.data.ncbitaxonomy.test
//
// import org.scalatest.FunSuite
// import scala.xml._
// import com.bio4j.data._, ncbiTaxonomy._
//
// case object NCBITaxonomyParsingContext {
//
//   lazy val nodes: Stream[TaxonNode] =
//     io.Source.fromFile("data/in/taxdump/nodes.dmp").getLines.nodes.toStream
//
//   lazy val names: Stream[ScientificName] =
//     io.Source.fromFile("data/in/taxdump/names.dmp").getLines.scientificNames.toStream
//
//   def hasNonEmptyName(node: TaxonNode): Boolean =
//     names.find { n =>
//       n.taxID == node.taxID &&
//       n.scientificName.nonEmpty
//     }.nonEmpty
//
//   def hasValidParent(node: TaxonNode): Boolean = {
//     { node.taxID == "1" &&
//       // root has itself as a parent
//       node.parentTaxID == "1"
//     } || {
//       node.taxID != node.parentTaxID &&
//       nodes.find { _.taxID == node.parentTaxID }.nonEmpty
//     }
//   }
// }
//
// class NCBITaxonomyParsingTests extends FunSuite {
//   import NCBITaxonomyParsingContext._
//
//   ignore("show some nodes") {
//
//     nodes.take(20).foreach { node =>
//       info(f"${node.taxID.toInt}%5d: parent: ${node.parentTaxID.toInt}%8d, rank: ${node.rank}")
//     }
//   }
//
//   ignore("show some names") {
//
//     names.take(20).foreach { name =>
//       info(f"${name.taxID.toInt}%5d: ${name.scientificName}")
//     }
//   }
//
//   test("each node has a non-empty scientific name and a valid parent") {
//     val N = 100
//
//     print("Checking nodes [")
//
//     nodes.take(N).foreach { node =>
//       assert(hasValidParent(node))
//       assert(hasNonEmptyName(node))
//       print(".")
//     }
//
//     println("]")
//
//     info(s"Warning: checked only first ${N}!")
//   }
// }

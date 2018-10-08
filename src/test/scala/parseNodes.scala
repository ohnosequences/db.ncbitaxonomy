package ohnosequences.db.ncbitaxonomy.test

import org.scalatest.OptionValues.convertOptionToValuable
import ohnosequences.db.ncbitaxonomy._

class ParseNodes extends NCBITaxonomyTest("ParseNodes") {

  val nodeLines: Seq[String] =
    Seq(
      """318     |       29438   |       no rank |               |       0       |       1       |       11      |       1       |       0       |       1       |       1       |       0       |               |""",
      """319     |       29438   |       no rank |               |       0       |       1       |       11      |       1       |       0       |       1       |       1       |       0       |               |""",
      """321     |       317     |       no rank |               |       0       |       1       |       11      |       1       |       0       |       1       |       1       |       0       |               |""",
      """322     |       47877   |       no rank |               |       0       |       1       |       11      |       1       |       0       |       1       |       1       |       0       |               |""",
      """323     |       251701  |       no rank |               |       0       |       1       |       11      |       1       |       0       |       1       |       1       |       0       |               |""",
      """329     |       48736   |       species |       RP      |       0       |       1       |       11      |       1       |       0       |       1       |       1       |       0       |               |""",
      """330     |       1232139 |       species |       PP      |       0       |       1       |       11      |       1       |       0       |       1       |       1       |       0       |               |"""
    )

  test("parse several nodes") {

    val nodes =
      parse.nodes.fromLines(nodeLines.toIterator).toSeq

    val firstNode = nodes.headOption.value
    val lastNode  = nodes.lastOption.value

    assert {
      (firstNode.id === 318) &&
      (firstNode.parentID === 29438) &&
      (firstNode.rank === Rank.NoRank)
    }

    assert {
      (lastNode.id === 330) &&
      (lastNode.parentID === 1232139) &&
      (lastNode.rank === Rank.Species)
    }
  }
}

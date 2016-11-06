package com.bio4j.data.ncbitaxonomy.dmp

import com.bio4j.data.ncbitaxonomy._

case object row {

  val fieldSeparator: Char = '|'
  val endOfRow: String = "|"

  def fromLine(line: String): Array[String] =
    line
      .stripSuffix(endOfRow)
      .split(fieldSeparator)
      .map(_.trim)
      .toArray[String]
}

case class Node(val fields: Array[String]) extends AnyVal with AnyNode {

  def ID: String =
    fields(0)

  def parentID: String =
    fields(1)

  def rank: String =
    fields(2)
}

case object Node {

  def from(line: String): Node =
    Node(row.fromLine(line))
}

case object nodes {

  def fromLines(lines: Iterator[String]): Iterator[Node] =
    lines map Node.from
}
case object names {

  def fromLines(lines: Iterator[String]): Iterator[ScientificName] =
    lines
      .collect { case line if(row.fromLine(line)(3) == "scientific name") =>
        val r = row.fromLine(line)
        ScientificName(r(0), r(1))
      }
}

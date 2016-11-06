package com.bio4j.data.ncbitaxonomy.dmp

import com.bio4j.data.ncbitaxonomy._

case object row {

  val fieldSeparator: String = "\t|\t"
  val endOfRow: String = "\t|"

  def fromLine(line: String): Array[String] =
    line
      .stripSuffix(row.endOfRow)
      .split(row.fieldSeparator)
      .map(_.trim)
      .toArray
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
    lines.collect { case line if(row.fromLine(line)(3) == "scientific name") =>

      val r = row.fromLine(line)

      ScientificName(r(0), r(1))
    }
}

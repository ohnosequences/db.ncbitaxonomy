package ohnosequences.db.ncbitaxonomy.dmp

import ohnosequences.db.ncbitaxonomy._

case object row {

  val fieldSeparator: Char = '|'
  val endOfRow: String     = "|"

  def fromLine(line: String): Array[String] =
    line
      .stripSuffix(endOfRow)
      .split(fieldSeparator)
      .map(_.trim)
      .toArray[String]
}

case object Node {

  def fromLine(line: String): Node = {
    val fields   = row.fromLine(line)
    val id       = fields(0).toInt
    val parentID = fields(1).toInt
    val rank     = Rank(fields(2))

    Node(id, parentID, rank)
  }
}

case object names {

  def fromLines(lines: Iterator[String]): Iterator[ScientificName] =
    lines
      .collect {
        case line if (row.fromLine(line)(3) == "scientific name") =>
          val r = row.fromLine(line)
          ScientificName(r(0), r(1))
      }
}

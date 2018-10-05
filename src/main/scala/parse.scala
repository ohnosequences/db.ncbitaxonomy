package ohnosequences.db.ncbitaxonomy

import ohnosequences.files.Lines

case object parse {
  type Field = String
  type Row   = Array[Field]

  case object row {

    val fieldSeparator: Char = '|'
    val endOfRow: String     = "|"

    def fromLine(line: String): Row =
      line
        .stripSuffix(endOfRow)
        .split(fieldSeparator)
        .map(_.trim)
        .toArray[String]
  }

  case object node {

    def fromRow(fields: Row): Node = {
      val id       = fields(0).toInt
      val parentID = fields(1).toInt
      val rank     = Rank(fields(2))

      Node(id, parentID, rank)
    }

    def fromLine(line: String): Node =
      fromRow((row.fromLine(line)): @inline): @inline
  }

  case object names {

    def fromLines(lines: Lines): Iterator[ScientificName] =
      lines
        .map { row.fromLine(_) }
        .filter { fields =>
          fields(3) == "scientific name"
        }
        .map { fields =>
          ScientificName(fields(0).toInt, fields(1))
        }
  }
}

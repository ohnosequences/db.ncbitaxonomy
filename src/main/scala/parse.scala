package ohnosequences.db.ncbitaxonomy

import ohnosequences.files.Lines

// https://coderwall.com/p/lcxjzw/safely-parsing-strings-to-numbers-in-scala
object StringUtils {
  implicit class StringImprovements(val s: String) {
    import scala.util.control.Exception._

    def toIntOpt: Option[Int] =
      catching(classOf[NumberFormatException]) opt s.toInt
  }
}

case object parse {
  type Field = String
  type Row   = Array[Field]

  import StringUtils._

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
    val empty: Option[Node] = Option.empty[Node]

    def fromRow(fields: Row): Option[Node] =
      // We need at least 3 fields to be able to parse something
      if (fields.length >= 3) {
        val maybeID     = fields(0).toIntOpt
        val maybeParent = fields(1).toIntOpt
        val maybeRank   = Rank(fields(2))

        maybeID.fold(empty) { id: TaxID =>
          maybeParent.fold(empty) { parent: TaxID =>
            maybeRank.fold(empty) { rank: Rank =>
              Some(Node(id, parent, rank))
            }
          }
        }
      } else
        empty

    def fromLine(line: String): Option[Node] =
      fromRow((row.fromLine(line)): @inline): @inline
  }

  case object nodes {

    def fromLines(lines: Lines): Iterator[Option[Node]] =
      lines
        .map { node.fromLine(_): @inline }
  }

  case object name {
    val empty: Option[ScientificName] = Option.empty[ScientificName]

    def fromRow(fields: Row): Option[ScientificName] =
      // We need at least 4 fields to be able to parse something
      if (fields.length >= 4 && fields(3) == "scientific name") {
        val maybeID = fields(0).toIntOpt
        val name    = fields(1)

        maybeID.fold(empty) { id =>
          Some(ScientificName(id, name))
        }
      } else
        empty

    def fromLine(line: String): Option[ScientificName] =
      fromRow((row.fromLine(line)): @inline): @inline
  }

  case object names {

    def fromLines(lines: Lines): Iterator[ScientificName] =
      lines
        .map { name.fromLine(_): @inline }
        .collect {
          case Some(name) =>
            name
        }
  }
}

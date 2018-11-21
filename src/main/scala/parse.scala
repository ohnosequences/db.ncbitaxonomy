package ohnosequences.db.ncbitaxonomy

import ohnosequences.files.Lines

/** Auxiliary class to ensure safe parsing from `String`s to `Int`s */
// https://coderwall.com/p/lcxjzw/safely-parsing-strings-to-numbers-in-scala
object StringUtils {
  implicit class StringImprovements(val s: String) {
    import scala.util.control.Exception._

    def toIntOpt: Option[Int] =
      catching(classOf[NumberFormatException]) opt s.toInt
  }
}

/** Object that contains necessary methods to parse `nodes.dmp` and `names.dmp` files */
case object parse {
  type Field = String
  type Row   = Array[Field]

  import StringUtils._

  /** Method to parse rows into a `Row` (an `Array` of `String`) */
  case object row {

    /** Separator between columns of the file */
    val fieldSeparator: Char = '|'

    /** End of each line */
    val endOfRow: String = "|"

    /** Splits columns (or fields) of a line
      *
      * @param line a `String` representing the line of a file
      * @return an `Array` of `String`s
      */
    def fromLine(line: String): Row =
      line
        .stripSuffix(endOfRow)
        .split(fieldSeparator)
        .map(_.trim)
        .toArray[String]
  }

  /** Methods to parse a `Node` */
  case object node {

    /** Just stores an empty `Node` */
    val empty: Option[Node] = Option.empty[Node]

    /** Parses a `Node`, if possible, from a `Row`
      *
      * @param fields an `Array` of `String`s
      * @return Some(node) if it was possible to parse all the necessary fields,
      * None otherwise
      */
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

    /** Parses a `Node`, if possible, from a line
      *
      * @param line a `String` representing the line of a file
      * @return Some(node) if it was possible to parse all the necessary fields,
      * None otherwise
      */
    def fromLine(line: String): Option[Node] =
      fromRow((row.fromLine(line)): @inline): @inline
  }

  /** Methods to parse `Node`s */
  case object nodes {

    /** Parses `Node`s, if possible, from lines of a file
      *
      * @param lines a `Iterator` over `String` representing the lines of a file
      * @return Some(node) in a position if it was possible to parse all the
      * necessary fields, None otherwise in that position
      */
    def fromLines(lines: Lines): Iterator[Option[Node]] =
      lines
        .map { node.fromLine(_): @inline }
  }

  /** Methods to parse a `ScientificName` */
  case object name {

    /** Just stores an empty `Name` */
    val empty: Option[ScientificName] = Option.empty[ScientificName]

    /** Parses a `ScientificName`, if possible, from a `Row`
      *
      * @param fields an `Array` of `String`s
      * @return Some(name) if it was possible to parse all the necessary fields and
      * it represents a `scientific name` indeed, None otherwise
      */
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

    /** Parses a `ScientificName`, if possible, from a line
      *
      * @param line of a file
      * @return Some(name) if it was possible to parse all the necessary fields and
      * it represents a `scientific name` indeed, None otherwise
      */
    def fromLine(line: String): Option[ScientificName] =
      fromRow((row.fromLine(line)): @inline): @inline
  }

  /** Methods to parse all `ScientificName`s from a file */
  case object names {

    /** Parses all `ScientificName`s from a file
      *
      * @param lines, an `Iterator` over the lines of a file
      * @return an `Iterator` over `ScientificName`s, where all lines which
      * could not be parsed into a `ScientificName` were discarded
      */
    def fromLines(lines: Lines): Iterator[ScientificName] =
      lines
        .map { name.fromLine(_): @inline }
        .collect {
          case Some(name) =>
            name
        }
  }
}

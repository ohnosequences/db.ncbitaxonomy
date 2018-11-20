package ohnosequences.db.ncbitaxonomy

import ohnosequences.forests.{Tree, io => treeIO, IOError => SerializationError}
import treeIO.{Serialization, SerializationFormat}
import scala.collection.mutable.{ArrayBuffer, HashMap}
import ohnosequences.files.{read, write, Lines, File, Error => FileError}

case object io {

  import StringUtils._

  /** Default format for the serialization */
  val defaultFormat: SerializationFormat = SerializationFormat("╪", "┼")

  /** Structure to hold a tree of (TaxID, Rank), as a root and a 
    * mapping of each nodes children TaxID -> children = Array[(TaxID, Rank)]
    */
  private final case class RankMap(
      root: Option[IdWithRank],
      children: HashMap[TaxID, Array[IdWithRank]]
  )

  /** Stores the name for each TaxID */
  private type NamesMap = HashMap[TaxID, String]

    /** Structure to hold a tree of TaxNodes, as a root and a mapping of each
      * nodes children TaxID -> children = Array[TaxNode]
    */
  final case class TreeMap(
      root: Option[TaxNode],
      children: HashMap[TaxID, Array[TaxNode]]
  )

  /** Stores a tuple (TaxID, Rank) */
  private final case class IdWithRank(id: TaxID, rank: Rank)

  /** Given the `nodes.dmp` file, as an iterator over its lines, 
    * it maps it to a [[RankMap]]
    * 
    * @param nodesLines an iterator over the lines of `nodes.dmp`
    */
  private def generateRanksMap(nodesLines: Lines): RankMap = {
    val children = new HashMap[TaxID, ArrayBuffer[IdWithRank]]

    val nodes = parse.nodes.fromLines(nodesLines)

    /*
     Find root while looping through the nodes, where
     the root is the node whose parent and id are the same

     Main loop fills children map
     */
    val root = nodes.foldLeft(Option.empty[IdWithRank]) {
      (maybeRoot, maybeNode) =>
        maybeNode match {
          case Some(Node(id, parent, rank)) =>
            val result = IdWithRank(id, rank)

            if (children.isDefinedAt(parent)) {
              children(parent) += result
              maybeRoot
            } else if (id == parent) {
              Some(result)
            } else {
              children += (parent -> ArrayBuffer(result))
              maybeRoot
            }
          case None =>
            maybeRoot
        }
    }

    // Transform ArrayBuffers to Arrays
    val childrenMap = children.map {
      case (parent, descendants) =>
        (parent, descendants.toArray)
    }

    new RankMap(root, childrenMap)
  }

  /** Given the `names.dmp` file, as an iterator over its lines,
    * it maps it to a [[NamesMap]]
    * 
    * @param namesLines an iterator over the lines of `names.dmp`
    */
  private def generateNamesMap(namesLines: Lines): NamesMap = {
    val result = new NamesMap

    val names = parse.names.fromLines(namesLines)

    names.foreach {
      case ScientificName(id, name) =>
        if (!result.isDefinedAt(id))
          result += (id -> name)
    }

    result
  }

  /** Given the `nodes.dmp` and `names.dmp` files, it reads them into a
    * [[TreeMap]]
    * 
    * @param nodesFile `nodes.dmp`
    * @param namesFile `names.dmp`
    * 
    * @return a Left(error) if some error arised reading `nodesFile` or `namesFile`.
    * Otherwise a Right(map), where `map` is a valid [[TreeMap]]
    */
  def generateTreeMap(nodesFile: File, namesFile: File): FileError + TreeMap = {
    // Read nodes file
    val ranksResult = read.withLines(nodesFile) { lines =>
      generateRanksMap(lines)
    }

    ranksResult.flatMap { ranks =>
      // Read names file
      val namesResult = read.withLines(namesFile) { lines =>
        generateNamesMap(lines)
      }

      namesResult.map { names =>
        val root = ranks.root.flatMap {
          case IdWithRank(id, rank) =>
            names.get(id).map { name =>
              TaxNode(id, rank, name)
            }
        }

        val children =
          // If root is not empty, build descendants
          // Else, output empty children, since nothing is
          // going to be built up having a missing root
          if (!root.isEmpty) {
            ranks.children.map {
              case (id, descendants) =>
                val newDescendants = descendants.map {
                  case IdWithRank(id, rank) =>
                    names.get(id).map { name =>
                      TaxNode(id, rank, name)
                    }
                }.flatten

                (id, newDescendants)
            }
          } else {
            HashMap.empty[TaxID, Array[TaxNode]]
          }

        new TreeMap(root, children)
      }
    }
  }

  /** Given a [[TreeMap]] it unfolds a valid [[TaxTree]] from there
    * 
    * @param tree a [[TreeMap]]
    * 
    * @return a [[TaxTree]], which can be empty
    */
  def treeMapToTaxTree(tree: TreeMap): TaxTree = {
    val root     = tree.root
    val children = tree.children
    // Generator. Let's keep in mind that root of the tree is TaxID 1
    val init: TaxID = -1

    // Define values and next function to apply unfold and build tree
    val values = { parent: TaxID =>
      if (parent == init) {
        root match {
          case None =>
            Array.empty[TaxNode]
          case Some(value) =>
            Array(value)
        }
      } else
        children.get(parent).getOrElse(Array.empty[TaxNode])
    }

    val next = { current: TaxID =>
      if (current == init) {
        root match {
          case None =>
            Array.empty[TaxID]
          case Some(taxNode) =>
            Array(taxNode.id)
        }
      } else
        children
          .get(current)
          .fold(Array.empty[TaxID]) { nodes =>
            nodes.map { node =>
              node.id
            }
          }
    }

    Tree.unfold(values, next)(init)
  }

  /** Given the `nodes.dmp` and `names.dmp` files, it reads them into a tree
    * 
    * @param nodesFile `nodes.dmp`
    * @param namesFile `names.dmp`
    * 
    * @return a Left(error) if some error arised reading `nodesFile` or `namesFile`.
    * Otherwise a Right(tree), where `tree` is a valid [[TaxTree]]
    */
  def generateTaxTree(nodesFile: File, namesFile: File): FileError + TaxTree =
    generateTreeMap(nodesFile: File, namesFile: File).map { tree =>
      treeMapToTaxTree(tree)
    }

  /** Serializes a tree into a file for its data and another one for its shape
    * 
    * @param dataFile the file that will hold the data for the tree
    * @param shapeFile the file that will hold the shape for the tree
    * 
    * @return a Left(error) if some error arised writing the `dataFile` or
    * `shapeFile`. Otherwise a Right(files) where files is a tuple
    * (dataFile, shapeFile)
    */
  def dumpTaxTreeToFiles(
      tree: TaxTree,
      dataFile: File,
      shapeFile: File
  ): FileError + (File, File) = {

    val format = defaultFormat

    val serialization = treeIO.serializeTree(tree, format)
    // data and shape are numberedLines, we need to map them to
    // their first element to dump them to a file
    val data  = serialization.data.map { _._1 }
    val shape = serialization.shape.map { _._1 }

    val dataResult = write.linesToFile(dataFile)(data)

    dataResult.flatMap { dataFile =>
      val shapeResult = write.linesToFile(shapeFile)(shape)
      shapeResult.map { shapeFile =>
        (dataFile, shapeFile)
      }
    }
  }


  /** Given a serialized tree (in files), it reads it into a tree data structure
    * 
    * @param dataFile the serialized data file
    * @param shapeFile the serialized shape file
    * 
    * @return a Left(error) if some error arised reading `nodesFile` or `namesFile`
    * (error is a Left) or there is some problem with the serialization (error is a
    * Right). Otherwise, a Right(tree), where `tree` is a valid [[TaxTree]]
    */
  def readTaxTreeFromFiles(
      dataFile: File,
      shapeFile: File
  ): (FileError + SerializationError) + TaxTree = {
    val taxNodeRegex = "(\\d+),([a-zA-Z]*),(.*)".r

    val fromString: String => Option[TaxNode] = { str =>
      // Return a TaxNode iff id, parent and name can be parsed
      str match {
        case taxNodeRegex(idStr, rankStr, name) =>
          idStr.toIntOpt.flatMap { id =>
            Rank(rankStr).map { rank =>
              TaxNode(id, rank, name)
            }
          }
        case _ => None
      }
    }

    // Tries to deserialize the tree from the files
    val treeResult = read.withLines(dataFile) { dataLines =>
      val data = dataLines.zipWithIndex

      read.withLines(shapeFile) { shapeLines =>
        val shape = shapeLines.zipWithIndex

        val serialization = Serialization(
          data,
          shape,
          defaultFormat
        )

        val tree = treeIO.deserializeTree(
          serialization,
          fromString
        )

        tree
      }
    }

    // If error ocurred in the data file retrieval, project to left
    // If error ocurred in the shape file retrieval, it can be either
    // a non-existent file or a SerializationError
    treeResult.fold(
      dataError => Left(Left(dataError)),
      shapeResult =>
        shapeResult.fold(
          shapeError => Left(Left(shapeError)),
          treeResult =>
            treeResult.fold(
              serializationError => Left(Right(serializationError)),
              tree => Right(tree)
          )
      )
    )
  }

}

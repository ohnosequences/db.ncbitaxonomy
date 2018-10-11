package ohnosequences.db.ncbitaxonomy

import ohnosequences.forests.{Tree, io => treeIO, IOError => SerializationError}
import treeIO.{Serialization, SerializationFormat}
import scala.collection.mutable.{ArrayBuffer, HashMap}
import ohnosequences.files.{read, write, Lines, File, Error => FileError}

case object io {

  import StringUtils._

  /** Default format for serialization of the tree: 
    * ;;; to separate siblings and /// to separate families 
    */
  val defaultFormat: SerializationFormat = SerializationFormat(";;;", "///")

  private final case class RankMap(
      root: Option[IdWithRank],
      children: HashMap[TaxID, Array[IdWithRank]]
  )

  private type NamesMap = HashMap[TaxID, String]

  /** Structure to hold a root (`None` if there's no root) and children
    * of each node of a `Tree`
    */
  final case class TreeMap(
      root: Option[TaxNode],
      children: HashMap[TaxID, Array[TaxNode]]
  )

  private final case class IdWithRank(id: TaxID, rank: Rank)

  // Return a RankMap
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

  /** Generates a `TreeMap` to easily retrieve a `Tree` with unfold
    * 
    * @note `root` is the node whose parent is itself, when parsing the file,
    * if such node is found, we will end up with Some(taxNode) in the root,
    * otherwise `root` will be None, and our `Tree` an `EmptyTree`
    * 
    * @param nodesFile path to the `nodes.dmp` file from the NCBI taxonomy
    * @param namesFile path to the `names.dmp` file from the NCBI taxonomy
    * 
    * @return Left(error) if some error ocurred when reading the lines from
    * either `nodesFile` or `namesFile`, otherwise Right(treeMap) where 
    * `treeMap` contains the necessary information to compose this method with
    * `treeMapToTaxTree` and get a `Tree` of `TaxNode`s
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

  /** Generates a `Tree` of `TaxNode`s (id + rank + scientific name) using
    * a `TreeMap` as input
    * 
    * @param tree a `TreeMap`
    * 
    * @return an unfolded tree starting at `tree.root`
    * 
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

  /** Generates a `Tree` of `TaxNode`s (id + rank + scientific name) from a
    * `nodes.dmp` and a `names.dmp` NCBI files
    * 
    * @param nodesFile `nodes.dmp` from the NCBI
    * @param namesFile `names.dmp` from the NCBI
    * 
    * @return Left(error) if some error ocurred when reading the lines from
    * either `nodesFile` or `namesFile`, otherwise Right(taxTree) where 
    * `taxTree` is our `Tree` of `TaxNode`s
    * 
    * @example
    * {{{
    * val nodesFile = new File("./data/in/0.1.0/nodes.dmp")
    * val namesFile = new File("./data/in/0.1.0/names.dmp")
    * 
    * val tree = generateTaxTree(nodesFile, namesFile)
    * }}}
    */
  def generateTaxTree(nodesFile: File, namesFile: File): FileError + TaxTree =
    generateTreeMap(nodesFile: File, namesFile: File).map { tree =>
      treeMapToTaxTree(tree)
    }

  /** Dumps a `Tree` of `TaxNode`s (id + rank + scientific name) to a pair
    * of files: `dataFile`, containing the `TaxNode`s, and `shapeFile`,
    * containing the structure of the tree.
    * 
    * @param tree the tree we want to dump to files
    * @param dataFile file where we want to write the `TaxNode`s
    * @param shapeFile file where we want to write the structure of `tree`
    * 
    * @return Left(error) if some error ocurred when writing to `dataFile`
    * or `shapeFile`, otherwise Right(tuple) where tuple is the pair of 
    * written files (`dataFile`, `shapeFile`)
    * 
    * @example
    * {{{
    * val nodesFile = new File("./data/in/0.1.0/nodes.dmp")
    * val namesFile = new File("./data/in/0.1.0/names.dmp")
    * 
    * val tree = generateTaxTree(nodesFile, namesFile)
    * dumpTaxTreeToFiles(
    *  tree, 
    *  new File("./data/in/0.1.0/data.tree")
    *  new File("./data/in/0.1.0/shape.tree")
    * )
    * }}}
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

  /** Reads a `Tree` of `TaxNode`s (id + rank + scientific name) from a pair
    * of files: `dataFile`, containing the `TaxNode`s, and `shapeFile`,
    * containing the structure of the tree.
    * 
    * @param dataFile file where we want to read the `TaxNode`s from
    * @param shapeFile file where we want to read the structure of `tree` from
    * 
    * @return Left(err) if some error ocurred when reading from `dataFile`
    * or `shapeFile`, otherwise Right(Left(ser)) if some `SerializationError`
    * ocurred (incorrect data or shape), otherwise Right(Right(tree)) where
    * tree is the `Tree` of `TaxNode`s read from `dataFile` and `shapeFile`
    * 
    * @example
    * {{{
    * val dataFile  = new File("./data/in/0.1.0/data.tree")
    * val shapeFile = new File("./data/in/0.1.0/shape.tree")
    * 
    * val treeResult = readTaxTreeFromFiles(dataFile, shapeFile)
    * 
    * treeResult match {
    *   case Left(error: FileError) => println(error.msg)
    *   case Right(Left(error: SerializationError)) => println(error.msg)
    *   case Right(Right(tree)) => // do stuff with the tree
    * }
    * }}}
    */
  def readTaxTreeFromFiles(
      dataFile: File,
      shapeFile: File
  ): FileError + (SerializationError + TaxTree) = {
    val taxNodeRegex = "TaxNode\\((\\d+),([a-zA-Z]*),(.*)\\)".r

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
    treeResult.fold(dataError => Left(dataError), shapeResult => shapeResult)
  }

}

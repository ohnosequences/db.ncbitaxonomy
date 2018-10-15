package ohnosequences.db.ncbitaxonomy


/** Data structure to hold a node of the taxonomy:
  * an `id`, the `rank` of the taxonomy node and its scientific `name`
  */
final case class TaxNode(id: TaxID, rank: Rank, name: String)

/** Data structure to hold an element from `nodes.dmp` file:
  * an `id`, its `parentID` and the `rank` of the taxonomy node
  */
final case class Node(
    id: TaxID,
    parentID: TaxID,
    rank: Rank
)

/** Data structure to hold an element from `names.dmp` file:
  * a `nodeID` and the `name` for that taxonomic element
  */
final case class ScientificName(nodeID: TaxID, name: String)

package ohnosequences.db.ncbitaxonomy

final case class TaxNode(id: TaxID, rank: Rank)

final case class Node(
    id: TaxID,
    parentID: TaxID,
    rank: Rank
)

final case class ScientificName(nodeID: TaxID, name: String)

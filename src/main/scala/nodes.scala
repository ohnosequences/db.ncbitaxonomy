package ohnosequences.db.ncbitaxonomy

final case class TaxNode(id: TaxID, rank: Rank)

trait AnyNode extends Any {

  def ID: String
  def parentID: String
  def rank: Rank
  // there's more data there
}

sealed trait AnyNodeName {

  def nodeID: String
  def name: String
}

final case class ScientificName(nodeID: String, name: String)
    extends AnyNodeName

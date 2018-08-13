package ohnosequences.api.ncbitaxonomy

trait AnyNode extends Any {

  def ID: String
  def parentID: String
  def rank: String
  // there's more data there
}

sealed trait AnyNodeName {

  def nodeID: String
  def name: String
}

final case class ScientificName(nodeID: String, name: String)
    extends AnyNodeName

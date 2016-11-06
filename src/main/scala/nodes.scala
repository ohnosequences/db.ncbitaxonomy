package com.bio4j.data.ncbitaxonomy


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
case class ScientificName(nodeID: String, name: String) extends AnyNodeName

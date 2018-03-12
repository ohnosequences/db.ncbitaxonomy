package com.bio4j.data.ncbitaxonomy.test

import org.scalatest.FunSuite
import com.bio4j.data.ncbitaxonomy._

class ParseAllNodes extends FunSuite {

  def nodeLines =
    io.Source.fromFile("nodes.dmp").getLines

  test("parse all nodes and access all data") {

    dmp.nodes.fromLines(nodeLines) foreach { node =>
      val id     = node.ID
      val parent = node.parentID
      val rank   = node.rank

      // We just want to check whether we can access the values but sbt
      // complaints about the values above being unused, so trick sbt into
      // thinkink we are using them.
      // TODO: Code a proper test instead of this silly trick.
      id + parent + rank
    }
  }
}

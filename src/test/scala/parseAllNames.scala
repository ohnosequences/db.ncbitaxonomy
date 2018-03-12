package com.bio4j.data.ncbitaxonomy.test

import org.scalatest.FunSuite
import com.bio4j.data.ncbitaxonomy._

class ParseAllNames extends FunSuite {

  def namesLines =
    io.Source.fromFile("names.dmp").getLines

  test("parse all names and access all data") {

    dmp.names.fromLines(namesLines) foreach { n =>
      val id   = n.nodeID
      val name = n.name
    }
  }
}

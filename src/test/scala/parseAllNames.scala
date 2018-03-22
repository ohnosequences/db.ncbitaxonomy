package ohnosequences.api.ncbitaxonomy.test

import org.scalatest.FunSuite
import ohnosequences.api.ncbitaxonomy._

class ParseAllNames extends FunSuite {

  def namesLines =
    io.Source.fromFile("names.dmp").getLines

  test("parse all names and access all data") {

    dmp.names.fromLines(namesLines) foreach { n =>
      val id   = n.nodeID
      val name = n.name

      // We just want to check whether we can access the values but sbt
      // complaints about the values above being unused, so trick sbt into
      // thinkink we are using them.
      // TODO: Code a proper test instead of this silly trick.
      id + name
    }
  }
}

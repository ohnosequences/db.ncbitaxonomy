
```scala
package com.bio4j.data.ncbitaxonomy.test

import org.scalatest.FunSuite
import com.bio4j.data.ncbitaxonomy._, dmp._

class ParseAllNodes extends FunSuite {

  def nodeLines =
    io.Source.fromFile("nodes.dmp").getLines

  test("parse all nodes and access all data") {

    dmp.nodes.fromLines(nodeLines) foreach { node =>

      val id      = node.ID
      val parent  = node.parentID
      val rank    = node.rank
    }
  }
}

```




[test/scala/parseNames.scala]: parseNames.scala.md
[test/scala/parseAllNodes.scala]: parseAllNodes.scala.md
[test/scala/parseAllNames.scala]: parseAllNames.scala.md
[test/scala/parseNodes.scala]: parseNodes.scala.md
[main/scala/dmp/format.scala]: ../../main/scala/dmp/format.scala.md
[main/scala/nodes.scala]: ../../main/scala/nodes.scala.md
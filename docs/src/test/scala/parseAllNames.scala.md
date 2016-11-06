
```scala
package com.bio4j.data.ncbitaxonomy.test

import org.scalatest.FunSuite
import com.bio4j.data.ncbitaxonomy._, dmp._

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

```




[test/scala/parseNames.scala]: parseNames.scala.md
[test/scala/parseAllNodes.scala]: parseAllNodes.scala.md
[test/scala/parseAllNames.scala]: parseAllNames.scala.md
[test/scala/parseNodes.scala]: parseNodes.scala.md
[main/scala/dmp/format.scala]: ../../main/scala/dmp/format.scala.md
[main/scala/nodes.scala]: ../../main/scala/nodes.scala.md
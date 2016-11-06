
```scala
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

```




[test/scala/parseNames.scala]: ../../test/scala/parseNames.scala.md
[test/scala/parseAllNodes.scala]: ../../test/scala/parseAllNodes.scala.md
[test/scala/parseAllNames.scala]: ../../test/scala/parseAllNames.scala.md
[test/scala/parseNodes.scala]: ../../test/scala/parseNodes.scala.md
[main/scala/dmp/format.scala]: dmp/format.scala.md
[main/scala/nodes.scala]: nodes.scala.md
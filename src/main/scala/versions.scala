package ohnosequences.db.ncbitaxonomy

sealed abstract class Version { def name: String }
case object Version {

  val latest: Version   = _0_1_0
  val all: Set[Version] = Set(_0_0_1, _0_1_0)

  case object _0_0_1 extends Version { val name = "0.0.1" }
  case object _0_1_0 extends Version { val name = "0.1.0" }
}

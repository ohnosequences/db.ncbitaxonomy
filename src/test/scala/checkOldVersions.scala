package ohnosequences.db.ncbitaxonomy.test

import ohnosequences.db.ncbitaxonomy.{Version, names, nodes}

class CheckOldVersions extends NCBITaxonomyTest("CheckOldVersions") {

  test("S3 objects (nodes and names) exist for all versions") {

    val versionObjects =
      Version.all map { version =>
        (version, names(version), nodes(version))
      }

    assert {
      versionObjects forall {
        case (version, namesObj, nodesObj) =>
          println(s"S3 objects exist: ${version.name}")
          objectExists(namesObj) &&
          objectExists(nodesObj)
      }
    }
  }
}

package ohnosequences.db.ncbitaxonomy.test

import ohnosequences.db.ncbitaxonomy._

class CheckOldVersions extends NCBITaxonomyTest("CheckOldVersions") {

  test("nodes.dmp and names.dmp exist in S3 for all versions") {

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

  test(
    "data.tree and shape.tree exist in S3 for all versions. If not, create them") {

    val versionObjects =
      Version.all map { version =>
        (version, treeData(version), treeShape(version))
      }

    assert {
      versionObjects forall {
        case (version, treeData, treeShape) =>
          println(s"S3 objects exist: ${version.name}")
          objectExists(treeData) &&
          objectExists(treeShape)
      }
    }
  }
}

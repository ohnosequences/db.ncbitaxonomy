package ohnosequences.db.ncbitaxonomy.test

import ohnosequences.db.ncbitaxonomy._
import org.scalatest.DoNotDiscover

@DoNotDiscover
class CheckOldVersions extends NCBITaxonomyTest("CheckOldVersions") {

  test("nodes.dmp and names.dmp exist in S3 for all versions") {

    val versionObjects =
      Version.all map { version =>
        (version, names(version), nodes(version))
      }

    versionObjects foreach {
      case (version, namesObj, nodesObj) =>
        assert(
          objectExists(namesObj) && objectExists(nodesObj),
          s"S3 objects names.dmp and nodes.dmp do not exist for version ${version.name}"
        )
    }
  }

  test(
    "data.tree and shape.tree exist in S3 for all versions") {

    val versionObjects =
      Version.all map { version =>
        (version, treeData(version), treeShape(version))
      }

    versionObjects foreach {
      case (version, treeData, treeShape) =>
        assert(
          objectExists(treeData) && objectExists(treeShape),
          s"S3 objects data.tree and shape.tree do not exist for version ${version.name}"
        )
    }
  }
}

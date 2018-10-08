package ohnosequences.db.ncbitaxonomy.test

import ohnosequences.db.ncbitaxonomy.{Version, names, nodes}

class CheckOldVersions extends org.scalatest.FunSuite {

  private val s3Client = ScalaS3Client(s3.defaultClient)

  test("S3 objects exist for all versions") {

    val versionObjects =
      Version.all map { version =>
        (version, names(version), nodes(version))
      }

    assert {
      versionObjects forall {
        case (version, namesObj, nodesObj) =>
          println(s"S3 objects exist: ${version.name}")
          s3Client.objectExists(namesObj) && s3Client.objectExists(nodesObj)
      }
    }
  }
}

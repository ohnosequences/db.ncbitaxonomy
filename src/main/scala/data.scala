package ohnosequences.db

import ohnosequences.awstools.s3._

package object ncbitaxonomy {

  val version: String =
    "???"

  val s3Prefix: S3Folder =
    s3"resources.ohnosequences.com" /
      "db" /
      "ncbitaxonomy" /
      version /

  val names: S3Object =
    s3Prefix / "names.dmp"

  val nodes: S3Object =
    s3Prefix / "nodes.dmp"
}

package ohnosequences.db

import ohnosequences.awstools.s3._ // S3{Folder,Object} and s3"" string creation

package object ncbitaxonomy {

  val sourceFile: java.net.URI =
    new java.net.URI("ftp://ftp.ncbi.nlm.nih.gov/pub/taxonomy/taxdump.tar.gz")

  val version: String =
    "0.0.1"

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

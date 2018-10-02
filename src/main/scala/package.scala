package ohnosequences.db

import ohnosequences.awstools.s3._ // S3{Folder,Object} and s3"" string creation
import ohnosequences.forests._

package object ncbitaxonomy {

  type +[A, B] = Either[A, B]
  type TaxID   = Int
  type TaxTree = Tree[TaxNode]

  val sourceFile: java.net.URI =
    new java.net.URI("ftp://ftp.ncbi.nlm.nih.gov/pub/taxonomy/taxdump.tar.gz")

  def s3Prefix(version: Version): S3Folder =
    s3"resources.ohnosequences.com" /
      "db" /
      "ncbitaxonomy" /
      version.name /

  def names(version: Version): S3Object =
    s3Prefix(version) / "names.dmp"

  def nodes(version: Version): S3Object =
    s3Prefix(version) / "nodes.dmp"
}
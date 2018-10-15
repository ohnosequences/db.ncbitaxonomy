package ohnosequences.db

import com.amazonaws.services.s3.model.S3ObjectId
import ohnosequences.forests._
import ohnosequences.files.digest.DigestFunction
import java.net.URL

package object ncbitaxonomy {

  type +[A, B]  = Either[A, B]
  type TaxID    = Int
  type TaxTree  = Tree[TaxNode]
  type S3Object = S3ObjectId

  val sourceFile: URL = new URL(
    "ftp://ftp.ncbi.nlm.nih.gov/pub/taxonomy/taxdump.tar.gz")

  def s3Prefix(version: Version): String => S3Object =
    file =>
      new S3Object(
        "resources.ohnosequences.com",
        List(
          "db",
          "ncbitaxonomy",
          version.name,
          file
        ).mkString("/")
    )

  def names(version: Version): S3Object =
    s3Prefix(version)("names.dmp")

  def nodes(version: Version): S3Object =
    s3Prefix(version)("nodes.dmp")

  def treeData(version: Version): S3Object =
    s3Prefix(version)("data.tree")

  def treeShape(version: Version): S3Object =
    s3Prefix(version)("shape.tree")

  val hashingFunction: DigestFunction = DigestFunction.SHA512
}

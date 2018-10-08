package ohnosequences.db

import com.amazonaws.services.s3.model.S3ObjectId
import ohnosequences.forests._
import java.net.URL

package object ncbitaxonomy {

  type +[A, B] = Either[A, B]
  type TaxID   = Int
  type TaxTree = Tree[TaxNode]

  val sourceFile: URL = new URL(
    "ftp://ftp.ncbi.nlm.nih.gov/pub/taxonomy/taxdump.tar.gz")

  def s3Prefix(version: Version): String => S3ObjectId =
    file =>
      new S3ObjectId(
        "resources.ohnosequences.com",
        List(
          "db",
          "ncbitaxonomy",
          version.name,
          file
        ).mkString("/")
    )

  def names(version: Version): S3ObjectId =
    s3Prefix(version)("names.dmp")

  def nodes(version: Version): S3ObjectId =
    s3Prefix(version)("nodes.dmp")
}

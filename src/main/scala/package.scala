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

  /** URL for the NCBI taxonomy file */
  val sourceFile: URL = new URL(
    "ftp://ftp.ncbi.nlm.nih.gov/pub/taxonomy/taxdump.tar.gz")

  /** Returns the bucket + folder where our objects for the NCBI taxonomy reside
    * 
    * @param version the [[Version]] to compute route for
    */
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

  /** Returns the S3 route for the `names.dmp` file and a `version` of the database
    * 
    * @param version the [[Version]] to query
    */
  def names(version: Version): S3Object =
    s3Prefix(version)("names.dmp")

  /** Returns the S3 route for the `nodes.dmp` file and a `version` of the database
    * 
    * @param version the [[Version]] to query
    */
  def nodes(version: Version): S3Object =
    s3Prefix(version)("nodes.dmp")

  /** Returns the S3 route for the `data.tree` file and a `version` of the database
    * 
    * @param version the [[Version]] to query
    */
  def treeData(version: Version): S3Object =
    s3Prefix(version)("data.tree")

  /** Returns the S3 route for the `shape.tree` file and a `version` of the database
    * 
    * @param version the [[Version]] to query
    */
  def treeShape(version: Version): S3Object =
    s3Prefix(version)("shape.tree")

  /** HashingFunction to use when uploading metadata to S3 */
  val hashingFunction: DigestFunction = DigestFunction.SHA512
}

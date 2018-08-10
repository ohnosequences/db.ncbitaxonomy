package ohnosequences.db

import ohnosequences.awstools.s3._ // S3{Folder,Object} and s3"" string creation

package object ncbitaxonomy {

  private[ncbitaxonomy] type +[A, B] =
    Either[A, B]

  val sourceFile: java.net.URI =
    new java.net.URI("ftp://ftp.ncbi.nlm.nih.gov/pub/taxonomy/taxdump.tar.gz")

  sealed abstract class Version { def name: String }
  case object Version {

    val latest: Version = _0_1_0

    case object _0_0_1 extends Version { val name = "0.0.1" }
    case object _0_1_0 extends Version { val name = "0.1.0" }
  }

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

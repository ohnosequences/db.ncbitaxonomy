package ohnosequences.db.ncbitaxonomy

import ohnosequences.s3._
import ohnosequences.files.digest.DigestFunction
import java.net.URL

sealed abstract class Version(val name: String) {
  override final def toString: String = name
}

object Version {

  val all: Set[Version] = Set(v0_0_1, v0_1_0)

  case object v0_0_1 extends Version("0.0.1")
  case object v0_1_0 extends Version("0.1.0")
  case object v0_2_0 extends Version("0.2.0")
}

case object data {

  case object remote {
    val sourceFile: URL = new URL(
      "ftp://ftp.ncbi.nlm.nih.gov/pub/taxonomy/taxdump.tar.gz")
  }

  def s3Prefix(version: Version): String => S3Object =
    version match {
      // TODO: Remove this when we release first stable version
      case Version.v0_2_0 =>
        file =>
          s3"resources.ohnosequences.com" /
            "db" /
            "ncbitaxonomy" /
            "unstable" /
            version.toString /
            file
      case _ =>
        file =>
          s3"resources.ohnosequences.com" /
            "db" /
            "ncbitaxonomy" /
            version.toString /
            file
    }

  val names: Version => S3Object =
    s3Prefix(_)("names.dmp")

  def nodes: Version => S3Object =
    s3Prefix(_)("nodes.dmp")

  def treeData: Version => S3Object =
    s3Prefix(_)("data.tree")

  def treeShape: Version => S3Object =
    s3Prefix(_)("shape.tree")

  val hashingFunction: DigestFunction = DigestFunction.SHA512
}

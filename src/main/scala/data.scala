package ohnosequences.db.ncbitaxonomy

import ohnosequences.s3._
import ohnosequences.files.digest.DigestFunction
import java.net.URL
import java.io.File

sealed abstract class Version(val name: String) {
  override final def toString: String = name
}

object Version {

  val all: Set[Version] = Set(v0_2_0)

  val latest: Version = v0_2_0

  case object v0_2_0 extends Version("0.2.0")
}

case object data {

  /** Folder where we are going to dump data locally */
  val localFolder: Version => File = version => new File(s"./data/${version}")

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
    s3Prefix(_)(namesFile)

  val nodes: Version => S3Object =
    s3Prefix(_)(nodesFile)

  val treeData: Version => S3Object =
    s3Prefix(_)(treeDataFile)

  val treeShape: Version => S3Object =
    s3Prefix(_)(treeShapeFile)

  val everything: Version => Set[S3Object] =
    v => Set(names(v), nodes(v), treeData(v), treeShape(v))

  val hashingFunction: DigestFunction = DigestFunction.SHA512

  case object local {
    val names: Version => File =
      version => new File(localFolder(version), namesFile)

    val nodes: Version => File =
      version => new File(localFolder(version), nodesFile)

    val treeData: Version => File =
      version => new File(localFolder(version), treeDataFile)

    val treeShape: Version => File =
      version => new File(localFolder(version), treeShapeFile)

    val taxDump: Version => File =
      version => new File(localFolder(version), "taxdump.tar.gz")

    val tarFile: Version => File =
      version => new File(localFolder(version), "taxdump.tar")
  }
}

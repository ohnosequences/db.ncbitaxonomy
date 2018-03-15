package ohnosequences.db.ncbitaxonomy.test

import sys.process._ // System process execution: !
import ohnosequences.awstools.s3, s3.S3Object
import java.io.File

case object utils {

  /**
    * Returns `Some(file)` if the download from `uri` to `file`
    * succeeded, `None` otherwise.
    */
  def downloadFrom(uri: java.net.URI, file: File): Option[File] = {
    val command = s"wget ${uri.toString} -O ${file.getCanonicalPath}"
    val result  = command !

    if (result == 0)
      Some(file)
    else
      None
  }

  /**
    * Returns `Some(outputDir)` if the extraction from `input` into `outputDir`
    * succeeded, `None` otherwise.
    */
  def uncompressAndExtractTo(input: File, outputDir: File): Option[File] =
    if (!outputDir.isDirectory)
      None
    else {
      val command =
        s"gzip -c -d ${input.getCanonicalPath}" #|
          s"tar xf - -C ${outputDir.getCanonicalPath}"

      val result = command !

      if (result == 0)
        Some(outputDir)
      else
        None
    }

  /**
    * Returns `Some(s3Object)` if the upload from `file` to `s3Object`
    * succeeded, `None` otherwise.
    */
  def uploadTo(file: File, s3Object: S3Object): Option[S3Object] =
    scala.util.Try {
      s3.defaultClient.putObject(
        s3Object.bucket,
        s3Object.key,
        file
      )
    } match {
      case scala.util.Success(s) => Some(s3Object)
      case scala.util.Failure(e) => None
    }
}

package ohnosequences.db.ncbitaxonomy.test

import ohnosequences.db.ncbitaxonomy.+
import sys.process._ // System process execution: !
import ohnosequences.awstools.s3, s3.S3Object
import java.io.File

sealed trait Error {
  val msg: String
}

case object Error {
  final case class Download(val msg: String)    extends Error
  final case class Upload(val msg: String)      extends Error
  final case class Extract(val msg: String)     extends Error
  final case class DirCreation(val msg: String) extends Error
}

case object utils {

  /**
    * Returns `Some(file)` if the download from `uri` to `file`
    * succeeded, `None` otherwise.
    */
  def downloadFrom(uri: java.net.URI, file: File): Error.Download + File = {
    val command = s"wget ${uri.toString} -O ${file.getCanonicalPath}"
    val result  = command !

    if (result == 0)
      Right(file)
    else
      Left(Error.Download(s"Error downloading $uri to $file."))
  }

  /**
    * Returns `Some(outputDir)` if the extraction from `input` into `outputDir`
    * succeeded, `None` otherwise.
    */
  def uncompressAndExtractTo(input: File,
                             outputDir: File): Error.Extract + File =
    if (!outputDir.isDirectory)
      Left(
        Error.Extract(
          s"Error extracting $input into directory $outputDir: $outputDir is not a directory.")
      )
    else {
      val command =
        s"gzip -c -d ${input.getCanonicalPath}" #|
          s"tar xf - -C ${outputDir.getCanonicalPath}"

      val result = command !

      if (result == 0)
        Right(outputDir)
      else
        Left(
          Error.Extract(
            s"Error extracting $input into directory $outputDir: the command finished with errors.")
        )
    }

  /**
    * Returns `Some(s3Object)` if the upload from `file` to `s3Object`
    * succeeded, `None` otherwise.
    */
  def uploadTo(file: File, s3Object: S3Object): Error.Upload + S3Object =
    scala.util.Try {
      s3.defaultClient.putObject(
        s3Object.bucket,
        s3Object.key,
        file
      )
    } match {
      case scala.util.Success(s) =>
        Right(s3Object)
      case scala.util.Failure(e) =>
        Left(Error.Upload(s"Error uploading$file to $s3Object: ${e.toString}."))
    }

  /**
    * Returns `Some(directory)` if it was possible to create all directories in `directory` (or if they already existed); `None` otherwise.
    */
  def createDirectory(directory: File): Error.DirCreation + File =
    if (!directory.exists)
      if (directory.mkdirs())
        Right(directory)
      else
        Left(Error.DirCreation(s"Error creating directory $directory."))
    else
      Right(directory)
}

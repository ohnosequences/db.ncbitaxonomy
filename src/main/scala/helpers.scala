package ohnosequences.db.ncbitaxonomy

import com.amazonaws.services.s3.AmazonS3ClientBuilder
import ohnosequences.s3.{S3Object, request}
import ohnosequences.files.{directory, gzip, remote, tar}
import java.io.File

/** Helpers:
  * - Partial applications of functions from `s3`, using a standard S3Client
  *   built here, [[helpers.s3Client]], with a default part size,
  *   [[helpers.partSize5MiB]].
  * - Method to check whether all the files for a [[Version]] exist in S3.
  * - Wrappers for files utilities (uncompress, extract, download)
  */
private[ncbitaxonomy] case object helpers {

  lazy val s3Client = AmazonS3ClientBuilder.standard().build()

  val partSize5MiB = 5 * 1024 * 1024

  /** Downloads the specified `s3Obj` to a given `file` */
  def download(s3Obj: S3Object, file: File) =
    request
      .getCheckedFile(s3Client)(s3Obj, file)
      .left
      .map { err =>
        Error.S3Error(err)
      }

  /** Uploads the specified `file` a given `s3Obj` */
  def upload(file: File, s3Obj: S3Object) =
    request
      .paranoidPutFile(s3Client)(file, s3Obj, partSize5MiB)(
        data.hashingFunction
      )
      .left
      .map { err =>
        Error.S3Error(err)
      }

  /** Returns true when object does not exists or communication with S3
    * cannot be established */
  def objectExists(s3Obj: S3Object) =
    request
      .objectExists(s3Client)(s3Obj)
      .fold(
        err => true,
        identity
      )

  /**
    * Finds any object under [[data.prefix(version)]] that could be overwritten
    * by [[mirrorNewVersion]].
    *
    * @param version is the version that specifies the S3 folder
    *
    * @return Some(object) with the first object found under
    * [[data.prefix(version)]] if any, None otherwise.
    */
  def findVersionInS3(version: Version): Option[S3Object] =
    data
      .everything(version)
      .find(
        obj => objectExists(obj)
      )

  /** Tries to download the contents from an `url` into a `file` using the CLI
    * tool wget.
    *
    * @param uri is the URI from where the content will be fetched.
    * @param file is the file where the downloaded contents will be stored.
    *
    * @return `Right(file)` if the download succeeded, `Left(Error)` otherwise.
    */
  def downloadFromURL(url: java.net.URL, file: File): Error + File =
    remote.download(url, file).left.map(Error.FileError)

  /** Tries to uncompress a gzip file `input` to `output`.
    *
    * @param input is the file to be uncompressed.
    * @param output is the file where the uncompressed version of `input` will
    * be stored.
    *
    * @return `Right(output)` if the uncompression from `input` to `output`
    * succeeded, `Left(Error)` otherwise.
    */
  def uncompress(input: File, output: File): Error + File =
    gzip.uncompress(input, output).left.map(Error.FileError)

  /** Tries to extract the contents of a tar file `input` into the directory
    * `outputDir`.
    *
    * @param input is the file to be extracted.
    * @param outputDir is the directory that will contain the extracted `input`.
    *
    * @return `Right(outputDir)` if the extraction from `input` into `outputDir`
    * succeeded, `Left(Error)` otherwise.
    */
  def extract(input: File, outputDir: File): Error + File =
    tar.extract(input, outputDir).left.map(Error.FileError)

  /** Tries to create the directory specified by `directory`
    *
    * @note If the directory already exists, this method does nothing.
    *
    * @param folder is the file pointing to the directory to create.
    *
    * @return a `Left(error)` if the file exists and it is not a directory or if
    * the creation of the directory failed; a `Right(directory)` otherwise.
    */
  def createDirectory(folder: File): Error + File =
    directory.createDirectory(folder).left.map(Error.FileError)

  /** Tries to recursively delete the directory specified by `directory`, in case
    * it exists
    *
    * @note This method tries to delete a directory and all of its content,
    * failing if any of its content deletions fails
    *
    * @param folder is the file pointing to the directory to delete.
    *
    * @return a `Left(error)` if the directory is not a valid folder or
    * if the deletion of the directory failed; a `Right(boolean)`
    * otherwise, where boolean indicates whether it was possible to delete the
    * directory or not
    */
  def deleteDirectory(folder: File): Error + Boolean =
    if (folder.isDirectory)
      directory.recursiveDeleteDirectory(folder).left.map(Error.FileError)
    else
      Right(false)

  /** Downloads the `s3Obj` to `file` whenever `file` does not exist
    * or its checksum is different from the `s3Obj` checksum
    */
  def getFileIfDifferent(s3Obj: S3Object, file: File) =
    request
      .getCheckedFileIfDifferent(s3Client)(s3Obj, file)
      .left
      .map { err =>
        Error.S3Error(err)
      }

}

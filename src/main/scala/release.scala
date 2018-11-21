package ohnosequences.db.ncbitaxonomy

import ohnosequences.s3.S3Object
import java.io.File
import helpers._
import data._

case object release {

  /** Downloads, extracts and uncompresses the file from [[remote.sourceFile]],
    * which is stored in the NCBI ftp server
    *
    * @param version the [[Version]] we want to get the files for. This parameter
    * is only used to store the files in a certain version folder. The downloaded
    * NCBI file is independent of this parameter.
    *
    * @return a Left(error) if some error arised during the process. Otherwise,
    * a Right(files) where files is a tuple (names, nodes) with the path to
    * `names.dmp` and `nodes.dmp` files, present in the remote `.tar.gz` file
    */
  private def getTaxDump(version: Version): Error + (File, File) = {
    val taxDump   = local.taxDump(version)
    val tarFile   = local.tarFile(version)
    val nodesFile = local.nodes(version)
    val namesFile = local.names(version)

    for {
      toUncompress <- downloadFromURL(remote.sourceFile, taxDump)
      tarFile      <- uncompress(toUncompress, tarFile)
      _            <- extract(tarFile, localFolder(version))
    } yield {
      (nodesFile, namesFile)
    }
  }

  /** Performs the mirroring of the NCBI tree
    *
    * For a given [[Version]]:
    *
    *   1. Cleans the local folder for the given [[Version]]
    *   2. Creates the local folder where the objects are going to be stored:
    *      [[localFolder]]
    *   3. Downloads and extracts the `taxdump.tar.gz` file from the NCBI ftp
    *   4. Generates the tree from the `nodes.dmp` and `names.dmp` file
    *   5. Serializes the generated tree into a pair of files `data.tree` and
    *      `shape.tree`
    *   6. Uploads `nodes.dmp`, `names.dmp`, `data.tree` and `shape.tree` to
    *      S3.
    *
    * @param version the [[Version]] we want to mirror
    *
    * @return Right(objects) where objects is a collection of all the mirrored
    * files if everything went smoothly. Otherwise a Left(error), where error
    * can be due to:
    *
    *   - An error in the deletion / creation of the folders
    *   - An error in the download / uncompression / extraction of the
    *     `taxdump.tar.gz` file
    *   - An error generating the tree
    *   - An error writing `data.tree` / `shape.tree`
    *   - An error uploading some of the files
    */
  private def mirrorVersion(version: Version): Error + Set[S3Object] = {
    val genTree     = (io.generateTaxTree _).tupled
    val remoteNodes = nodes(version)
    val remoteNames = names(version)
    val data        = local.treeData(version)
    val shape       = local.treeShape(version)
    val localNodes  = local.nodes(version)
    val localNames  = local.names(version)
    val remoteData  = treeData(version)
    val remoteShape = treeShape(version)
    val localDir    = localFolder(version)

    for {
      _     <- deleteDirectory(localDir)
      _     <- createDirectory(localDir)
      files <- getTaxDump(version)
      tree  <- genTree(files)
      _     <- io.dumpTaxTreeToFiles(tree, data, shape)
      _     <- upload(data, remoteData)
      _     <- upload(shape, remoteShape)
      _     <- upload(localNodes, remoteNodes)
      _     <- upload(localNames, remoteNames)
    } yield {
      Set(remoteData, remoteShape, remoteNodes, remoteNames)
    }
  }

  /** Mirrors a new version of the NCBI taxonomy to S3 iff the upload does
    * not overwrite anything.
    *
    * This method tries to download the NCBI taxonomy file from the NCBI
    * ftp server, extract it, generate the tree from the raw `nodes.dmp`
    * and `names.dmp` files and generate and upload the tree and the files
    * needed to generate it to S3
    *
    * @param version is the new version that wants to be released
    *
    * @return an Error + Set[S3Object], with a Right(set) with all the mirrored
    * S3 objects if everything worked as expected or with a Left(error) if an
    * error occurred.
    */
  def mirrorNewVersion(version: Version): Error + Set[S3Object] =
    findVersionInS3(version).fold(
      mirrorVersion(version)
    ) { obj =>
      Left(Error.S3ObjectExists(obj))
    }

}

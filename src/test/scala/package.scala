package ohnosequences.db.ncbitaxonomy

import com.amazonaws.services.s3.AmazonS3ClientBuilder
import ohnosequences.s3.request

package object test {

  private[test] val s3Client = AmazonS3ClientBuilder.standard().build()

  private[test] def downloadFromS3(s3Obj: S3ObjectId, file: File) =
    request.getCheckedFile(s3Client)(s3Obj, file)

  private[test] def paranoidPutFile(file: File, s3Obj: S3ObjectId) =
    request.paranoidPutFile(s3Client)(file, s3Obj, partSize5MiB)(
      rnacentral.data.hashingFunction
)

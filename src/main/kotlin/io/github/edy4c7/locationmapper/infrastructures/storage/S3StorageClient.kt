package io.github.edy4c7.locationmapper.infrastructures.storage

import io.github.edy4c7.locationmapper.domains.interfaces.storage.StorageClient
import org.springframework.stereotype.Component
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.Delete
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest
import software.amazon.awssdk.services.s3.model.ObjectIdentifier
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.nio.file.Path
import kotlin.io.path.name

@Component
internal class S3StorageClient(private val client: S3Client) :
    StorageClient {
    override fun upload(bucketName: String, data: Path, attachmentName: String): String {
        val req = PutObjectRequest.builder().bucket(bucketName).key(data.name)
            .contentDisposition("attachment; filename=\"$attachmentName\"")
            .build()

        client.putObject(req, RequestBody.fromFile(data))

        return req.key()
    }

    override fun delete(bucketName: String, vararg keys: String): List<String> {
        val delete = Delete.builder().objects(keys.map {
            ObjectIdentifier.builder().key(it).build()
        }).build()
        val res = client.deleteObjects(DeleteObjectsRequest.builder().bucket(bucketName).delete(delete).build())
        return res.deleted().map {
            it.key()
        }
    }
}
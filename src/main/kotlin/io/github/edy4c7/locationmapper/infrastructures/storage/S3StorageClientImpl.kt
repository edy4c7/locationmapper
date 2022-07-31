package io.github.edy4c7.locationmapper.infrastructures.storage

import io.github.edy4c7.locationmapper.domains.interfaces.storage.StorageClient
import org.springframework.stereotype.Component
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import java.net.URL
import java.nio.file.Path
import kotlin.io.path.name

@Component
class S3StorageClient(private val client: S3Client) :
    StorageClient {
    override fun upload(bucketName: String, data: Path, attachmentName: String): URL {
        client.putObject(
            PutObjectRequest.builder().bucket(bucketName).key(data.name)
                .contentDisposition("attachment; filename=\"$attachmentName\"")
                .build(),
            RequestBody.fromFile(data)
        )
        return client.utilities().getUrl(GetUrlRequest.builder().bucket(bucketName).key(data.name).build())
    }

    override fun delete(bucketName: String, vararg keys: String) {
        val delete = Delete.builder().objects(keys.map {
            ObjectIdentifier.builder().key(it).build()
        }).build()
        client.deleteObjects(DeleteObjectsRequest.builder().bucket(bucketName).delete(delete).build())
    }
}
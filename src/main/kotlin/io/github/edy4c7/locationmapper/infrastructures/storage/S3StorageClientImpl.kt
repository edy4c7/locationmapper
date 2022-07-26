package io.github.edy4c7.locationmapper.infrastructures.storage

import io.github.edy4c7.locationmapper.domains.interfaces.storage.StorageClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.nio.file.Path

@Component
class S3StorageClient(@Value("\${s3.bucket}")private val bucketName:String, private val client: S3Client) : StorageClient {
    override fun upload(key: String, data: Path) {
        client.putObject(PutObjectRequest.builder().bucket(bucketName).key(key).build(),
            RequestBody.fromFile(data))
    }
}
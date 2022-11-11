package io.github.edy4c7.locationmapper.domains.interfaces

import java.nio.file.Path

internal interface StorageClient {
    fun upload(bucketName: String, key: String, attachmentName: String, data: Path): String

    fun delete(bucketName: String, vararg keys: String): List<String>
}
package io.github.edy4c7.locationmapper.domains.interfaces.storage

import java.net.URL
import java.nio.file.Path

interface StorageClient {
    fun upload(bucketName: String, data: Path, attachmentName: String): URL

    fun delete(bucketName: String, vararg keys: String)
}
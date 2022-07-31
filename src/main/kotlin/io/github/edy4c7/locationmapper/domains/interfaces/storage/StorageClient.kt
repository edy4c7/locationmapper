package io.github.edy4c7.locationmapper.domains.interfaces.storage

import java.net.URL
import java.nio.file.Path

interface StorageClient {
    fun upload(key: String, attachmentName: String, data: Path): URL

    fun delete(vararg keys: String)
}
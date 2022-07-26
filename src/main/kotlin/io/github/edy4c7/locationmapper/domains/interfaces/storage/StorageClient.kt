package io.github.edy4c7.locationmapper.domains.interfaces.storage

import java.nio.file.Path

interface StorageClient {
    fun upload(key: String, data: Path)
}
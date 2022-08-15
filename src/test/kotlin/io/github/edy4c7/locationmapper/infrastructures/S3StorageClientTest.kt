package io.github.edy4c7.locationmapper.infrastructures

import io.github.edy4c7.locationmapper.infrastructures.storage.S3StorageClient
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import java.nio.file.Path
import kotlin.io.path.name

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
private class S3StorageClientTest {
    @MockK(relaxed = true)
    private lateinit var client: S3Client

    @InjectMockKs
    private lateinit var storageClient: S3StorageClient

    @BeforeAll
    fun beforeAll() {
        MockKAnnotations.init(this)
    }

    @Test
    fun testUpload() {
        val data = mockk<Path>(relaxed = true)

        every { data.name } returns "abcd1234"

        storageClient.upload("bucketName", "abcd1234", "locationmapper.zip", data)

        verify {
            client.putObject(PutObjectRequest.builder()
                .bucket("bucketName")
                .key("abcd1234")
                .contentDisposition("attachment; filename=\"locationmapper.zip\"")
                .build(), data)
        }
    }

    @Test
    fun testDelete() {
        val res = mockk<DeleteObjectsResponse>()
        every { res.deleted() } returns listOf(
            DeletedObject.builder().key("key1").build(),
            DeletedObject.builder().key("key2").build()
        )
        every { client.deleteObjects(any<DeleteObjectsRequest>()) } returns res

        val ret = storageClient.delete("bucketName", "key1", "key2", "key3")

        verify {
            client.deleteObjects(DeleteObjectsRequest.builder()
                .bucket("bucketName")
                .delete(
                    Delete.builder().objects(
                        ObjectIdentifier.builder().key("key1").build(),
                        ObjectIdentifier.builder().key("key2").build(),
                        ObjectIdentifier.builder().key("key3").build()
                    )
                        .build()
                )
                .build()
            )
        }

        assertEquals(listOf("key1", "key2"), ret)
    }
}
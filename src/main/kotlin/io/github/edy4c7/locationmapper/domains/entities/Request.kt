package io.github.edy4c7.locationmapper.domains.entities

import io.github.edy4c7.locationmapper.domains.valueobjects.JobStatus
import org.seasar.doma.Entity
import org.seasar.doma.Id
import org.seasar.doma.Version

@Entity
class Request {
    @Id
    var id: String = ""

    var status: JobStatus = JobStatus.RESERVED

    @Version var version: Long = -1
}
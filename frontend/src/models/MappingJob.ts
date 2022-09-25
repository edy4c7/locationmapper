import JobStatus from "./JobStatus"

interface MappingJob {
  id: string
  status: JobStatus
  url: string|null
}

export default MappingJob
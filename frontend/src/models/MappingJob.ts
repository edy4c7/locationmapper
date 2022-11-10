import JobStatus from "./JobStatus"

interface MappingJob {
  id: string
  status: JobStatus
  url?: string
}

export default MappingJob

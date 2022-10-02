import axios from 'axios';
import { useCallback, useState } from 'react';
import JobStatus from '../models/JobStatus';
import MappingJob from '../models/MappingJob';

interface State {
  id: string
  status: JobStatus
}

const api = axios.create({
  baseURL: 'http://localhost:8080'
})

export default function useApi() {
  const [ state, setState ] = useState<State>({
    id: '',
    status: 'UNKNOWN',
  })

  const submit = useCallback(async (file: File) => {
    const res = (await api.postForm<MappingJob>('/mapping', {
      'nmea': file
    })).data

    const {id, status} = res
    setState({...state, id, status})

    const timer = setInterval(async () => {
      const res = (await api.get<MappingJob>(`/mapping/${id}`)).data
      const { status } = res

      setState(ps => ({...ps, status}))

      if(status !== 'STARTING' && status !== 'STARTED') {
        clearInterval(timer)
      }
    }, 10000)
  }, [state])

  return [submit, state] as const
}
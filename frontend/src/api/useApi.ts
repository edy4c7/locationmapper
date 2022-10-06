import { AxiosInstance } from 'axios';
import { useInjection } from 'inversify-react';
import { useCallback, useState } from 'react';
import JobStatus from '../models/JobStatus';
import MappingJob from '../models/MappingJob';

interface State {
  id: string
  status: JobStatus
}

export default function useApi() {
  const axios = useInjection<AxiosInstance>('axios')
  const [ state, setState ] = useState<State>({
    id: '',
    status: 'UNKNOWN',
  })

  const submit = useCallback(async (file: File) => {
    const res = (await axios.postForm<MappingJob>('/mapping', {
      'nmea': file
    })).data

    const {id, status} = res
    setState({...state, id, status})

    const timer = setInterval(async () => {
      const res = (await axios.get<MappingJob>(`/mapping/${id}`)).data
      const { status } = res

      setState(ps => ({...ps, status}))

      if(status !== 'STARTING' && status !== 'STARTED') {
        clearInterval(timer)
      }
    }, 10000)
  }, [axios, state])

  return [submit, state] as const
}
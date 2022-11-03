import { AxiosInstance } from 'axios';
import { useInjection } from 'inversify-react';
import { useCallback, useReducer, useState } from 'react';
import JobStatus from '../models/JobStatus';
import MappingJob from '../models/MappingJob';

interface State {
  id?: string
  status?: JobStatus
  isPolling: boolean
}

type StartAction = {
  type: 'START',
  args: { id: string, status: JobStatus }
}

type UpdateAction = {
  type: 'UPDATE',
  args: { status: JobStatus }
}

type Action = UpdateAction | StartAction

const reducer = (state: State, action: Action): State => {
  const { status } = action.args
  const isPolling = (status === 'STARTING' || status === 'STARTED')
  switch (action.type) {
    case 'START':
      return { id: action.args.id, status, isPolling }
    case 'UPDATE':
      return { ...state, status, isPolling }
  }
}

export default function useApi() {
  const axios = useInjection<AxiosInstance>('axios')
  const [ state, dispatch ] = useReducer(reducer, {
    isPolling: false,
  })

  const submit = useCallback(async (file: File) => {
    const res = (await axios.postForm<MappingJob>('/mapping', {
      'nmea': file
    })).data

    const {id, status} = res
    dispatch({ type: 'START', args: {id, status} })

    const timer = setInterval(async () => {
      const res = (await axios.get<MappingJob>(`/mapping/${id}`)).data
      const { status } = res

      dispatch({ type: 'UPDATE', args: { status } })

      if(!state.isPolling) {
        clearInterval(timer)
      }
    }, 10000)
  }, [axios, state.isPolling])

  return [submit, state] as const
}
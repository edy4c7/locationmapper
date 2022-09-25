import React, { useState } from 'react';
import { Button, Box, Stack } from '@mui/material';
import { useAppContext } from './context/context';
import JobStatus from './models/JobStatus';
import MappingJob from './models/MappingJob';

interface State {
  id: string
  status: JobStatus
  file: File | null
}

function App() {
  const { axios } = useAppContext()
  const [ state, setState ] = useState<State>({
    id: '',
    status: 'UNKNOWN',
    file: null
  })

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if(state.file) {
      const res = (await axios.postForm<MappingJob>('/mapping', {
        'nmea': state.file
      })).data

      const {id, status} = res
      setState({...state, id, status})
      const timer = setInterval(async () => {
        const res = (await axios.get<MappingJob>(`/mapping/${id}`)).data
        const { status } = res

        if(status !== 'STARTING' && status !== 'STARTED') {
          clearInterval(timer)
        }
      }, 10000)
    }
  }

  return (
    <Stack>
      <Stack component="form" direction="row" spacing={2} onSubmit={onSubmit}>
        <Stack direction="row" spacing={1} sx={{flex: "1 1 auto", border: '1px dashed grey' }}>
          <Button
            variant="contained"
            component="label"
          >
            GPSログ(NMEA)を選択
            <input
              type="file"
              hidden
              onChange={(e) => {
                if(state && e.target.files?.[0]) {
                  setState({...state, file: e.target.files?.[0]})
                }
              }}
            />
          </Button>
          <Stack direction="row" sx={{flex: "1 1 auto", alignItems: 'center'}}>
            {state.file?.name}
          </Stack>
        </Stack>
        <Box>
          <Button variant="contained" color='primary' type='submit'>送信</Button>
        </Box>
      </Stack> 
      <Stack>{state.id ? state.status : ''}</Stack>
    </Stack>
  );
}

export default App

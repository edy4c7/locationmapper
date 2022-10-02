import React, { useState } from 'react';
import { Button, Box, Stack } from '@mui/material';
import useApi from './api/useApi';

function App() {
  const [file, setFile] = useState<File>()
  const [submit, state] = useApi()

  function onSubmit(e: React.FormEvent) {
    e.preventDefault()
    file && submit(file)
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
                  setFile(e.target.files?.[0])
                }
              }}
            />
          </Button>
          <Stack direction="row" sx={{flex: "1 1 auto", alignItems: 'center'}}>
            {file?.name}
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

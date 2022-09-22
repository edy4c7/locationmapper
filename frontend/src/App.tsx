import React from 'react';
import { Button, Box, Stack } from '@mui/material';

function App() {
  const [fileName, setFileName] = React.useState('')
  return (
    <Stack component="form" direction="row" spacing={2}>
      <Stack direction="row" spacing={1} sx={{flex: "1 1 auto", border: '1px dashed grey' }}>
        <Button
          variant="contained"
          component="label"
        >
          GPSログ(NMEA)を選択
          <input
            type="file"
            hidden
            onChange={(e) => setFileName(e.target.files?.[0].name ?? '')}
          />
        </Button>
        <Stack direction="row" sx={{flex: "1 1 auto", alignItems: 'center'}}>
          {fileName}
        </Stack>
      </Stack>
      <Box>
        <Button variant="contained" color='primary'>送信</Button>
      </Box>
    </Stack> 
  );
}

export default App;

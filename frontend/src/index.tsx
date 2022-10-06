import React, { useRef } from 'react';
import ReactDOM from 'react-dom/client';
import { Container, createTheme, ThemeProvider, CssBaseline } from '@mui/material';
import './index.css';
import App from './App';
import reportWebVitals from './reportWebVitals';
import { context } from './context/context'
import axios, { AxiosInstance } from 'axios';
import { Provider } from 'inversify-react';
import { Container as InversifyContainer } from 'inversify'

const root = ReactDOM.createRoot(
  document.getElementById('root') as HTMLElement
);
const isDarkMode = matchMedia('(prefers-color-scheme: dark)').matches
const theme = createTheme({
  palette: {
    mode: isDarkMode ? 'dark' : 'light'
  }
})

const myAxios = axios.create({
  baseURL: 'http://localhost:8080'
})

root.render(
  <React.StrictMode>
    <Provider container={() => {
      const container = new InversifyContainer()
      container.bind<AxiosInstance>('axios').toConstantValue(axios.create({
        baseURL: 'http://localhost:8080'
      }))
      return container
    }}>
      <context.Provider value={{axios: myAxios}}>
        <ThemeProvider theme={theme}>
          <CssBaseline />
          <Container maxWidth="md">
            <h1>Location Mapper</h1>
            <App />
          </Container>
        </ThemeProvider>
      </context.Provider>
    </Provider>
  </React.StrictMode>
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();

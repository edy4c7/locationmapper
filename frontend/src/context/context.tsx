import axios, { AxiosInstance } from "axios";
import React, { useContext, useRef } from "react";

export const context = React.createContext({
  axios: axios as AxiosInstance,
})

export const Provider = ({ children }: React.PropsWithChildren<{}>) => {
  const axiosRef = useRef(axios.create({
    baseURL: 'http://localhost:8080'
  }))

  return <context.Provider value={{axios: axiosRef.current}}>{children}</context.Provider>
}

export function useAppContext() {
  return useContext(context)
}

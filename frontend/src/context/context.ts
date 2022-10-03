import axios, { AxiosInstance } from "axios";
import React, { useContext } from "react";

export const context = React.createContext({
  axios: axios as AxiosInstance,
})

export function useAppContext() {
  return useContext(context)
}

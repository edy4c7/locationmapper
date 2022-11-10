import { AxiosInstance } from "axios"
import { useInjection } from "inversify-react"
import { useCallback, useReducer, useState } from "react"
import JobStatus from "../models/JobStatus"
import MappingJob from "../models/MappingJob"

interface State {
  id?: string
  status?: JobStatus
  isPolling: boolean
  url?: string
}

type StartAction = {
  type: "START"
  args: { id: string; status: JobStatus }
}

type UpdateAction = {
  type: "UPDATE"
  args: { status: JobStatus }
}

type CompleteAction = {
  type: "COMPLETE"
  args: { status: JobStatus; url?: string }
}

type Action = UpdateAction | StartAction | CompleteAction

const reducer = (state: State, action: Action): State => {
  const { status } = action.args
  const isPolling = status === "STARTING" || status === "STARTED"
  switch (action.type) {
    case "START":
      return { ...state, id: action.args.id, status, isPolling }
    case "UPDATE":
      return { ...state, status, isPolling }
    case "COMPLETE":
      const { url } = action.args
      return { ...state, status, isPolling, url }
  }
}

export default function useApi() {
  const axios = useInjection<AxiosInstance>("axios")
  const [state, dispatch] = useReducer(reducer, {
    isPolling: false,
  })

  const submit = useCallback(
    async (file: File) => {
      const res = (
        await axios.postForm<MappingJob>("/mapping", {
          nmea: file,
        })
      ).data

      const { id, status } = res
      dispatch({ type: "START", args: { id, status } })

      const timer = setInterval(async () => {
        const res = (await axios.get<MappingJob>(`/mapping/${id}`)).data
        const { status, url } = res

        dispatch({ type: "UPDATE", args: { status } })

        if (status !== "STARTING" && status !== "STARTED") {
          dispatch({ type: "COMPLETE", args: { status, url } })
          clearInterval(timer)
        }
      }, 10000)
    },
    [axios]
  )

  return [submit, state] as const
}

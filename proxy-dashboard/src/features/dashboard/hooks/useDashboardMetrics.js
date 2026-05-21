import { useCallback, useEffect, useRef, useState } from 'react'
import { appConfig } from '@/config/appConfig.js'
import { getDashboardMetrics } from '@/features/dashboard/services/dashboardMetricsService.js'

export function useDashboardMetrics() {
  const [metrics, setMetrics] = useState(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')
  const [lastUpdated, setLastUpdated] = useState(null)
  const abortControllerRef = useRef(null)

  const refreshMetrics = useCallback(async () => {
    abortControllerRef.current?.abort()

    const controller = new AbortController()
    abortControllerRef.current = controller

    setError('')

    try {
      const nextMetrics = await getDashboardMetrics(controller.signal)
      setMetrics(nextMetrics)
      setLastUpdated(new Date())
    } catch (refreshError) {
      if (refreshError.name !== 'AbortError') {
        setError('No fue posible cargar las métricas del proxy.')
      }
    } finally {
      if (!controller.signal.aborted) {
        setIsLoading(false)
      }
    }
  }, [])

  useEffect(() => {
    window.setTimeout(() => {
      refreshMetrics()
    }, 0)

    const intervalId = window.setInterval(() => {
      refreshMetrics()
    }, appConfig.defaultRefreshIntervalMs)

    return () => {
      window.clearInterval(intervalId)
      abortControllerRef.current?.abort()
    }
  }, [refreshMetrics])

  return {
    error,
    isLoading,
    lastUpdated,
    metrics,
    refreshMetrics,
  }
}

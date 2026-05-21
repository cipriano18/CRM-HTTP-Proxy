export const appConfig = {
  dashboardPort: 3000,
  defaultRefreshIntervalMs: 3000,
  metricsEndpoint:
    import.meta.env.VITE_PROXY_METRICS_URL ??
    'http://localhost:8080/api/dashboard/metrics',
}

export const appConfig = {
  dashboardPort: 3000,
  defaultRefreshIntervalMs: 3000,

  metricsEndpoint:
    import.meta.env.VITE_PROXY_METRICS_URL ??
    'http://172.17.47.227:8090/api/dashboard/metrics',
}
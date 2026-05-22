import { appConfig } from '../../../config/appConfig'

function normalizeMetrics(data) {
  return {
    totalRequests: data.totalRequests ?? 0,
    transferredMegabytes: data.totalMB ?? 0,
    allowedRequests: data.allowed ?? 0,
    blockedRequests: data.blocked ?? 0,

    domains: (data.domains ?? []).map((item) => ({
      domain: item.domain,
      requests: item.count,
    })),

    activeClients: (data.clients ?? []).map((item) => ({
      ip: item.ip,
      requests: item.count,
    })),
  }
}

export async function getMockDashboardMetrics() {
  const response = await fetch(appConfig.metricsEndpoint)

  if (!response.ok) {
    throw new Error('No se pudieron cargar las métricas del proxy')
  }

  const data = await response.json()

  return normalizeMetrics(data)
}
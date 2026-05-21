import { appConfig } from '@/config/appConfig.js'
import { getMockDashboardMetrics } from '@/features/dashboard/data/mockDashboardMetrics.js'

function normalizeDomain(domain) {
  return {
    domain: domain.domain ?? '',
    requests: domain.requests ?? domain.count ?? 0,
  }
}

function normalizeClient(client) {
  return {
    ip: client.ip ?? '',
    requests: client.requests ?? client.count ?? 0,
  }
}

function normalizeMetricsResponse(payload) {
  const allowedRequests = payload.allowedRequests ?? payload.allowed ?? 0
  const blockedRequests = payload.blockedRequests ?? payload.blocked ?? 0

  return {
    totalRequests: payload.totalRequests ?? payload.requests ?? 0,
    transferredMegabytes:
      payload.transferredMegabytes ??
      payload.transferredMb ??
      payload.volumeMb ??
      payload.totalMB ??
      0,
    allowedRequests,
    blockedRequests,
    domains: (payload.domains ?? payload.topDomains ?? []).map(normalizeDomain),
    activeClients: (payload.clients ?? payload.activeClients ?? []).map(
      normalizeClient,
    ),
  }
}

export async function getDashboardMetrics(signal) {
  try {
    const response = await fetch(appConfig.metricsEndpoint, { signal })

    if (!response.ok) {
      throw new Error(`Unexpected status code: ${response.status}`)
    }

    const payload = await response.json()
    return normalizeMetricsResponse(payload)
  } catch {
    return getMockDashboardMetrics()
  }
}

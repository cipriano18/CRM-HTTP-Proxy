import { createCsvFilename, downloadCsv } from '@/lib/csv.js'

function createBlankReportRow() {
  return {
    section: '',
    group: '',
    item: '',
    value: '',
    notes: '',
  }
}

function toPercent(value) {
  return `${value.toFixed(1)}%`
}

export function exportDashboardCsv({
  activeClients,
  domains,
  lastUpdated,
  metrics,
}) {
  const topDomains = domains.slice(0, 5)
  const topClients = activeClients.slice(0, 5)
  const topDomain = topDomains[0]
  const fifthDomain = topDomains[4]
  const totalModeratedRequests =
    metrics.allowedRequests + metrics.blockedRequests || 1
  const allowedPercentage =
    (metrics.allowedRequests / totalModeratedRequests) * 100
  const blockedPercentage =
    (metrics.blockedRequests / totalModeratedRequests) * 100
  const topFiveRequests = topDomains.reduce(
    (sum, domain) => sum + domain.requests,
    0,
  )
  const leadDomainShare = topFiveRequests
    ? (topDomain.requests / topFiveRequests) * 100
    : 0

  const rows = [
    {
      section: 'report',
      group: 'metadata',
      item: 'title',
      value: 'Proxy HTTP Monitoring Dashboard',
      notes: '',
    },
    {
      section: 'report',
      group: 'metadata',
      item: 'generated_at',
      value: new Date().toISOString(),
      notes: 'Fecha y hora de exportación',
    },
    {
      section: 'report',
      group: 'metadata',
      item: 'last_dashboard_update',
      value: lastUpdated ? lastUpdated.toISOString() : 'not_available',
      notes: 'Última actualización visible en el dashboard',
    },
    {
      section: 'report',
      group: 'metadata',
      item: 'scope',
      value: 'dashboard_overview',
      notes: 'Incluye métricas, top dominios, insights, seguridad y top clientes',
    },
    createBlankReportRow(),
    {
      section: 'dashboard',
      group: 'summary_metrics',
      item: 'total_requests',
      value: metrics.totalRequests,
      notes: 'Solicitudes totales procesadas por el proxy',
    },
    {
      section: 'dashboard',
      group: 'summary_metrics',
      item: 'allowed_requests',
      value: metrics.allowedRequests,
      notes: 'Solicitudes permitidas',
    },
    {
      section: 'dashboard',
      group: 'summary_metrics',
      item: 'blocked_requests',
      value: metrics.blockedRequests,
      notes: 'Solicitudes bloqueadas',
    },
    {
      section: 'dashboard',
      group: 'summary_metrics',
      item: 'transferred_megabytes',
      value: metrics.transferredMegabytes,
      notes: 'Volumen total transferido en MB',
    },
    createBlankReportRow(),
    ...topDomains.map((domain, index) => ({
      section: 'dashboard',
      group: 'top_5_domains',
      item: `${index + 1}. ${domain.domain}`,
      value: domain.requests,
      notes: 'Dominio más visitado dentro del ranking principal',
    })),
    createBlankReportRow(),
    {
      section: 'dashboard',
      group: 'traffic_insights',
      item: 'total_domains_monitored',
      value: domains.length,
      notes: 'Cantidad total de dominios observados',
    },
    {
      section: 'dashboard',
      group: 'traffic_insights',
      item: 'leading_domain',
      value: topDomain?.domain ?? 'not_available',
      notes: 'Dominio con más solicitudes',
    },
    {
      section: 'dashboard',
      group: 'traffic_insights',
      item: 'leading_domain_requests',
      value: topDomain?.requests ?? 0,
      notes: 'Requests del dominio líder',
    },
    {
      section: 'dashboard',
      group: 'traffic_insights',
      item: 'leading_domain_share_in_top_5',
      value: toPercent(leadDomainShare),
      notes: 'Participación del dominio líder dentro del Top 5',
    },
    {
      section: 'dashboard',
      group: 'traffic_insights',
      item: 'top_5_total_requests',
      value: topFiveRequests,
      notes: 'Suma de requests del Top 5',
    },
    {
      section: 'dashboard',
      group: 'traffic_insights',
      item: 'top_5_cutoff_domain',
      value: fifthDomain?.domain ?? 'not_available',
      notes: 'Dominio que ocupa el quinto lugar',
    },
    {
      section: 'dashboard',
      group: 'traffic_insights',
      item: 'top_5_cutoff_requests',
      value: fifthDomain?.requests ?? 0,
      notes: 'Requests necesarios para entrar al Top 5',
    },
    createBlankReportRow(),
    {
      section: 'dashboard',
      group: 'request_status',
      item: 'allowed_requests',
      value: metrics.allowedRequests,
      notes: toPercent(allowedPercentage),
    },
    {
      section: 'dashboard',
      group: 'request_status',
      item: 'blocked_requests',
      value: metrics.blockedRequests,
      notes: toPercent(blockedPercentage),
    },
    createBlankReportRow(),
    ...topClients.map((client, index) => ({
      section: 'dashboard',
      group: 'top_active_clients',
      item: `${index + 1}. ${client.ip}`,
      value: client.requests,
      notes: 'IP con más actividad reciente',
    })),
    createBlankReportRow(),
    ...domains.map((domain, index) => ({
      section: 'appendix',
      group: 'all_domains',
      item: `${index + 1}. ${domain.domain}`,
      value: domain.requests,
      notes: 'Listado completo de dominios',
    })),
    createBlankReportRow(),
    ...activeClients.map((client, index) => ({
      section: 'appendix',
      group: 'all_active_clients',
      item: `${index + 1}. ${client.ip}`,
      value: client.requests,
      notes: 'Listado completo de clientes activos',
    })),
  ]

  downloadCsv({
    columns: ['section', 'group', 'item', 'value', 'notes'],
    filename: createCsvFilename('dashboard_report'),
    rows,
  })
}

export function exportDomainsCsv(domains) {
  downloadCsv({
    columns: ['rank', 'domain', 'requests'],
    filename: createCsvFilename('domains_report'),
    rows: domains.map((domain, index) => ({
      rank: index + 1,
      domain: domain.domain,
      requests: domain.requests,
    })),
  })
}

export function exportActiveClientsCsv(activeClients) {
  downloadCsv({
    columns: ['rank', 'ip', 'requests'],
    filename: createCsvFilename('active_clients_report'),
    rows: activeClients.map((client, index) => ({
      rank: index + 1,
      ip: client.ip,
      requests: client.requests,
    })),
  })
}

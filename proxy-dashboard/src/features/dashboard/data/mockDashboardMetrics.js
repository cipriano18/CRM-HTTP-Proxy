const baseMetrics = {
  totalRequests: 1250,
  transferredMegabytes: 235.5,
  allowedRequests: 1062,
  blockedRequests: 188,
  domains: [
    { domain: 'google.com', requests: 520 },
    { domain: 'youtube.com', requests: 300 },
    { domain: 'wikipedia.org', requests: 120 },
    { domain: 'github.com', requests: 115 },
    { domain: 'openai.com', requests: 95 },
  ],
  activeClients: [
    { ip: '192.168.0.5', requests: 340 },
    { ip: '192.168.0.8', requests: 120 },
    { ip: '192.168.0.12', requests: 98 },
    { ip: '192.168.0.16', requests: 72 },
    { ip: '192.168.0.21', requests: 53 },
    { ip: '192.168.0.22', requests: 49 },
    { ip: '192.168.0.24', requests: 46 },
    { ip: '192.168.0.28', requests: 41 },
    { ip: '192.168.0.31', requests: 38 },
    { ip: '192.168.0.33', requests: 35 },
    { ip: '192.168.0.37', requests: 31 },
    { ip: '192.168.0.40', requests: 28 },
    { ip: '192.168.0.43', requests: 25 },
    { ip: '192.168.0.47', requests: 23 },
    { ip: '192.168.0.51', requests: 20 },
    { ip: '192.168.0.58', requests: 18 },
    { ip: '192.168.0.61', requests: 16 },
    { ip: '192.168.0.63', requests: 14 },
    { ip: '192.168.0.71', requests: 12 },
    { ip: '192.168.0.75', requests: 10 },
  ],
}

let snapshot = structuredClone(baseMetrics)

function bumpValue(value, minIncrease, maxIncrease) {
  const delta = Math.random() * (maxIncrease - minIncrease) + minIncrease
  return value + delta
}

function nextDomainMetrics(domains) {
  return domains.map((item, index) => ({
    ...item,
    requests: Math.round(bumpValue(item.requests, 2, 16 - index)),
  }))
}

function nextClientMetrics(clients) {
  return clients
    .map((client, index) => ({
      ...client,
      requests: Math.round(bumpValue(client.requests, 1, Math.max(2, 12 - index))),
    }))
    .sort((left, right) => right.requests - left.requests)
}

export function getMockDashboardMetrics() {
  snapshot = {
    ...snapshot,
    totalRequests: Math.round(bumpValue(snapshot.totalRequests, 8, 28)),
    transferredMegabytes: Number(
      bumpValue(snapshot.transferredMegabytes, 1.4, 6.8).toFixed(1),
    ),
    allowedRequests: Math.round(bumpValue(snapshot.allowedRequests, 6, 24)),
    blockedRequests: Math.round(bumpValue(snapshot.blockedRequests, 1, 6)),
    domains: nextDomainMetrics(snapshot.domains),
    activeClients: nextClientMetrics(snapshot.activeClients),
  }

  return structuredClone(snapshot)
}

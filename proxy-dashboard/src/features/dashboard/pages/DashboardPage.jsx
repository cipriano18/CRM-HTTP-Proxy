import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import '@/lib/chartSetup.js'
import { AlertTriangle, CheckCircle2 } from 'lucide-react'
import PageShell from '@/components/layout/PageShell.jsx'
import ActiveClientsTable from '@/features/dashboard/components/ActiveClientsTable.jsx'
import DashboardFooter from '@/features/dashboard/components/DashboardFooter.jsx'
import DashboardHeader from '@/features/dashboard/components/DashboardHeader.jsx'
import MetricsOverview from '@/features/dashboard/components/MetricsOverview.jsx'
import RequestsStatusChart from '@/features/dashboard/components/RequestsStatusChart.jsx'
import TopDomainsChart from '@/features/dashboard/components/TopDomainsChart.jsx'
import TopDomainsTable from '@/features/dashboard/components/TopDomainsTable.jsx'
import { useDashboardMetrics } from '@/features/dashboard/hooks/useDashboardMetrics.js'

function RefreshToast({ isVisible }) {
  return (
    <div
      className={`pointer-events-none fixed right-5 bottom-5 z-50 transition-all duration-300 ${
        isVisible
          ? 'translate-y-0 opacity-100'
          : 'translate-y-4 opacity-0'
      }`}
    >
      <div className="flex items-center gap-3 rounded-2xl border border-emerald-400/25 bg-slate-950/92 px-4 py-3 text-sm text-slate-100 shadow-xl shadow-black/35">
        <CheckCircle2 className="h-5 w-5 text-emerald-400" />
        <span>Se refrescó manualmente.</span>
      </div>
    </div>
  )
}

function DashboardPage() {
  const navigate = useNavigate()
  const [showRefreshToast, setShowRefreshToast] = useState(false)
  const { error, isLoading, lastUpdated, metrics, refreshMetrics } =
    useDashboardMetrics()

  useEffect(() => {
    if (!showRefreshToast) {
      return undefined
    }

    const timeoutId = window.setTimeout(() => {
      setShowRefreshToast(false)
    }, 2400)

    return () => {
      window.clearTimeout(timeoutId)
    }
  }, [showRefreshToast])

  const handleManualRefresh = () => {
    refreshMetrics()
    setShowRefreshToast(true)
  }

  const handleOpenFullClientsPage = () => {
    navigate('/clientes-activos')
  }

  const handleOpenDomainsPage = () => {
    navigate('/dominios')
  }

  if (isLoading && !metrics) {
    return (
      <PageShell>
        <div className="panel-surface flex min-h-[60vh] items-center justify-center p-8">
          <div className="text-center">
            <div className="mx-auto h-14 w-14 animate-spin rounded-full border-4 border-cyan-300/20 border-t-cyan-300" />
            <p className="mt-5 text-sm uppercase tracking-[0.24em] text-slate-400">
              Cargando métricas del proxy...
            </p>
          </div>
        </div>
      </PageShell>
    )
  }

  const sortedClients = [...metrics.activeClients].sort(
    (left, right) => right.requests - left.requests,
  )
  const sortedDomains = [...metrics.domains].sort(
    (left, right) => right.requests - left.requests,
  )
  const topDomains = sortedDomains.slice(0, 5)
  const topActiveClients = sortedClients.slice(0, 5)

  return (
    <>
      <PageShell className="pb-0">
        <DashboardHeader
          lastUpdated={lastUpdated}
          onManualRefresh={handleManualRefresh}
        />

        {error ? (
          <section
            className="panel-surface reveal-up flex items-start gap-3 border border-amber-400/20 bg-amber-400/10 p-4 text-sm text-amber-100"
            style={{ '--reveal-delay': '120ms' }}
          >
            <AlertTriangle className="mt-0.5 h-5 w-5 shrink-0" />
            <p>{error}</p>
          </section>
        ) : null}

        <div className="reveal-up" style={{ '--reveal-delay': '120ms' }}>
          <MetricsOverview metrics={metrics} />
        </div>

        <div className="panel-grid reveal-up" style={{ '--reveal-delay': '220ms' }}>
          <TopDomainsChart domains={topDomains} />
          <TopDomainsTable
            domains={topDomains}
            totalDomains={sortedDomains.length}
            onOpenFullPage={handleOpenDomainsPage}
          />
        </div>

        <div className="panel-grid reveal-up" style={{ '--reveal-delay': '320ms' }}>
          <RequestsStatusChart
            allowedRequests={metrics.allowedRequests}
            blockedRequests={metrics.blockedRequests}
          />
          <ActiveClientsTable
            activeClients={topActiveClients}
            onOpenFullPage={handleOpenFullClientsPage}
          />
        </div>

        <RefreshToast isVisible={showRefreshToast} />
      </PageShell>

      <DashboardFooter />
    </>
  )
}

export default DashboardPage

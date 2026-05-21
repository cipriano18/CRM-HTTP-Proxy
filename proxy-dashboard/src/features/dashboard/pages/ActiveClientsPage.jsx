import { useMemo } from 'react'
import { ArrowLeft, Users } from 'lucide-react'
import { Link } from 'react-router-dom'
import PageShell from '@/components/layout/PageShell.jsx'
import ActiveClientsDirectory from '@/features/dashboard/components/ActiveClientsDirectory.jsx'
import { exportActiveClientsCsv } from '@/features/dashboard/lib/reportExports.js'
import { useDashboardMetrics } from '@/features/dashboard/hooks/useDashboardMetrics.js'

function ActiveClientsPage() {
  const { isLoading, metrics } = useDashboardMetrics()

  const sortedClients = useMemo(() => {
    if (!metrics) {
      return []
    }

    return [...metrics.activeClients].sort(
      (left, right) => right.requests - left.requests,
    )
  }, [metrics])

  if (isLoading && !metrics) {
    return (
      <PageShell>
        <div className="panel-surface flex min-h-[60vh] items-center justify-center p-8">
          <div className="text-center">
            <div className="mx-auto h-14 w-14 animate-spin rounded-full border-4 border-cyan-300/20 border-t-cyan-300" />
            <p className="mt-5 text-sm uppercase tracking-[0.24em] text-slate-400">
              Cargando clientes activos...
            </p>
          </div>
        </div>
      </PageShell>
    )
  }

  return (
    <PageShell>
      <section
        className="panel-surface reveal-scale overflow-hidden"
        style={{ '--reveal-delay': '40ms' }}
      >
        <div className="flex flex-col gap-5 px-6 py-6 lg:flex-row lg:items-center lg:justify-between lg:px-8">
          <div>
            <div className="inline-flex items-center gap-2 rounded-full border border-cyan-400/25 bg-cyan-400/10 px-3 py-1 text-xs font-semibold uppercase tracking-[0.26em] text-cyan-200">
              <Users className="h-3.5 w-3.5" />
              Clientes Activos
            </div>
            <h1 className="mt-4 font-display text-4xl font-bold tracking-tight text-white md:text-5xl">
              Directorio completo de clientes
            </h1>
            <p className="mt-3 max-w-2xl text-sm leading-6 text-slate-300 md:text-base">
              Consulta todas las IPs registradas por el proxy, filtradas y
              ordenadas por volumen de requests.
            </p>
          </div>

          <Link
            to="/"
            className="inline-flex items-center justify-center gap-2 rounded-2xl border border-white/10 bg-white/5 px-4 py-3 font-semibold text-slate-100 shadow-lg shadow-black/10 transition duration-300 ease-out hover:-translate-y-0.5 hover:scale-[1.03] hover:border-white/20 hover:bg-white/10 hover:shadow-xl hover:shadow-black/20"
          >
            <ArrowLeft className="h-4 w-4" />
            Volver al dashboard
          </Link>
        </div>
      </section>

      <div className="reveal-up" style={{ '--reveal-delay': '160ms' }}>
        <ActiveClientsDirectory
          activeClients={sortedClients}
          onExportCsv={exportActiveClientsCsv}
        />
      </div>
    </PageShell>
  )
}

export default ActiveClientsPage

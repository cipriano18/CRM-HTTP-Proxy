import { ArrowRight, Flag, Globe, Layers3, Trophy } from 'lucide-react'
import Panel from '@/components/ui/Panel.jsx'
import { formatPercentage, formatRequestCount } from '@/lib/formatters.js'

function TopDomainsTable({ domains, totalDomains, onOpenFullPage }) {
  const topDomain = domains[0]
  const fifthDomain = domains[4]
  const topFiveRequests = domains.reduce((sum, item) => sum + item.requests, 0)
  const leadDomainShare = topFiveRequests
    ? (topDomain.requests / topFiveRequests) * 100
    : 0

  return (
    <Panel className="p-6">
      <div className="flex items-start justify-between gap-4">
        <div className="min-w-0">
          <div className="flex flex-wrap items-center gap-3">
            <p className="text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">
              Resumen por dominios
            </p>
            <span className="rounded-full border border-white/10 px-3 py-1 text-xs text-slate-300">
              {totalDomains} dominios
            </span>
          </div>
          <h2 className="mt-2 font-display text-2xl font-bold text-white">
            Insights del tráfico web
          </h2>
          <p className="mt-2 text-sm leading-6 text-slate-400">
            Resumen rápido del comportamiento del Top 5.
          </p>
        </div>

        <button
          type="button"
          onClick={onOpenFullPage}
          className="inline-flex h-11 w-11 shrink-0 items-center justify-center rounded-full border border-cyan-400/20 bg-cyan-400/10 text-cyan-200 shadow-lg shadow-cyan-500/10 transition duration-300 ease-out hover:-translate-y-0.5 hover:scale-105 hover:border-cyan-300/50 hover:bg-cyan-300/25 hover:text-white hover:shadow-xl hover:shadow-cyan-400/20"
          aria-label="Ir a la página completa de dominios"
        >
          <ArrowRight className="h-5 w-5" />
        </button>
      </div>

      <div className="mt-6 grid gap-4 sm:grid-cols-2">
        <article className="rounded-2xl border border-white/10 bg-white/4 p-4">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-2xl bg-cyan-400/15 text-cyan-200">
              <Trophy className="h-5 w-5" />
            </div>
            <div>
              <p className="text-xs font-semibold uppercase tracking-[0.22em] text-slate-500">
                Dominio líder
              </p>
              <p className="mt-1 font-medium text-white">
                {topDomain?.domain ?? 'Sin datos'}
              </p>
            </div>
          </div>
          <p className="mt-4 text-sm text-slate-300">
            {topDomain
              ? `${formatRequestCount(topDomain.requests)} requests observados.`
              : 'No hay tráfico registrado todavía.'}
          </p>
        </article>

        <article className="rounded-2xl border border-white/10 bg-white/4 p-4">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-2xl bg-emerald-400/15 text-emerald-200">
              <Globe className="h-5 w-5" />
            </div>
            <div>
              <p className="text-xs font-semibold uppercase tracking-[0.22em] text-slate-500">
                Peso del líder
              </p>
              <p className="mt-1 font-medium text-white">
                {formatPercentage(leadDomainShare)}
              </p>
            </div>
          </div>
          <p className="mt-4 text-sm text-slate-300">
            Participación del dominio principal dentro del Top 5 mostrado.
          </p>
        </article>

        <article className="rounded-2xl border border-white/10 bg-white/4 p-4">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-2xl bg-amber-400/15 text-amber-200">
              <Layers3 className="h-5 w-5" />
            </div>
            <div>
              <p className="text-xs font-semibold uppercase tracking-[0.22em] text-slate-500">
                Requests del Top 5
              </p>
              <p className="mt-1 font-medium text-white">
                {formatRequestCount(topFiveRequests)}
              </p>
            </div>
          </div>
          <p className="mt-4 text-sm text-slate-300">
            Suma total de solicitudes entre los cinco dominios más visitados.
          </p>
        </article>

        <article className="rounded-2xl border border-white/10 bg-white/4 p-4">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-2xl bg-fuchsia-400/15 text-fuchsia-200">
              <Flag className="h-5 w-5" />
            </div>
            <div>
              <p className="text-xs font-semibold uppercase tracking-[0.22em] text-slate-500">
                Cierre del Top 5
              </p>
              <p className="mt-1 font-medium text-white">
                {fifthDomain?.domain ?? 'Sin datos'}
              </p>
            </div>
          </div>
          <p className="mt-4 text-sm text-slate-300">
            {fifthDomain
              ? `${formatRequestCount(fifthDomain.requests)} requests para entrar al ranking principal.`
              : 'Todavía no hay suficientes dominios para completar el Top 5.'}
          </p>
        </article>
      </div>
    </Panel>
  )
}

export default TopDomainsTable

import { ArrowRight } from 'lucide-react'
import Panel from '@/components/ui/Panel.jsx'
import { formatRequestCount } from '@/lib/formatters.js'

function ActiveClientsTable({ activeClients, onOpenFullPage }) {
  return (
    <Panel className="p-6">
      <div className="flex items-start justify-between gap-4">
        <div className="min-w-0">
          <div className="flex flex-wrap items-center gap-3">
            <p className="text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">
              Clientes activos
            </p>
            <span className="rounded-full border border-white/10 px-3 py-1 text-xs text-slate-300">
              {activeClients.length} clientes
            </span>
          </div>
          <h2 className="mt-2 font-display text-2xl font-bold text-white">
            IPs con más actividad reciente
          </h2>
        </div>

        <button
          type="button"
          onClick={onOpenFullPage}
          className="inline-flex h-11 w-11 shrink-0 items-center justify-center rounded-full border border-cyan-400/20 bg-cyan-400/10 text-cyan-200 shadow-lg shadow-cyan-500/10 transition duration-300 ease-out hover:-translate-y-0.5 hover:scale-105 hover:border-cyan-300/50 hover:bg-cyan-300/25 hover:text-white hover:shadow-xl hover:shadow-cyan-400/20"
          aria-label="Ir a la página completa de clientes activos"
        >
          <ArrowRight className="h-5 w-5" />
        </button>
      </div>

      <div className="mt-6 overflow-hidden rounded-2xl border border-white/10">
        <table className="min-w-full divide-y divide-white/10">
          <thead className="bg-white/5">
            <tr>
              <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">
                IP del cliente
              </th>
              <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">
                Requests
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-white/10 bg-slate-950/15">
            {activeClients.map((client) => (
              <tr key={client.ip}>
                <td className="px-4 py-3 font-mono text-sm text-cyan-100">
                  {client.ip}
                </td>
                <td className="px-4 py-3 text-sm text-slate-200">
                  {formatRequestCount(client.requests)}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </Panel>
  )
}

export default ActiveClientsTable

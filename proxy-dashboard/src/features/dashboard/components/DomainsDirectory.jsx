import { useDeferredValue, useState } from 'react'
import { Search } from 'lucide-react'
import Panel from '@/components/ui/Panel.jsx'
import { formatRequestCount } from '@/lib/formatters.js'

const PAGE_SIZE = 8

function DomainsDirectory({ domains }) {
  const [searchTerm, setSearchTerm] = useState('')
  const [currentPage, setCurrentPage] = useState(1)
  const deferredSearchTerm = useDeferredValue(searchTerm)

  const normalizedSearchTerm = deferredSearchTerm.trim().toLowerCase()
  const filteredDomains = domains.filter((item) =>
    item.domain.toLowerCase().includes(normalizedSearchTerm),
  )

  const totalPages = Math.max(1, Math.ceil(filteredDomains.length / PAGE_SIZE))
  const safeCurrentPage = Math.min(currentPage, totalPages)
  const pageStart = (safeCurrentPage - 1) * PAGE_SIZE
  const paginatedDomains = filteredDomains.slice(pageStart, pageStart + PAGE_SIZE)

  return (
    <section id="domains-directory">
      <Panel className="p-6 lg:p-7">
        <div className="flex flex-col gap-5 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <p className="text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">
              Tabla completa
            </p>
            <h2 className="mt-2 font-display text-3xl font-bold text-white">
              Dominios monitoreados por el proxy
            </h2>
            <p className="mt-2 text-sm text-slate-400">
              Lista completa ordenada por cantidad de solicitudes, de mayor a
              menor.
            </p>
          </div>

          <label className="flex w-full max-w-md items-center gap-3 rounded-2xl border border-white/10 bg-white/5 px-4 py-3">
            <Search className="h-4 w-4 text-slate-400" />
            <input
              type="text"
              value={searchTerm}
              onChange={(event) => {
                setSearchTerm(event.target.value)
                setCurrentPage(1)
              }}
              placeholder="Filtrar por dominio"
              className="w-full bg-transparent text-sm text-slate-100 outline-none placeholder:text-slate-500"
            />
          </label>
        </div>

        <div className="mt-6 overflow-hidden rounded-2xl border border-white/10">
          <table className="min-w-full divide-y divide-white/10">
            <thead className="bg-white/5">
              <tr>
                <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">
                  Posición
                </th>
                <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">
                  Dominio
                </th>
                <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">
                  Requests
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-white/10 bg-slate-950/15">
              {paginatedDomains.length > 0 ? (
                paginatedDomains.map((item, index) => (
                  <tr key={item.domain}>
                    <td className="px-4 py-3 text-sm text-slate-400">
                      {pageStart + index + 1}
                    </td>
                    <td className="px-4 py-3 text-sm text-cyan-100">
                      {item.domain}
                    </td>
                    <td className="px-4 py-3 text-sm text-slate-200">
                      {formatRequestCount(item.requests)}
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td
                    colSpan="3"
                    className="px-4 py-8 text-center text-sm text-slate-400"
                  >
                    No se encontraron dominios con ese filtro.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        <div className="mt-5 flex flex-col gap-4 border-t border-white/10 pt-5 sm:flex-row sm:items-center sm:justify-between">
          <p className="text-sm text-slate-400">
            Mostrando {paginatedDomains.length} de {filteredDomains.length}{' '}
            dominios
          </p>

          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => setCurrentPage((page) => Math.max(1, page - 1))}
              disabled={safeCurrentPage === 1}
              className="rounded-xl border border-white/10 px-3 py-2 text-sm text-slate-200 transition hover:bg-white/5 disabled:cursor-not-allowed disabled:opacity-40"
            >
              Anterior
            </button>
            <span className="rounded-xl border border-white/10 px-3 py-2 text-sm text-slate-300">
              Página {safeCurrentPage} de {totalPages}
            </span>
            <button
              type="button"
              onClick={() =>
                setCurrentPage((page) => Math.min(totalPages, page + 1))
              }
              disabled={safeCurrentPage === totalPages}
              className="rounded-xl border border-white/10 px-3 py-2 text-sm text-slate-200 transition hover:bg-white/5 disabled:cursor-not-allowed disabled:opacity-40"
            >
              Siguiente
            </button>
          </div>
        </div>
      </Panel>
    </section>
  )
}

export default DomainsDirectory

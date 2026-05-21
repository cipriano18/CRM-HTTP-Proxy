function DashboardFooter() {
  return (
    <footer
      className="reveal-fade mt-6 w-full overflow-hidden border-y border-white/10 bg-slate-950/30"
      style={{ '--reveal-delay': '420ms' }}
    >
      <div className="grid gap-px bg-white/10 lg:grid-cols-[1.5fr_1fr_1fr]">
        <section className="bg-slate-950/25 px-5 py-5 lg:px-8">
          <p className="text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">
            Proyecto
          </p>
          <h2 className="mt-2 font-display text-lg font-semibold text-white">
            Proxy HTTP Monitoring Dashboard
          </h2>
          <p className="mt-2 text-sm leading-6 text-slate-300">
            Universidad Nacional - Comunicación y Redes de Computadores
          </p>
          <p className="mt-1 text-sm text-cyan-200">I Ciclo 2026</p>
        </section>

        <section className="bg-slate-950/25 px-5 py-5">
          <p className="text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">
            Frontend Developer
          </p>
          <p className="mt-2 text-sm font-medium text-white">
            Makin Artavia Zúñiga
          </p>
        </section>

        <section className="bg-slate-950/25 px-5 py-5">
          <p className="text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">
            Backend Developers
          </p>
          <div className="mt-2 space-y-1.5">
            <p className="text-sm font-medium text-white">
              Reyner Rojas Gutiérrez
            </p>
            <p className="text-sm font-medium text-white">
              Cipriano Rivera Escobar
            </p>
          </div>
        </section>
      </div>
    </footer>
  )
}

export default DashboardFooter

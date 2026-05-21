function StatCard({ accentClass, helper, icon: Icon, title, value }) {
  return (
    <article className="panel-surface relative overflow-hidden p-5">
      <div
        className={`absolute inset-x-0 top-0 h-1 rounded-full ${accentClass}`.trim()}
      />
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-xs font-semibold uppercase tracking-[0.28em] text-slate-400">
            {title}
          </p>
          <p className="mt-4 font-display text-4xl font-bold tracking-tight text-white">
            {value}
          </p>
          <p className="mt-2 text-sm text-slate-400">{helper}</p>
        </div>
        <div className="rounded-2xl border border-white/10 bg-white/8 p-3 text-slate-100">
          <Icon className="h-5 w-5" />
        </div>
      </div>
    </article>
  )
}

export default StatCard

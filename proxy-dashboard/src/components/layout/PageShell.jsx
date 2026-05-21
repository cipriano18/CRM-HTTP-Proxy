function PageShell({ children, className = '' }) {
  return (
    <div
      className={`min-h-screen bg-transparent px-4 py-6 text-slate-100 sm:px-6 lg:px-8 ${className}`.trim()}
    >
      <div className="mx-auto flex w-full max-w-7xl flex-col gap-6">
        {children}
      </div>
    </div>
  )
}

export default PageShell

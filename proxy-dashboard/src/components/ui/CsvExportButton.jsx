import { Download } from 'lucide-react'

function CsvExportButton({ className = '', label = 'Exportar CSV', onClick }) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={`inline-flex items-center justify-center gap-2 rounded-2xl border border-white/10 bg-white/5 px-4 py-3 text-sm font-semibold text-slate-100 transition duration-300 ease-out hover:border-white/20 hover:bg-white/10 ${className}`.trim()}
    >
      <Download className="h-4 w-4" />
      {label}
    </button>
  )
}

export default CsvExportButton

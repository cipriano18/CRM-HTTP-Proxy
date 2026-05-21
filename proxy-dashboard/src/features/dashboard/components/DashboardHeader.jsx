import { useEffect, useRef } from 'react'
import {
  Activity,
  Clock3,
  LayoutDashboard,
  RefreshCcw,
  Server,
} from 'lucide-react'
import refreshSound from '@/features/dashboard/assets/refresh.mp3'
import { appConfig } from '@/config/appConfig.js'
import { formatTime } from '@/lib/formatters.js'

function DashboardHeader({ lastUpdated, onManualRefresh }) {
  const refreshAudioRef = useRef(null)

  useEffect(() => {
    const audio = new Audio(refreshSound)
    audio.preload = 'auto'
    refreshAudioRef.current = audio

    return () => {
      audio.pause()
      audio.currentTime = 0
      refreshAudioRef.current = null
    }
  }, [])

  const handleRefreshClick = () => {
    if (refreshAudioRef.current) {
      refreshAudioRef.current.currentTime = 0
      refreshAudioRef.current.play().catch(() => {})
    }

    onManualRefresh()
  }

  return (
    <header
      className="panel-surface reveal-scale overflow-hidden"
      style={{ '--reveal-delay': '40ms' }}
    >
      <div className="flex flex-col gap-6 px-6 py-6 lg:flex-row lg:items-center lg:justify-between lg:px-8">
        <div className="max-w-2xl">
          <div className="inline-flex items-center gap-2 rounded-full border border-cyan-400/25 bg-cyan-400/10 px-3 py-1 text-xs font-semibold uppercase tracking-[0.26em] text-cyan-200">
            <Activity className="h-3.5 w-3.5" />
            Monitor de Proxy
          </div>
          <h1 className="mt-4 font-display text-4xl font-bold tracking-tight text-white md:text-5xl">
            Panel de monitoreo del proxy
          </h1>
          <p className="mt-3 max-w-xl text-sm leading-6 text-slate-300 md:text-base">
            Visualiza el estado del tráfico, el volumen transferido y la
            actividad de los clientes desde una interfaz separada del proxy
            principal.
          </p>
        </div>

        <div className="flex items-stretch">
          <div className="flex overflow-hidden rounded-[1.7rem] border border-white/10 bg-slate-950/30 shadow-lg shadow-black/15">
            <div className="flex min-w-[12.5rem] items-center gap-3 bg-white/6 px-4 py-3">
              <div className="flex h-10 w-10 items-center justify-center rounded-2xl bg-white/8 text-cyan-200">
                <Clock3 className="h-4 w-4" />
              </div>
              <div>
                <p className="text-[0.68rem] font-semibold uppercase tracking-[0.22em] text-slate-500">
                  Última actualización
                </p>
                <p className="mt-1 text-sm font-medium text-slate-100">
                  {lastUpdated ? formatTime(lastUpdated) : 'Cargando...'}
                </p>
              </div>
            </div>

            <button
              type="button"
              onClick={handleRefreshClick}
              className="group inline-flex items-center justify-center gap-2 bg-cyan-400 px-5 py-3 font-semibold text-slate-950 transition duration-300 ease-out hover:bg-cyan-200"
            >
              <RefreshCcw className="h-4 w-4 transition-transform duration-500 ease-out group-hover:-rotate-360" />
              Actualizar
            </button>
          </div>
        </div>
      </div>

      <div className="grid gap-px border-t border-white/10 bg-white/10 md:grid-cols-3">
        <div className="bg-slate-950/25 px-6 py-4">
          <p className="text-xs uppercase tracking-[0.24em] text-slate-500">
            Servicio del proxy
          </p>
          <div className="mt-2 flex items-center gap-2 text-sm font-medium text-slate-100">
            <Server className="h-4 w-4 text-cyan-300" />
            Puerto 8080
          </div>
        </div>
        <div className="bg-slate-950/25 px-6 py-4">
          <p className="text-xs uppercase tracking-[0.24em] text-slate-500">
            Panel de control
          </p>
          <div className="mt-2 flex items-center gap-2 text-sm font-medium text-slate-100">
            <LayoutDashboard className="h-4 w-4 text-cyan-300" />
            Puerto {appConfig.dashboardPort}
          </div>
        </div>
        <div className="bg-slate-950/25 px-6 py-4">
          <p className="text-xs uppercase tracking-[0.24em] text-slate-500">
            Actualización
          </p>
          <div className="mt-2 flex items-center gap-2 text-sm font-medium text-slate-100">
            <Clock3 className="h-4 w-4 text-emerald-400" />
            Cada 3 segundos
          </div>
        </div>
      </div>
    </header>
  )
}

export default DashboardHeader

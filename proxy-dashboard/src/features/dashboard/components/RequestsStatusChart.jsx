import { Doughnut } from 'react-chartjs-2'
import Panel from '@/components/ui/Panel.jsx'
import { formatPercentage, formatRequestCount } from '@/lib/formatters.js'

function RequestsStatusChart({ allowedRequests, blockedRequests }) {
  const totalRequests = allowedRequests + blockedRequests || 1
  const allowedPercentage = (allowedRequests / totalRequests) * 100
  const blockedPercentage = (blockedRequests / totalRequests) * 100

  const chartData = {
    labels: ['Permitidas', 'Bloqueadas'],
    datasets: [
      {
        data: [allowedRequests, blockedRequests],
        backgroundColor: ['#35b7cb', '#f59e0b'],
        borderWidth: 0,
      },
    ],
  }

  const chartOptions = {
    animation: false,
    cutout: '72%',
    maintainAspectRatio: false,
    plugins: {
      legend: {
        labels: {
          color: '#d7e4eb',
        },
      },
    },
  }

  return (
    <Panel className="p-6">
      <p className="text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">
        Seguridad y filtrado
      </p>
      <h2 className="mt-2 font-display text-2xl font-bold text-white">
        Solicitudes bloqueadas vs permitidas
      </h2>

      <div className="mt-6 grid gap-6 lg:grid-cols-[1.1fr_0.9fr]">
        <div className="h-64">
          <Doughnut data={chartData} options={chartOptions} />
        </div>

        <div className="flex flex-col justify-center gap-4">
          <article className="rounded-2xl border border-cyan-400/20 bg-cyan-400/10 p-4">
            <p className="text-xs font-semibold uppercase tracking-[0.22em] text-cyan-200">
              Permitidas
            </p>
            <p className="mt-2 font-display text-3xl font-bold text-white">
              {formatPercentage(allowedPercentage)}
            </p>
            <p className="mt-2 text-sm text-slate-300">
              {formatRequestCount(allowedRequests)} solicitudes aprobadas.
            </p>
          </article>

          <article className="rounded-2xl border border-amber-400/20 bg-amber-400/10 p-4">
            <p className="text-xs font-semibold uppercase tracking-[0.22em] text-amber-200">
              Bloqueadas
            </p>
            <p className="mt-2 font-display text-3xl font-bold text-white">
              {formatPercentage(blockedPercentage)}
            </p>
            <p className="mt-2 text-sm text-slate-300">
              {formatRequestCount(blockedRequests)} solicitudes rechazadas.
            </p>
          </article>
        </div>
      </div>
    </Panel>
  )
}

export default RequestsStatusChart

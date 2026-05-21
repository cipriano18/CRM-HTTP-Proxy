import { Bar } from 'react-chartjs-2'
import Panel from '@/components/ui/Panel.jsx'

function TopDomainsChart({ domains }) {
  const chartData = {
    labels: domains.map((item) => item.domain),
    datasets: [
      {
        borderRadius: 10,
        data: domains.map((item) => item.requests),
        backgroundColor: [
          '#35b7cb',
          '#20c997',
          '#60a5fa',
          '#fbbf24',
          '#fb923c',
        ],
      },
    ],
  }

  const chartOptions = {
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
    },
    scales: {
      x: {
        grid: { display: false },
        ticks: { color: '#c9d7df' },
      },
      y: {
        beginAtZero: true,
        border: { display: false },
        grid: { color: 'rgba(255,255,255,0.08)' },
        ticks: { color: '#8ca3b0' },
      },
    },
  }

  return (
    <Panel className="p-6">
      <div className="flex items-center justify-between gap-4">
        <div>
          <p className="text-xs font-semibold uppercase tracking-[0.24em] text-slate-500">
            Top 5 dominios
          </p>
          <h2 className="mt-2 font-display text-2xl font-bold text-white">
            Dominios más visitados
          </h2>
        </div>
      </div>

      <div className="mt-6 h-80">
        <Bar data={chartData} options={chartOptions} />
      </div>
    </Panel>
  )
}

export default TopDomainsChart

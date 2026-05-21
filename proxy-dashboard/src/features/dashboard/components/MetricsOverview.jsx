import { Ban, Database, Globe, ShieldCheck } from 'lucide-react'
import StatCard from '@/components/ui/StatCard.jsx'
import {
  formatMegabytes,
  formatPercentage,
  formatRequestCount,
} from '@/lib/formatters.js'

function MetricsOverview({ metrics }) {
  const totalModeratedRequests =
    metrics.allowedRequests + metrics.blockedRequests || 1
  const blockedPercentage =
    (metrics.blockedRequests / totalModeratedRequests) * 100

  const cards = [
    {
      accentClass: 'bg-cyan-400',
      helper: 'Solicitudes HTTP/HTTPS procesadas por el proxy.',
      icon: Globe,
      title: 'Total requests',
      value: formatRequestCount(metrics.totalRequests),
    },
    {
      accentClass: 'bg-emerald-400',
      helper: 'Volumen transferido desde el proxy hacia los clientes.',
      icon: Database,
      title: 'Datos transferidos',
      value: formatMegabytes(metrics.transferredMegabytes),
    },
    {
      accentClass: 'bg-amber-400',
      helper: 'Tráfico aprobado según reglas de acceso actuales.',
      icon: ShieldCheck,
      title: 'Solicitudes permitidas',
      value: formatRequestCount(metrics.allowedRequests),
    },
    {
      accentClass: 'bg-orange-400',
      helper: `${formatPercentage(blockedPercentage)} del tráfico fue bloqueado.`,
      icon: Ban,
      title: 'Solicitudes bloqueadas',
      value: formatRequestCount(metrics.blockedRequests),
    },
  ]

  return (
    <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
      {cards.map((card) => (
        <StatCard key={card.title} {...card} />
      ))}
    </section>
  )
}

export default MetricsOverview

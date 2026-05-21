const compactNumberFormatter = new Intl.NumberFormat('es-CR')
const decimalFormatter = new Intl.NumberFormat('es-CR', {
  minimumFractionDigits: 1,
  maximumFractionDigits: 1,
})
const percentFormatter = new Intl.NumberFormat('es-CR', {
  minimumFractionDigits: 1,
  maximumFractionDigits: 1,
})
const timeFormatter = new Intl.DateTimeFormat('es-CR', {
  hour: '2-digit',
  minute: '2-digit',
  second: '2-digit',
})

export function formatRequestCount(value) {
  return compactNumberFormatter.format(value)
}

export function formatMegabytes(value) {
  return `${decimalFormatter.format(value)} MB`
}

export function formatPercentage(value) {
  return `${percentFormatter.format(value)}%`
}

export function formatTime(value) {
  return timeFormatter.format(value)
}

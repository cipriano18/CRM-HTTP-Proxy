function escapeCsvValue(value) {
  const normalizedValue =
    value === null || value === undefined ? '' : String(value)

  if (
    normalizedValue.includes(',') ||
    normalizedValue.includes('"') ||
    normalizedValue.includes('\n')
  ) {
    return `"${normalizedValue.replaceAll('"', '""')}"`
  }

  return normalizedValue
}

export function createCsvFilename(prefix) {
  const now = new Date()
  const timestamp = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(
    2,
    '0',
  )}-${String(now.getDate()).padStart(2, '0')}_${String(
    now.getHours(),
  ).padStart(2, '0')}-${String(now.getMinutes()).padStart(2, '0')}`

  return `${prefix}_${timestamp}.csv`
}

export function downloadCsv({ columns, filename, rows }) {
  const header = columns.join(',')
  const body = rows.map((row) =>
    columns.map((column) => escapeCsvValue(row[column])).join(','),
  )

  const csvContent = [header, ...body].join('\n')
  const blob = new Blob([csvContent], {
    type: 'text/csv;charset=utf-8;',
  })

  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.append(link)
  link.click()
  link.remove()
  URL.revokeObjectURL(url)
}

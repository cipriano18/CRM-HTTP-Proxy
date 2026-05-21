function Panel({ children, className = '' }) {
  return <section className={`panel-surface ${className}`.trim()}>{children}</section>
}

export default Panel

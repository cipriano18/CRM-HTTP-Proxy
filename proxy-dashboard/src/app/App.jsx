import { BrowserRouter, Route, Routes } from 'react-router-dom'
import ActiveClientsPage from '@/features/dashboard/pages/ActiveClientsPage.jsx'
import DashboardPage from '@/features/dashboard/pages/DashboardPage.jsx'
import DomainsPage from '@/features/dashboard/pages/DomainsPage.jsx'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<DashboardPage />} />
        <Route path="/clientes-activos" element={<ActiveClientsPage />} />
        <Route path="/dominios" element={<DomainsPage />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App

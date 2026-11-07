import Landing from "./components/landing/Landing";
import LancheroRegistro from "./components/lanchero/LancheroRegistro";
import { BrowserRouter, Navigate, Route, Routes, useOutletContext } from "react-router-dom";
import Admin from "./components/Admin/Admin";
import type { AdminOutletContext } from "./components/Admin/Admin";
import SolicitudesPendientes from "./components/Admin/SolicitudesPendientes";
import DashboardPage from "./pages/admin/DashboardPage";

function SolicitudesPanel() {
  const { token, onLogout } = useOutletContext<AdminOutletContext>();
  return <SolicitudesPendientes token={token} onLogout={onLogout} />;
}

function DashboardPanel() {
  const { token } = useOutletContext<AdminOutletContext>();
  return <DashboardPage token={token} />;
}

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Landing />} />
        <Route
          path="/registro"
          element={
            <div className="flex min-h-dvh flex-col bg-bg-fill-secondary p-4 font-inter md:p-16">
              <LancheroRegistro />
            </div>
          }
        />
        <Route path="/admin" element={<Admin />}>
          <Route index element={<Navigate to="/panel" replace />} />
        </Route>
        <Route path="/admin/*" element={<Navigate to="/admin" replace />} />
        <Route path="/panel" element={<Admin />}>
          <Route index element={<DashboardPanel />} />
          <Route path="buscador" element={<p className="text-text-primary">Buscador (pendiente)</p>} />
          <Route path="viajes" element={<p className="text-text-primary">Viajes (pendiente)</p>} />
          <Route path="solicitudes" element={<SolicitudesPanel />} />
          <Route path="solicitudes/:id" element={<SolicitudesPanel />} />
          <Route path="lancheros" element={<p className="text-text-primary">Lancheros (pendiente)</p>} />
          <Route path="*" element={<Navigate to="/panel/solicitudes" replace />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;

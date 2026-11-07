import Landing from "./components/landing/Landing";
import LancheroRegistro from "./components/lanchero/LancheroRegistro";
import { BrowserRouter, Outlet, Routes, Route } from 'react-router-dom';
import Admin from "./components/Admin/Admin";
import AdminLayout from "./components/Admin/AdminLayout";

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
        <Route path="/admin" element={<Admin />} />

        <Route path="/panel" element={<AdminLayout onCerrarSesion={() => console.log("Cerrar sesión")}>
              <Outlet />
            </AdminLayout>
          }
        >
          <Route index element={<p className="text-text-primary">Resumen general (pendiente)</p>} />
          <Route path="buscador" element={<p className="text-text-primary">Buscador (pendiente)</p>} />
          <Route path="viajes" element={<p className="text-text-primary">Viajes (pendiente)</p>} />
          <Route path="solicitudes" element={<p className="text-text-primary">Solicitudes (pendiente)</p>} />
          <Route path="lancheros" element={<p className="text-text-primary">Lancheros (pendiente)</p>} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
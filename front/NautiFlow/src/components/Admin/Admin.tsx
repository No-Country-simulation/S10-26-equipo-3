import Login from "../Login/Login";
import Splash from "../Splash/Splah";
import { Navigate, Route, Routes } from "react-router-dom";
import { useState } from "react";
import { authService, type LoginResponse } from "../../services/authService";
import SolicitudesPendientes from "./SolicitudesPendientes";

export default function Admin() {
  const [token, setToken] = useState(() => authService.getToken());
  const [showSplash, setShowSplash] = useState(true);

  const iniciarSesion = (response: LoginResponse) => setToken(response.token);
  const cerrarSesion = () => {
    authService.logout();
    setToken(null);
  };

  return (
    <>
      {token ? (
        <Routes>
          <Route path="solicitudes" element={<SolicitudesPendientes token={token} onLogout={cerrarSesion} />} />
          <Route path="solicitudes/:id" element={<SolicitudesPendientes token={token} onLogout={cerrarSesion} />} />
          <Route path="*" element={<Navigate to="/admin/solicitudes" replace />} />
        </Routes>
      ) : <Login onSuccess={iniciarSesion} />}
      {showSplash && <Splash onFinish={() => setShowSplash(false)} />}
    </>
  );
}

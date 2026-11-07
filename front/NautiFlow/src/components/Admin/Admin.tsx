import Login from "../Login/Login";
import Splash from "../Splash/Splah";
import { Outlet, useNavigate } from "react-router-dom";
import { useState } from "react";
import { authService, type LoginResponse } from "../../services/authService";
import AdminLayout from "./AdminLayout";

export type AdminOutletContext = {
  token: string;
  onLogout: () => void;
};

export default function Admin() {
  const [token, setToken] = useState(() => authService.getToken());
  const [showSplash, setShowSplash] = useState(true);
  const navigate = useNavigate();
  const iniciarSesion = (response: LoginResponse) => {
    setToken(response.token);
    navigate("/panel", { replace: true });
  };
  const cerrarSesion = () => {
    authService.logout();
    setToken(null);
    navigate("/admin", { replace: true });
  };

  return (
    <>
      {token ? (
        <AdminLayout onCerrarSesion={cerrarSesion}>
          <Outlet context={{ token, onLogout: cerrarSesion } satisfies AdminOutletContext} />
        </AdminLayout>
      ) : <Login onSuccess={iniciarSesion} />}
      {showSplash && <Splash onFinish={() => setShowSplash(false)} />}
    </>
  );
}

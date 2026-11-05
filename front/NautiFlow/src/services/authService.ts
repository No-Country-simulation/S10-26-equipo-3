const BASE_URL: string =
  import.meta.env.VITE_API_URL ?? "http://localhost:3000/api";
 
const TOKEN_KEY = "token";
 
export interface LoginRequest {
  email: string;
  password: string;
}
 
// Ajusta esta forma a lo que devuelva tu API
export interface LoginResponse {
  token: string;
 
}
 
async function login(credentials: LoginRequest): Promise<LoginResponse> {
  let res: Response;
  try {
    // Cambia "/auth/login" si tu endpoint es distinto
    res = await fetch(`${BASE_URL}/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(credentials),
    });
  } catch {
    throw new Error("No se pudo conectar con el servidor.");
  }
 
  if (!res.ok) {
    if (res.status === 401) throw new Error("Email o contraseña incorrectos.");
    let message = "Ocurrió un error. Inténtalo de nuevo.";
    try {
      message = (await res.json()).message ?? message;
    } catch {
      /* la respuesta no era JSON */
    }
    throw new Error(message);
  }
 
  const data = (await res.json()) as LoginResponse;
  localStorage.setItem(TOKEN_KEY, data.token);
  return data;
}
 
function logout() {
  localStorage.removeItem(TOKEN_KEY);
}
 
function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}
 
export const authService = { login, logout, getToken };
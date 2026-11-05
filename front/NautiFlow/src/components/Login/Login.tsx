import { type FormEvent, useState } from "react";
import Imglogin from "./Imglogin/Imglogin";
import Logo from "../Logo";
import { authService, type LoginResponse } from "../../services/authService";
 
interface LoginProps {
  onSuccess?: (data: LoginResponse) => void; 
}
 
const inputClass =
  "h-11 w-full rounded-lg border border-transparent bg-white px-3.5 text-sm text-[#1b1f21] placeholder:text-[#8d9799] focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-[#1f5463]";
 
export default function Login({ onSuccess }: LoginProps) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
 
  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (loading) return;
    setLoading(true);
    setError(null);
    try {
      const data = await authService.login({ email, password });
      onSuccess?.(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Ocurrió un error inesperado.");
    } finally {
      setLoading(false);
    }
  };
 
  return (
    <main className="grid min-h-screen grid-cols-1 overflow-x-hidden bg-[#e2e8e8] font-sans text-[#1b1f21] md:grid-cols-[minmax(360px,45%)_minmax(0,1fr)]">
     
      <svg className="absolute" width="0" height="0" aria-hidden="true">
        <defs>
          <clipPath id="login-blob" clipPathUnits="objectBoundingBox">
            <path d="M0,0 L1,0 L1,0.88 C0.8,0.95 0.5,1 0.1,1 C0,0.9 0,0.75 0.05,0.6 C0.1,0.45 0.12,0.3 0.06,0.15 C0.03,0.08 0.01,0.03 0,0 Z" />
          </clipPath>
        </defs>
      </svg>
 
    
      <section className="flex min-w-0 flex-col px-6 py-8 md:px-12 md:py-10 xl:px-16 xl:py-12">
       
        <div className="w-10 shrink-0 [&>*]:h-auto [&>*]:w-full">
          <Logo />
        </div>
 
        <div className="mx-auto flex w-full max-w-[360px] flex-1 flex-col justify-center md:mx-0">
          <p className="text-lg font-medium text-[#5f6b6e]">Bienvenido a</p>
          <h1 className="mb-10 mt-1 text-[2rem] font-bold tracking-tight sm:text-4xl">
            NautiFlow
          </h1>
 
          <form onSubmit={handleSubmit} className="flex flex-col">
            <label htmlFor="email" className="mb-1.5 text-sm font-medium">
              Email
            </label>
            <input
              id="email"
              type="email"
              placeholder="example@mail.com"
              autoComplete="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              disabled={loading}
              required
              className={`${inputClass} mb-4`}
            />
 
            <label htmlFor="password" className="mb-1.5 text-sm font-medium">
              Contraseña
            </label>
            <div className="relative mb-4">
              <input
                id="password"
                type={showPassword ? "text" : "password"}
                placeholder="******"
                autoComplete="current-password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                disabled={loading}
                required
                className={`${inputClass} pr-11`}
              />
              <button
                type="button"
                onClick={() => setShowPassword((v) => !v)}
                aria-label={showPassword ? "Ocultar contraseña" : "Mostrar contraseña"}
                className="absolute right-0 top-0 grid h-11 w-11 place-items-center rounded-lg focus-visible:outline-2 focus-visible:outline-[#1f5463]"
              >
                <svg
                  width="18"
                  height="18"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="1.8"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  {showPassword ? (
                    <>
                      <path d="M3 3l18 18" />
                      <path d="M10.6 6.1A10.7 10.7 0 0 1 12 6c6 0 9.5 6 9.5 6a17 17 0 0 1-3.2 3.9M6.6 7.6C4 9.3 2.5 12 2.5 12S6 18 12 18c1.4 0 2.6-.3 3.7-.8" />
                      <path d="M9.9 9.9a3 3 0 0 0 4.2 4.2" />
                    </>
                  ) : (
                    <>
                      <path d="M2.5 12S6 6 12 6s9.5 6 9.5 6-3.5 6-9.5 6S2.5 12 2.5 12z" />
                      <circle cx="12" cy="12" r="3" />
                    </>
                  )}
                </svg>
              </button>
            </div>
 
            {error && (
              <p role="alert" className="mb-2 text-sm text-red-700">
                {error}
              </p>
            )}
 
            <button
              type="submit"
              disabled={loading}
              className="mt-2 h-11 rounded-full bg-[#1f5463] text-sm font-semibold text-white transition-colors hover:bg-[#17414d] focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#1f5463] disabled:cursor-not-allowed disabled:opacity-60"
            >
              {loading ? "Iniciando sesión…" : "Iniciar sesión"}
            </button>
          </form>
        </div>
      </section>
 
     
      <aside
        className="relative hidden min-w-0 overflow-hidden md:-ml-[6%] md:block"
        aria-hidden="true"
      >
        <Imglogin />
      </aside>
    </main>
  );
}
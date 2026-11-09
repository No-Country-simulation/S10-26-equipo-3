import { useNavigate } from "react-router-dom";
import imagenLanchaBanner from "../../assets/landing/imagen-lancha-banner.png";

export default function RegistroLanchaSection() {
  const navigate = useNavigate();

  return (
    <section className="flex flex-col items-center w-full overflow-clip px-4 sm:px-8 md:px-[60px] py-10 md:py-16 shrink-0">
      {/* Banner */}
      <div className="flex flex-col md:flex-row items-stretch w-full  mx-auto overflow-hidden rounded-2xl bg-bg-fill-secondary shadow-sm">
        {/* Imagen izquierda / superior en móvil */}
        <div className="relative w-full md:w-1/2 h-48 sm:h-64 md:h-auto min-h-[200px] shrink-0">
          <img
            src={imagenLanchaBanner}
            alt="Lancha en el delta"
            className="absolute inset-0 size-full object-cover"
          />
        </div>

        {/* Contenido derecho */}
        <div className="flex flex-1 flex-col gap-4 justify-center items-start p-6 sm:p-8 md:p-10 w-full">
          <h2 className="font-bold text-2xl sm:text-3xl md:text-[length:var(--text-heading-3xl-size)] leading-snug md:leading-[var(--text-heading-3xl-line-height)] text-bg-fill-primary">
            ¿Tienes una lancha?
          </h2>

          <p className="font-medium text-sm sm:text-base leading-relaxed text-bg-fill-primary/90 w-full">
            Tu lancha también puede ser parte de NautiFlow. Registra tu
            embarcación, recibe solicitudes y conecta con nuevos pasajeros.
          </p>

          <button
            type="button"
            onClick={() => navigate("/registro")}
            className="flex items-center justify-center gap-2 h-11 px-6 rounded-full bg-bg-fill-primary text-text-invert font-semibold text-sm sm:text-base transition-colors hover:bg-bg-fill-primary-hover w-full sm:w-auto cursor-pointer mt-2"
          >
            Registrar mi lancha
          </button>
        </div>
      </div>
    </section>
  );
}

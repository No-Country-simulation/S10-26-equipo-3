import imagenLanchaBanner from "../assets/landing/imagen-lancha-banner.jpeg";

export default function RegistroLanchaSection() {
  return (
    <section className="flex flex-col items-start w-full overflow-clip px-8 py-12 shrink-0">
      {/* Banner */}
      <div
        className="flex items-start w-full overflow-clip p-2.5 rounded-l shrink-0"
        style={{ height: "220px" }}
      >
        {/* Imagen izquierda */}
        <div
          className="relative h-full rounded-tl-l rounded-bl-l shrink-0"
          style={{ aspectRatio: "730/200" }}
        >
          <div className="absolute inset-0 overflow-hidden pointer-events-none rounded-tl-l rounded-bl-l">
            <img
              src={imagenLanchaBanner}
              alt="Lancha en el delta"
              className="absolute max-w-none w-full"
              style={{
                height: "205.67%",
                left: "0.02%",
                top: "-81.95%",
              }}
            />
          </div>
        </div>

        {/* Contenido derecho */}
        <div className="flex flex-1 flex-col gap-3 h-full items-start justify-center min-w-0 overflow-clip p-8 rounded-tr-l rounded-br-l bg-bg-fill-secondary">
          <h2 className="font-bold text-[length:var(--text-heading-3xl-size)] leading-[var(--text-heading-3xl-line-height)] text-bg-fill-primary whitespace-nowrap">
            ¿Tienes una lancha?
          </h2>

          <div className="flex flex-col gap-5 items-start justify-center w-full shrink-0">
            <p className="font-semibold text-[length:var(--text-body-sm-semibold-size)] leading-[var(--text-body-sm-semibold-line-height)] text-bg-fill-primary w-full">
              Tu lancha también puede ser parte de NautiFlow. Registra tu
              embarcación, recibe solicitudes y conecta con nuevos pasajeros.
            </p>
            <button
              type="button"
              className="flex items-center justify-center gap-2 h-10 px-4 py-2 w-full rounded-full bg-bg-fill-primary text-text-invert font-medium text-[length:var(--text-body-sm-semibold-size)] leading-[var(--text-body-sm-semibold-line-height)] transition-colors hover:bg-bg-fill-primary-hover"
            >
              Registrar mi lancha
            </button>
          </div>
        </div>
      </div>
    </section>
  );
}

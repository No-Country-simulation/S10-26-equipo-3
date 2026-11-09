import type { ActividadMensual } from "../../services/adminDashboardService";

const MESES_CORTOS = ["Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"];
const MESES_LARGOS = ["enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"];

function partesMes(mes: string): { corto: string; largo: string } {
  const [anio, numero] = mes.split("-");
  const indice = Number(numero) - 1;
  if (Number.isNaN(indice) || indice < 0 || indice > 11) return { corto: mes, largo: mes };
  return { corto: MESES_CORTOS[indice], largo: `${MESES_LARGOS[indice]} ${anio}` };
}

type Props = {
  actividad: ActividadMensual[] | null;
};

export default function ActividadViajesChart({ actividad }: Props) {
  const maximo = Math.max(1, ...(actividad ?? []).map((item) => item.viajes));
  const total = (actividad ?? []).reduce((suma, item) => suma + item.viajes, 0);

  return (
    <article aria-labelledby="actividad-viajes-title" className="rounded-xl border border-border-secondary bg-white p-4 sm:p-6">
      <header className="flex flex-wrap items-baseline justify-between gap-2">
        <h2 id="actividad-viajes-title" className="text-xl font-bold leading-7 text-text-primary">Actividad de viaje</h2>
        <p className="text-sm font-semibold text-text-secondary">Últimos 6 meses</p>
      </header>
      {actividad === null ? (
        <p role="status" className="py-16 text-center text-sm text-text-secondary">Cargando actividad…</p>
      ) : actividad.length === 0 ? (
        <p className="py-16 text-center text-sm text-text-secondary">Todavía no hay viajes registrados.</p>
      ) : (
        <>
          <div className="mt-6 flex h-56 items-end justify-around gap-2 border-b border-border-secondary sm:gap-6" aria-hidden="true">
            {actividad.map((item) => {
              const { corto, largo } = partesMes(item.mes);
              const alto = item.viajes === 0 ? 0 : Math.max(4, Math.round((item.viajes / maximo) * 184));
              return (
                <div key={item.mes} className="group relative flex h-full w-full max-w-14 flex-col items-center justify-end">
                  <span className="mb-1.5 text-sm font-semibold tabular-nums text-text-primary">{item.viajes}</span>
                  <div
                    className="w-full max-w-11 rounded-t-[4px] bg-bg-fill-primary transition-opacity group-hover:opacity-85"
                    style={{ height: `${alto}px` }}
                  />
                  <span className="pointer-events-none absolute -top-9 left-1/2 z-10 hidden -translate-x-1/2 whitespace-nowrap rounded-md bg-text-primary px-2.5 py-1.5 text-xs font-semibold text-white shadow-md group-hover:block">
                    {item.viajes} {item.viajes === 1 ? "viaje" : "viajes"} en {largo}
                  </span>
                  <span className="sr-only">{corto}</span>
                </div>
              );
            })}
          </div>
          <div className="mt-2 flex justify-around gap-2 sm:gap-6" aria-hidden="true">
            {actividad.map((item) => (
              <span key={item.mes} className="w-full max-w-14 text-center text-sm text-text-secondary">{partesMes(item.mes).corto}</span>
            ))}
          </div>
          <table className="sr-only">
            <caption>Viajes iniciados por mes en los últimos 6 meses. Total: {total}.</caption>
            <thead><tr><th scope="col">Mes</th><th scope="col">Viajes</th></tr></thead>
            <tbody>
              {actividad.map((item) => (
                <tr key={item.mes}><td>{partesMes(item.mes).largo}</td><td>{item.viajes}</td></tr>
              ))}
            </tbody>
          </table>
        </>
      )}
    </article>
  );
}

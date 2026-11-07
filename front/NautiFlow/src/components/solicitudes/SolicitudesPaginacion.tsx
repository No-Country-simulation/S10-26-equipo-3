type Props = {
  page: number;
  totalPages: number;
  disabled?: boolean;
  onChange: (page: number) => void;
};

export default function SolicitudesPaginacion({
  page,
  totalPages,
  disabled = false,
  onChange,
}: Props) {
  const cantidadPaginas = Math.max(totalPages, 1);
  const paginas = cantidadPaginas <= 4
    ? Array.from({ length: cantidadPaginas }, (_, index) => index)
    : page < 3
      ? [0, 1, 2]
      : page >= cantidadPaginas - 2
        ? [cantidadPaginas - 3, cantidadPaginas - 2, cantidadPaginas - 1]
        : [page - 1, page, page + 1];

  return (
    <nav
      aria-label="Paginación de solicitudes"
      className="flex items-center justify-center gap-4 py-10 text-sm font-semibold text-text-primary"
    >
      <div className="flex flex-wrap items-center justify-center gap-3">
        {paginas[0] > 0 && (
          <button
            type="button"
            disabled={disabled}
            onClick={() => onChange(0)}
            className="grid h-9 w-9 place-items-center rounded-full hover:bg-bg-surface-secondary disabled:cursor-not-allowed"
          >
            1
          </button>
        )}
        {paginas[0] > 1 && <span aria-hidden="true">…</span>}
        {paginas.map((pagina) => (
          <button
            key={pagina}
            type="button"
            aria-current={page === pagina ? "page" : undefined}
            aria-label={`Página ${pagina + 1}`}
            disabled={disabled}
            onClick={() => onChange(pagina)}
            className={`grid h-9 w-9 place-items-center rounded-full disabled:cursor-not-allowed ${
              page === pagina
                ? "border border-bg-fill-primary text-text-primary"
                : "hover:bg-bg-surface-secondary"
            }`}
          >
            {pagina + 1}
          </button>
        ))}
        {paginas[paginas.length - 1] < cantidadPaginas - 1 && (
          <span aria-hidden="true">…</span>
        )}
        <button
          type="button"
          disabled={disabled || page + 1 >= totalPages}
          onClick={() => onChange(page + 1)}
          className="inline-flex items-center gap-2 rounded-full px-2 py-2 hover:bg-bg-surface-secondary disabled:cursor-not-allowed disabled:opacity-50"
        >
          Siguiente
          <span aria-hidden="true">›</span>
        </button>
      </div>
    </nav>
  );
}

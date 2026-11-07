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
      className="flex items-center justify-center py-3 text-xs font-semibold text-text-primary sm:py-5 lg:py-6"
    >
      <div className="flex items-center justify-center gap-1 sm:gap-2">
        {paginas[0] > 0 && (
          <button
            type="button"
            disabled={disabled}
            onClick={() => onChange(0)}
            className="grid h-8 w-8 place-items-center rounded-full hover:bg-bg-surface-secondary disabled:cursor-not-allowed"
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
            className={`grid h-8 w-8 place-items-center rounded-full disabled:cursor-not-allowed ${
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
          className="inline-flex min-h-8 items-center gap-1 rounded-full px-1.5 py-1 hover:bg-bg-surface-secondary disabled:cursor-not-allowed disabled:opacity-50 sm:gap-2 sm:px-2 sm:py-2 lg:px-2 lg:py-2"
        >
          <span className="hidden sm:inline">Siguiente</span>
          <span aria-hidden="true">›</span>
        </button>
      </div>
    </nav>
  );
}

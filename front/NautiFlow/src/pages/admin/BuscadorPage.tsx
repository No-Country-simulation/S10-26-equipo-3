import { useEffect, useRef, useState } from "react";
import {
  BsSearch,
  BsPerson,
  BsGeoAlt,
  BsCheck2,
  BsDash,
  BsPlus,
} from "react-icons/bs";
import AsignarViajeButton from "../../components/Admin/AsignarViajeButton";
import Notification, { type NotificationKind } from "../../components/Admin/Notification";
import type { ViajeCreado } from "../../services/viajeAdminService";
import { zonasService, type Zona } from "../../services/zonasService";
import {
  buscarLanchasDisponibles,
  type LanchaDisponible,
} from "../../services/lancheroAdminService";

type Props = {
  token: string;
};

function IconoBarco({ className = "size-4" }: { className?: string }) {
  return (
    <svg
      className={className}
      viewBox="0 0 16 15"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      aria-hidden="true"
    >
      <path
        d="M12.5 6.5V2.5H9.5M9.5 2.5H6.5M9.5 2.5V0.5H6.5V2.5M6.5 2.5H3.5V6.5"
        stroke="currentColor"
        strokeLinejoin="round"
      />
      <path
        d="M3 13.5L1.5 7.5L8 5.5L14.5 7.5L13 13.5"
        stroke="currentColor"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <path
        d="M0.5 12.5L0.9 12.9C1.1 13.1 1.3 13.3 1.6 13.4C1.8 13.5 2.1 13.6 2.4 13.6C2.6 13.6 2.9 13.5 3.2 13.4C3.4 13.3 3.6 13.1 3.8 12.9L4.25 12.5L4.7 12.9C4.9 13.1 5.1 13.3 5.3 13.4C5.6 13.5 5.9 13.6 6.1 13.6C6.4 13.6 6.7 13.5 6.9 13.4C7.2 13.3 7.4 13.1 7.6 12.9L8 12.5L8.4 12.9C8.6 13.1 8.8 13.3 9.1 13.4C9.3 13.5 9.6 13.6 9.9 13.6C10.1 13.6 10.4 13.5 10.7 13.4C10.9 13.3 11.1 13.1 11.3 12.9L11.75 12.5L12.2 12.9C12.4 13.1 12.6 13.3 12.8 13.4C13.1 13.5 13.4 13.6 13.6 13.6C13.9 13.6 14.2 13.5 14.4 13.4C14.7 13.3 14.9 13.1 15.1 12.9L15.5 12.5M5 8.5L8 7.5L11 8.5"
        stroke="currentColor"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}

type LanchaResultado = {
  id: number;
  nombre: string;
  capacidad: number;
  zonasDisponibles: string;
  idZonaOrigen: number;
  idZonaDestino: number;
  estado: string;
  activo: boolean;
  imagen: string;
  whatsapp?: string;
};

const ZONAS_FALLBACK: Zona[] = [
  { id: 4, nombre: "Delta" },
  { id: 1, nombre: "Primera Sección" },
  { id: 2, nombre: "Segunda Sección" },
  { id: 3, nombre: "Tercera Sección" },
];

type HorarioOpcion = {
  valor24h: string;
  etiqueta12h: string;
};

function obtenerHoraInicial(): string {
  const ahora = new Date();
  const horaActual = ahora.getHours();
  if (horaActual >= 6 && horaActual < 22) {
    return `${String(horaActual + 1).padStart(2, "0")}:00`;
  }
  if (horaActual < 6) {
    return "06:00";
  }
  return "22:00";
}

// Horarios permitidos de 6:00 AM a 10:00 PM (22:00)
const HORARIOS_DISPONIBLES: HorarioOpcion[] = Array.from({ length: 17 }, (_, index) => {
  const horaNum = index + 6; // 6 a 22
  const valor24h = `${horaNum.toString().padStart(2, "0")}:00`;
  const periodo = horaNum < 12 ? "AM" : "PM";
  const hora12 = horaNum <= 12 ? horaNum : horaNum - 12;
  const etiqueta12h = `${hora12}:00 ${periodo}`;
  return { valor24h, etiqueta12h };
});

type Aviso = {
  id: number;
  kind: NotificationKind;
  title: string;
  message: string;
};

export default function BuscadorPage({ token }: Props) {
  const [capacidad, setCapacidad] = useState<number>(2);
  const [dropdownCapacidadAbierto, setDropdownCapacidadAbierto] = useState(false);
  const [tempCapacidad, setTempCapacidad] = useState<number>(2);
  const dropdownRef = useRef<HTMLDivElement>(null);

  const [zonas, setZonas] = useState<Zona[]>(ZONAS_FALLBACK);
  const [origen, setOrigen] = useState<number>(4);
  const [dropdownOrigenAbierto, setDropdownOrigenAbierto] = useState(false);
  const [tempOrigen, setTempOrigen] = useState<number>(4);
  const dropdownOrigenRef = useRef<HTMLDivElement>(null);

  const [destino, setDestino] = useState<number>(1);
  const [dropdownDestinoAbierto, setDropdownDestinoAbierto] = useState(false);
  const [tempDestino, setTempDestino] = useState<number>(1);
  const dropdownDestinoRef = useRef<HTMLDivElement>(null);

  const [hora, setHora] = useState<string>(obtenerHoraInicial);
  const [dropdownHoraAbierto, setDropdownHoraAbierto] = useState(false);
  const [tempHora, setTempHora] = useState<string>(obtenerHoraInicial);
  const dropdownHoraRef = useRef<HTMLDivElement>(null);

  const [lanchas, setLanchas] = useState<LanchaResultado[]>([]);
  const [cargandoLanchas, setCargandoLanchas] = useState(false);
  const [errorBusqueda, setErrorBusqueda] = useState<string | null>(null);
  const [aviso, setAviso] = useState<Aviso | null>(null);

  useEffect(() => {
    let cancelado = false;
    zonasService
      .obtenerZonas()
      .then((data) => {
        if (!cancelado && Array.isArray(data) && data.length > 0) {
          const delta = data.find((z) => z.id === 4 || z.nombre.toLowerCase() === "delta") ?? {
            id: 4,
            nombre: "Delta",
          };
          const otrasZonas = data.filter((z) => z.id !== delta.id && z.nombre.toLowerCase() !== "delta");
          setZonas([delta, ...otrasZonas]);
        }
      })
      .catch(() => {
        // En caso de error de red, mantiene ZONAS_FALLBACK
      });
    return () => {
      cancelado = true;
    };
  }, []);

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setDropdownCapacidadAbierto(false);
      }
      if (dropdownOrigenRef.current && !dropdownOrigenRef.current.contains(event.target as Node)) {
        setDropdownOrigenAbierto(false);
      }
      if (dropdownDestinoRef.current && !dropdownDestinoRef.current.contains(event.target as Node)) {
        setDropdownDestinoAbierto(false);
      }
      if (dropdownHoraRef.current && !dropdownHoraRef.current.contains(event.target as Node)) {
        setDropdownHoraAbierto(false);
      }
    }
    if (
      dropdownCapacidadAbierto ||
      dropdownOrigenAbierto ||
      dropdownDestinoAbierto ||
      dropdownHoraAbierto
    ) {
      document.addEventListener("mousedown", handleClickOutside);
    }
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [
    dropdownCapacidadAbierto,
    dropdownOrigenAbierto,
    dropdownDestinoAbierto,
    dropdownHoraAbierto,
  ]);

  const zonaOrigenSeleccionada = zonas.find((z) => z.id === origen)?.nombre ?? "Delta";
  const zonaDestinoSeleccionada = zonas.find((z) => z.id === destino)?.nombre ?? "Primera Sección";
  const horaSeleccionadaEtiqueta =
    HORARIOS_DISPONIBLES.find((h) => h.valor24h === hora)?.etiqueta12h ?? `${hora} hs`;

  const ejecutarBusqueda = async (
    filtroCapacidad: number,
    filtroOrigen: number,
    filtroDestino: number,
    filtroHora: string,
  ) => {
    setCargandoLanchas(true);
    setErrorBusqueda(null);
    try {
      const data = await buscarLanchasDisponibles(token, {
        capacidad: filtroCapacidad,
        zonaOrigen: filtroOrigen,
        zonaDestino: filtroDestino,
        hora: filtroHora,
      });
      const mapeadas: LanchaResultado[] = data.map((item: LanchaDisponible) => ({
        id: item.id,
        nombre: item.nombre,
        capacidad: item.capacidad,
        zonasDisponibles: Array.isArray(item.zonasDisponibles)
          ? item.zonasDisponibles.join(", ")
          : String(item.zonasDisponibles ?? ""),
        idZonaOrigen: filtroOrigen,
        idZonaDestino: filtroDestino,
        estado: item.verificada ? "Verificado" : "No verificado",
        activo: item.estadoOperativo === "ACTIVO",
        imagen: "/lancha-buscador.png",
        whatsapp: item.whatsappLanchero,
      }));
      setLanchas(mapeadas);
    } catch (err: unknown) {
      const mensaje =
        err instanceof Error ? err.message : "Error al buscar embarcaciones disponibles.";
      setErrorBusqueda(mensaje);
      setLanchas([]);
    } finally {
      setCargandoLanchas(false);
    }
  };

  const handleBuscar = () => {
    ejecutarBusqueda(capacidad, origen, destino, hora);
  };

  useEffect(() => {
    ejecutarBusqueda(capacidad, origen, destino, hora);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleViajeAsignado = (trip: ViajeCreado) => {
    setAviso({
      id: Date.now(),
      kind: "success",
      title: "Viaje asignado con éxito",
      message: `Se asignó el viaje #${trip.id} a ${trip.turistaNombreCompleto}.`,
    });
  };

  const construirUrlWhatsApp = (lancha: LanchaResultado) => {
    const telefono = (lancha.whatsapp ?? "1123456789").replace(/\D/g, "");
    const mensaje =
      `Hola, te consulto por la embarcación *${lancha.nombre}*:\n` +
      `• Capacidad requerida: *${capacidad} personas*\n` +
      `• Zona origen: *${zonaOrigenSeleccionada}*\n` +
      `• Zona destino: *${zonaDestinoSeleccionada}*\n` +
      `• Hora de salida: *${horaSeleccionadaEtiqueta}*\n\n` +
      `¿Confirmas disponibilidad para realizar este viaje?`;
    return `https://wa.me/${telefono}?text=${encodeURIComponent(mensaje)}`;
  };

  return (
    <div className="flex flex-col gap-6 w-full -mx-1 sm:-mx-2 lg:-mx-3 pt-2">
      {/* Notificación flotante */}
      {aviso && (
        <Notification
          kind={aviso.kind}
          title={aviso.title}
          message={aviso.message}
          onClose={() => setAviso(null)}
        />
      )}

      {/* Título de la página */}
      <div className="flex flex-col gap-1 w-full">
        <h1 className="text-2xl sm:text-[28px] font-bold text-text-primary leading-tight">
          Búsqueda de viajes
        </h1>
      </div>

      {/* Barra de búsqueda de viajes (Figma 1819:4943) */}
      <div className="flex items-center justify-center w-full">
        <div className="w-full max-w-[914px] bg-white rounded-2xl md:rounded-full shadow-[0px_4px_6px_-2px_rgba(12,12,13,0.1),0px_2px_4px_-2px_rgba(12,12,13,0.05)] border border-border-secondary p-3 md:p-2 flex flex-col md:flex-row items-stretch md:items-center justify-between gap-2 md:gap-0">
          {/* Campo Capacidad con Popover / Dropdown (Figma 1582:4658) */}
          <div
            ref={dropdownRef}
            className="relative flex-1"
          >
            <button
              type="button"
              onClick={() => {
                setTempCapacidad(capacidad);
                setDropdownCapacidadAbierto((prev) => !prev);
              }}
              className="w-full flex flex-col justify-center px-4 py-2 hover:bg-bg-surface-secondary/50 rounded-xl md:rounded-l-full transition-colors cursor-pointer text-left"
            >
              <span className="text-xs font-semibold text-text-secondary leading-4">
                Capacidad
              </span>
              <span className="text-sm font-semibold text-text-primary leading-[18px]">
                {capacidad} personas
              </span>
            </button>

            {/* Dropdown capacidad popover */}
            {dropdownCapacidadAbierto && (
              <div
                role="dialog"
                aria-label="Selector de capacidad"
                className="absolute top-full left-0 mt-2 z-40 w-[240px] bg-white rounded-xl shadow-[0px_4px_16px_rgba(12,12,13,0.15)] border border-border-secondary p-4 flex flex-col gap-3"
              >
                <div className="flex flex-col gap-0.5">
                  <p className="font-bold text-sm text-text-primary leading-[18px]">
                    Capacidad
                  </p>
                  <p className="text-xs font-semibold text-text-secondary leading-4">
                    ¿Cuántas personas viajan?
                  </p>
                </div>

                {/* Selector cantidad (+ -) */}
                <div className="flex items-center justify-center gap-6 py-1">
                  <button
                    type="button"
                    aria-label="Disminuir personas"
                    onClick={() => setTempCapacidad((prev) => Math.max(1, prev - 1))}
                    disabled={tempCapacidad <= 1}
                    className="size-7 rounded-full flex items-center justify-center text-text-primary hover:bg-bg-surface-secondary transition-colors disabled:opacity-30 disabled:cursor-not-allowed cursor-pointer"
                  >
                    <BsDash className="size-4" />
                  </button>
                  <span className="text-sm font-semibold text-text-primary min-w-[20px] text-center">
                    {tempCapacidad}
                  </span>
                  <button
                    type="button"
                    aria-label="Aumentar personas"
                    onClick={() => setTempCapacidad((prev) => prev + 1)}
                    className="size-7 rounded-full flex items-center justify-center text-text-primary hover:bg-bg-surface-secondary transition-colors cursor-pointer"
                  >
                    <BsPlus className="size-5" />
                  </button>
                </div>

                {/* Botón Aplicar */}
                <button
                  type="button"
                  onClick={() => {
                    setCapacidad(tempCapacidad);
                    setDropdownCapacidadAbierto(false);
                  }}
                  className="w-full h-10 rounded-full bg-bg-fill-primary hover:bg-bg-fill-primary-hover text-text-invert font-semibold text-sm transition-colors flex items-center justify-center cursor-pointer shadow-sm"
                >
                  Aplicar
                </button>
              </div>
            )}
          </div>

          <div className="hidden md:block h-10 w-px bg-border-secondary shrink-0" />

          {/* Campo Origen con Popover / Dropdown (Figma 1582:4672) */}
          <div ref={dropdownOrigenRef} className="relative flex-1">
            <button
              type="button"
              onClick={() => {
                setTempOrigen(origen);
                setDropdownOrigenAbierto((prev) => !prev);
              }}
              className="w-full flex flex-col justify-center px-4 py-2 hover:bg-bg-surface-secondary/50 rounded-xl transition-colors cursor-pointer text-left"
            >
              <span className="text-xs font-semibold text-text-secondary leading-4">
                Origen
              </span>
              <span className="text-sm font-semibold text-text-primary leading-[18px] truncate">
                {zonaOrigenSeleccionada}
              </span>
            </button>

            {/* Dropdown origen popover */}
            {dropdownOrigenAbierto && (
              <div
                role="dialog"
                aria-label="Selector de origen"
                className="absolute top-full left-0 mt-2 z-40 w-[240px] bg-white rounded-xl shadow-[0px_4px_16px_rgba(12,12,13,0.15)] border border-border-secondary p-4 flex flex-col gap-3"
              >
                <div className="flex flex-col gap-0.5">
                  <p className="font-bold text-sm text-text-primary leading-[18px]">
                    Origen
                  </p>
                  <p className="text-xs font-semibold text-text-secondary leading-4">
                    Selecciona una zona de salida
                  </p>
                </div>

                <div className="h-px w-full bg-border-secondary" />

                {/* Lista de Zonas */}
                <div className="flex flex-col gap-2 max-h-[220px] overflow-y-auto pr-1">
                  {zonas.map((z) => {
                    const isSelected = tempOrigen === z.id;
                    return (
                      <button
                        key={z.id}
                        type="button"
                        onClick={() => setTempOrigen(z.id)}
                        className={`flex items-center justify-between p-1.5 rounded-lg transition-colors cursor-pointer text-left ${
                          isSelected ? "bg-bg-surface-secondary" : "hover:bg-bg-surface-primary"
                        }`}
                      >
                        <div className="flex items-center gap-2 min-w-0">
                          {/* Ícono barco */}
                          <IconoBarco className="size-4 shrink-0 text-text-primary" />
                          <span className="font-bold text-xs text-text-primary leading-4 truncate">
                            {z.nombre}
                          </span>
                        </div>

                        {/* Radio indicator */}
                        <div
                          className={`size-[18px] rounded-full border-2 flex items-center justify-center shrink-0 transition-colors ${
                            isSelected
                              ? "border-bg-fill-primary"
                              : "border-border-secondary"
                          }`}
                        >
                          {isSelected && (
                            <div className="size-2 rounded-full bg-bg-fill-primary" />
                          )}
                        </div>
                      </button>
                    );
                  })}
                </div>

                {/* Botón Aplicar */}
                <button
                  type="button"
                  onClick={() => {
                    setOrigen(tempOrigen);
                    if (destino === tempOrigen) {
                      const otraZona = zonas.find((z) => z.id !== tempOrigen);
                      if (otraZona) setDestino(otraZona.id);
                    }
                    setDropdownOrigenAbierto(false);
                  }}
                  className="w-full h-10 rounded-full bg-bg-fill-primary hover:bg-bg-fill-primary-hover text-text-invert font-semibold text-sm transition-colors flex items-center justify-center cursor-pointer shadow-sm mt-1"
                >
                  Aplicar
                </button>
              </div>
            )}
          </div>

          <div className="hidden md:block h-10 w-px bg-border-secondary shrink-0" />

          {/* Campo Destino con Popover / Dropdown (Figma 1582:4819) */}
          <div ref={dropdownDestinoRef} className="relative flex-1">
            <button
              type="button"
              onClick={() => {
                setTempDestino(destino);
                setDropdownDestinoAbierto((prev) => !prev);
              }}
              className="w-full flex flex-col justify-center px-4 py-2 hover:bg-bg-surface-secondary/50 rounded-xl transition-colors cursor-pointer text-left"
            >
              <span className="text-xs font-semibold text-text-secondary leading-4">
                Destino
              </span>
              <span className="text-sm font-semibold text-text-primary leading-[18px] truncate">
                {zonaDestinoSeleccionada}
              </span>
            </button>

            {/* Dropdown destino popover */}
            {dropdownDestinoAbierto && (
              <div
                role="dialog"
                aria-label="Selector de destino"
                className="absolute top-full left-0 mt-2 z-40 w-[240px] bg-white rounded-xl shadow-[0px_4px_16px_rgba(12,12,13,0.15)] border border-border-secondary p-4 flex flex-col gap-3"
              >
                <div className="flex flex-col gap-0.5">
                  <p className="font-bold text-sm text-text-primary leading-[18px]">
                    Destino
                  </p>
                  <p className="text-xs font-semibold text-text-secondary leading-4">
                    Selecciona una zona de llegada
                  </p>
                </div>

                <div className="h-px w-full bg-border-secondary" />

                {/* Lista de Zonas */}
                <div className="flex flex-col gap-2 max-h-[220px] overflow-y-auto pr-1">
                  {zonas.map((z) => {
                    const isSelected = tempDestino === z.id;
                    const isDisabled = z.id === origen;
                    return (
                      <button
                        key={z.id}
                        type="button"
                        disabled={isDisabled}
                        title={
                          isDisabled
                            ? "No puedes seleccionar la misma zona de origen"
                            : undefined
                        }
                        onClick={() => {
                          if (!isDisabled) setTempDestino(z.id);
                        }}
                        className={`flex items-center justify-between p-1.5 rounded-lg transition-colors text-left ${
                          isDisabled
                            ? "opacity-35 cursor-not-allowed bg-bg-surface-secondary/30"
                            : isSelected
                            ? "bg-bg-surface-secondary cursor-pointer"
                            : "hover:bg-bg-surface-primary cursor-pointer"
                        }`}
                      >
                        <div className="flex items-center gap-2 min-w-0">
                          {/* Ícono barco */}
                          <IconoBarco
                            className={`size-4 shrink-0 ${
                              isDisabled ? "text-text-disable" : "text-text-primary"
                            }`}
                          />
                          <span
                            className={`font-bold text-xs leading-4 truncate ${
                              isDisabled ? "text-text-disable" : "text-text-primary"
                            }`}
                          >
                            {z.nombre}
                          </span>
                        </div>

                        {/* Radio indicator */}
                        <div
                          className={`size-[18px] rounded-full border-2 flex items-center justify-center shrink-0 transition-colors ${
                            isDisabled
                              ? "border-border-tertiary bg-bg-surface-secondary"
                              : isSelected
                              ? "border-bg-fill-primary"
                              : "border-border-secondary"
                          }`}
                        >
                          {isSelected && !isDisabled && (
                            <div className="size-2 rounded-full bg-bg-fill-primary" />
                          )}
                        </div>
                      </button>
                    );
                  })}
                </div>

                {/* Botón Aplicar */}
                <button
                  type="button"
                  onClick={() => {
                    if (tempDestino !== origen) {
                      setDestino(tempDestino);
                    }
                    setDropdownDestinoAbierto(false);
                  }}
                  className="w-full h-10 rounded-full bg-bg-fill-primary hover:bg-bg-fill-primary-hover text-text-invert font-semibold text-sm transition-colors flex items-center justify-center cursor-pointer shadow-sm mt-1"
                >
                  Aplicar
                </button>
              </div>
            )}
          </div>

          <div className="hidden md:block h-10 w-px bg-border-secondary shrink-0" />

          {/* Campo Cuándo con Popover / Dropdown (Figma 1621:7025) */}
          <div ref={dropdownHoraRef} className="relative flex-1">
            <button
              type="button"
              onClick={() => {
                setTempHora(hora);
                setDropdownHoraAbierto((prev) => !prev);
              }}
              className="w-full flex flex-col justify-center px-4 py-2 hover:bg-bg-surface-secondary/50 rounded-xl transition-colors cursor-pointer text-left"
            >
              <span className="text-xs font-semibold text-text-secondary leading-4">
                Cuándo
              </span>
              <span className="text-sm font-semibold text-text-primary leading-[18px] truncate">
                {horaSeleccionadaEtiqueta}
              </span>
            </button>

            {/* Dropdown horarios disponibles popover */}
            {dropdownHoraAbierto && (
              <div
                role="dialog"
                aria-label="Selector de horarios disponibles"
                className="absolute top-full right-0 md:left-0 mt-2 z-40 w-[290px] bg-white rounded-xl shadow-[0px_4px_16px_rgba(12,12,13,0.15)] border border-border-secondary p-4 flex flex-col gap-3"
              >
                <div className="flex flex-col gap-0.5">
                  <p className="font-bold text-sm text-text-primary leading-[18px]">
                    Horarios disponibles
                  </p>
                  <p className="text-xs font-semibold text-text-secondary leading-4">
                    Selecciona una hora de salida
                  </p>
                </div>

                {/* Grid de horarios (6:00 AM a 10:00 PM) */}
                <div className="grid grid-cols-2 gap-2 max-h-[220px] overflow-y-auto py-1 pr-1">
                  {HORARIOS_DISPONIBLES.map((h) => {
                    const isSelected = tempHora === h.valor24h;
                    return (
                      <button
                        key={h.valor24h}
                        type="button"
                        onClick={() => setTempHora(h.valor24h)}
                        className={`h-10 px-3 rounded-full text-xs font-semibold transition-colors flex items-center justify-center cursor-pointer ${
                          isSelected
                            ? "bg-bg-fill-primary text-text-invert shadow-xs"
                            : "bg-bg-fill-secondary text-text-primary hover:bg-bg-surface-tertiary"
                        }`}
                      >
                        {h.etiqueta12h}
                      </button>
                    );
                  })}
                </div>

                {/* Botón Aplicar */}
                <button
                  type="button"
                  onClick={() => {
                    setHora(tempHora);
                    setDropdownHoraAbierto(false);
                  }}
                  className="w-full h-10 rounded-full bg-bg-fill-primary hover:bg-bg-fill-primary-hover text-text-invert font-semibold text-sm transition-colors flex items-center justify-center cursor-pointer shadow-sm mt-1"
                >
                  Aplicar
                </button>
              </div>
            )}
          </div>

          {/* Botón de búsqueda */}
          <button
            type="button"
            onClick={handleBuscar}
            aria-label="Buscar viajes"
            className="shrink-0 size-12 md:size-14 rounded-full bg-bg-fill-primary text-text-invert flex items-center justify-center hover:bg-bg-fill-primary-hover transition-colors shadow-sm cursor-pointer self-center md:self-auto"
          >
            <BsSearch aria-hidden="true" className="size-5 shrink-0" />
          </button>
        </div>
      </div>

      {/* Contenedor de Resultados (Figma 1819:4957) */}
      <section
        aria-labelledby="resultados-titulo"
        className="bg-white rounded-xl border border-border-secondary p-4 sm:p-6 shadow-sm flex flex-col gap-4 w-full"
      >
        {/* Encabezado de resultados */}
        <div className="flex flex-col gap-1 border-b border-border-primary pb-3">
          <h2
            id="resultados-titulo"
            className="text-lg sm:text-xl font-semibold text-text-primary leading-tight"
          >
            Resultados encontrados
          </h2>
          <p className="text-sm text-text-secondary">
            Viajes disponibles según los filtros seleccionados
          </p>
        </div>

        {/* Lista de viajes (Figma 1819:4961) */}
        {cargandoLanchas ? (
          <div className="flex flex-col items-center justify-center py-12 text-center">
            <span className="text-text-secondary text-base animate-pulse">
              Buscando embarcaciones disponibles…
            </span>
          </div>
        ) : errorBusqueda ? (
          <div className="flex flex-col items-center justify-center py-12 text-center">
            <p className="text-base font-semibold text-text-error">
              {errorBusqueda}
            </p>
            <p className="text-sm text-text-secondary mt-1">
              Prueba cambiando la hora o los filtros seleccionados.
            </p>
          </div>
        ) : lanchas.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-12 text-center">
            <p className="text-base font-semibold text-text-primary">
              No se encontraron lanchas disponibles
            </p>
            <p className="text-sm text-text-secondary mt-1">
              Prueba cambiando la capacidad o las zonas seleccionadas.
            </p>
          </div>
        ) : (
          <div className="flex flex-col gap-4 w-full">
            {lanchas.map((lancha) => (
              <article
                key={lancha.id}
                className="bg-white border border-border-secondary rounded-xl p-3 sm:p-4 flex flex-col md:flex-row items-stretch gap-4 hover:shadow-md transition-shadow"
              >
                {/* Imagen lancha */}
                <div className="relative shrink-0 w-full md:w-[260px] h-[173px] rounded-lg overflow-hidden bg-bg-surface-secondary">
                  <img
                    src={lancha.imagen}
                    alt={lancha.nombre}
                    className="w-full h-full object-cover rounded-lg"
                  />
                </div>

                {/* Contenido / Detalles del viaje */}
                <div className="flex-1 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 min-w-0">
                  <div className="flex flex-col gap-2 min-w-0 flex-1">
                    {/* Nombre */}
                    <h3 className="font-semibold text-base sm:text-lg text-text-primary">
                      {lancha.nombre}
                    </h3>

                    {/* Capacidad */}
                    <div className="flex items-center gap-2 text-sm">
                      <BsPerson className="size-5 shrink-0 text-text-secondary" aria-hidden="true" />
                      <span className="font-semibold text-text-primary">
                        Capacidad: {lancha.capacidad}{" "}
                        <span className="font-normal text-text-secondary">personas</span>
                      </span>
                    </div>

                    {/* Zonas disponibles */}
                    <div className="flex items-center gap-2 text-sm">
                      <BsGeoAlt className="size-5 shrink-0 text-text-secondary" aria-hidden="true" />
                      <span className="font-semibold text-text-primary">
                        Zonas disponibles:{" "}
                        <span className="font-normal text-text-secondary">
                          {lancha.zonasDisponibles}
                        </span>
                      </span>
                    </div>

                    {/* Estado */}
                    <div className="flex items-center gap-2 text-sm">
                      <BsCheck2 className="size-5 shrink-0 text-text-secondary" aria-hidden="true" />
                      <span className="font-semibold text-text-primary">
                        Estado:{" "}
                        <span className="font-normal text-text-secondary">
                          {lancha.estado}
                        </span>
                      </span>
                    </div>
                  </div>

                  {/* Estado Activo */}
                  <div className="shrink-0 sm:self-start pt-1">
                    <span className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-800">
                      Activo
                    </span>
                  </div>

                  {/* Acciones */}
                  <div className="flex flex-col gap-2.5 w-full sm:w-[190px] shrink-0 border-t sm:border-t-0 sm:border-l border-border-secondary pt-3 sm:pt-0 sm:pl-4 justify-center">
                    {/* Botón Asignar viaje */}
                    <AsignarViajeButton
                      token={token}
                      lancha={{
                        id: lancha.id,
                        nombre: lancha.nombre,
                        capacidad: lancha.capacidad,
                      }}
                      busqueda={{
                        idZonaOrigen: origen,
                        zonaOrigen: zonaOrigenSeleccionada,
                        idZonaDestino: destino,
                        zonaDestino: zonaDestinoSeleccionada,
                        hora: hora,
                        cantidadPasajeros: capacidad,
                      }}
                      onAssigned={handleViajeAsignado}
                    />

                    {/* Botón Contactar por WhatsApp */}
                    <a
                      href={construirUrlWhatsApp(lancha)}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="rounded-full bg-bg-fill-secondary px-4 py-2.5 text-sm font-semibold text-text-primary hover:bg-bg-fill-secondary-hover hover:text-text-invert transition-colors flex items-center justify-center gap-2 cursor-pointer text-center"
                    >
                      Contactar
                    </a>
                  </div>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}


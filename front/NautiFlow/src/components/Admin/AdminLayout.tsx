import type { ReactNode } from "react";
import type { IconType } from "react-icons";
import { BsBoxArrowRight, BsChevronLeft, BsFolderCheck, BsPeople, BsPerson, BsSearch, BsSignpostSplit, } from "react-icons/bs";
import Logo from "../Logo";
import { NavLink, useLocation } from "react-router-dom";

export type Seccion = "buscador" | "viajes" | "solicitudes" | "lancheros";

type Grupo = {
    titulo: string;
    items: { ruta: string; etiqueta: string; Icono: IconType }[];
};

const GRUPOS: Grupo[] = [
    {
        titulo: "Gestión de viajes",
        items: [
            { ruta: "/admin/buscador", etiqueta: "Buscador", Icono: BsSearch },
            { ruta: "/admin/viajes", etiqueta: "Viajes", Icono: BsSignpostSplit },
        ],
    },
    {
        titulo: "Gestión de la flota",
        items: [
            { ruta: "/admin/solicitudes", etiqueta: "Solicitudes", Icono: BsFolderCheck },
            { ruta: "/admin/lancheros", etiqueta: "Lancheros", Icono: BsPeople },
        ],
    },
];

// Textos del desplegable
const soloDesplegado = "whitespace-nowrap opacity-0 transition-opacity group-hover:opacity-100 group-focus-within:opacity-100";
const tituloDeGrupo = "h-0 overflow-hidden whitespace-nowrap px-3.5 text-xs font-semibold leading-4 text-text-secondary opacity-0 transition-all group-hover:h-6 group-hover:opacity-100 group-focus-within:h-6 group-focus-within:opacity-100";

type Props = {
    titulo?: string;
    onCerrarSesion: () => void;
    onVolver?: () => void;
    nombre?: string;
    correo?: string;
    children: ReactNode;
};

export default function AdminLayout({
    titulo,
    onCerrarSesion,
    onVolver,
    nombre = "Admin",
    correo = "admin@gmail.com",
    children,
}: Props) {
    const { pathname } = useLocation();
    const tituloAutomatico =
        GRUPOS.flatMap((g) => g.items).find((i) => pathname.startsWith(i.ruta))?.etiqueta ??
        "Bienvenido a NautiFlow";
    return (
        <div className="min-h-dvh bg-bg-fill-secondary font-inter">
            {/* Menú lateral desplegable por encima del contenido*/}
            <aside className="group fixed inset-y-0 left-0 z-20 w-16 overflow-hidden border-r border-border-secondary bg-bg-surface-primary transition-all duration-200 hover:w-60 hover:shadow-lg focus-within:w-60 focus-within:shadow-lg">
                <div className="flex items-center gap-3 py-5 pl-5">
                    <Logo className="h-6 w-auto shrink-0" />
                    <span className={`text-lg font-semibold leading-6 text-text-primary ${soloDesplegado}`}>
                        NautiFlow
                    </span>
                </div>

                <nav aria-label="Navegación principal" className="flex flex-col gap-2 px-2 pt-4">
                    {GRUPOS.map((grupo) => (
                        <div key={grupo.titulo} className="flex flex-col gap-1">
                            <p className={tituloDeGrupo}>{grupo.titulo}</p>

                            {grupo.items.map(({ ruta, etiqueta, Icono }) => (
                                <NavLink
                                    key={ruta}
                                    to={ruta}
                                    className={({ isActive }) =>
                                        `flex w-full items-center gap-3 rounded-m px-3.5 py-2 text-left text-sm leading-[22px] text-text-primary focus-visible:ring-2 focus-visible:ring-bg-fill-primary ${isActive ? "bg-bg-fill-primary/10 font-semibold" : "hover:bg-bg-surface-secondary"
                                        }`
                                    }
                                >
                                    <Icono className="h-5 w-5 shrink-0" aria-hidden="true" />
                                    <span className={soloDesplegado}>{etiqueta}</span>
                                </NavLink>
                            ))}
                        </div>
                    ))}
                </nav>
            </aside>

            <div className="flow-root pl-16">
                <header className="m-5 flex items-center justify-between gap-4 rounded-m bg-bg-fill-primary/10 px-6 py-4">
                    <div className="flex items-center gap-2">
                        {onVolver && (
                            <button type="button" onClick={onVolver} aria-label="Volver" className="rounded-sm text-text-primary focus-visible:ring-2 focus-visible:ring-bg-fill-primary">
                                <BsChevronLeft className="h-5 w-5" aria-hidden="true" />
                            </button>
                        )}
                        <h1 className="text-3xl font-bold leading-10 text-text-primary">{titulo ?? tituloAutomatico}</h1>
                    </div>

                    <div className="flex items-center gap-4">
                        <div className="text-right">
                            <p className="text-sm font-semibold leading-[22px] text-text-primary">{nombre}</p>
                            <p className="text-xs leading-4 text-text-secondary">{correo}</p>
                        </div>
                        <span className="flex h-10 w-10 items-center justify-center rounded-full bg-bg-fill-primary text-text-invert">
                            <BsPerson className="h-5 w-5" aria-hidden="true" />
                        </span>
                        <button type="button" onClick={onCerrarSesion}
                            className="flex items-center gap-2 rounded-full border border-border-info px-4 py-2 text-sm font-semibold leading-[22px] text-text-primary hover:bg-bg-fill-primary hover:text-text-invert focus-visible:ring-2 focus-visible:ring-bg-fill-primary"
                        >
                            <BsBoxArrowRight className="h-4 w-4" aria-hidden="true" />
                            Cerrar sesión
                        </button>
                    </div>
                </header>
                <main className="px-5 pb-5">{children}</main>
            </div>
        </div>
    );
}
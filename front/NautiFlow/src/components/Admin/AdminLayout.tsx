import type { CSSProperties, ReactNode } from "react";
import type { IconType } from "react-icons";
import { BsBoxArrowRight, BsChevronDown, BsFolderCheck, BsHouseDoor, BsList, BsPeople, BsPerson, BsSearch, BsX } from "react-icons/bs";
import { useEffect, useRef, useState } from "react";
import Logo from "../Logo";
import { NavLink, useLocation } from "react-router-dom";

export type Seccion = "buscador" | "viajes" | "solicitudes" | "lancheros";

const IconoRuta: IconType = ({ className }) => (
    <span
        className={className}
        aria-hidden="true"
        style={{
            backgroundColor: "currentColor",
            mask: "url('/icons/route.svg') center / contain no-repeat",
            WebkitMask: "url('/icons/route.svg') center / contain no-repeat",
        }}
    />
);
type Grupo = {
    titulo: string;
    items: { ruta: string; etiqueta: string; Icono: IconType }[];
};

const GRUPOS: Grupo[] = [
    {
        titulo: "General",
        items: [{ ruta: "", etiqueta: "Dashboard", Icono: BsHouseDoor }],
    },
    {
        titulo: "Gestión de viajes",
        items: [
            { ruta: "buscador", etiqueta: "Buscador", Icono: BsSearch },
            { ruta: "viajes", etiqueta: "Viajes", Icono: IconoRuta },
        ],
    },
    {
        titulo: "Gestión de la flota",
        items: [
            { ruta: "solicitudes", etiqueta: "Solicitudes", Icono: BsFolderCheck },
            { ruta: "lancheros", etiqueta: "Lancheros", Icono: BsPeople },
        ],
    },
];

type Props = {
    onCerrarSesion: () => void;
    nombre?: string;
    correo?: string;
    children: ReactNode;
};

export default function AdminLayout({
    onCerrarSesion,
    nombre = "Admin",
    correo = "admin@gmail.com",
    children,
}: Props) {
    const { pathname } = useLocation();
    const [menuHover, setMenuHover] = useState(false);
    const [menuFocus, setMenuFocus] = useState(false);
    const [menuMobileOpen, setMenuMobileOpen] = useState(false);
    const [perfilAbierto, setPerfilAbierto] = useState(false);
    const perfilRef = useRef<HTMLDivElement>(null);
    const menuExpandido = menuHover || menuFocus || menuMobileOpen;
    const anchoSidebar = menuExpandido ? "clamp(240px, 20vw, 280px)" : "clamp(56px, 4vw, 72px)";
    const basePath = pathname.startsWith("/admin") ? "/admin" : "/panel";
    const esDetalleSolicitud = /\/solicitudes\/[^/]+$/.test(pathname);
    const paginaActual = pathname === basePath ? "Dashboard" : (
        GRUPOS.flatMap((g) => g.items)
            .find((i) => i.ruta && pathname.startsWith(`${basePath}/${i.ruta}`))?.etiqueta ??
        "Bienvenido a NautiFlow"
    );

    useEffect(() => {
        const cerrarMenuMovil = (event: KeyboardEvent) => {
            if (event.key === "Escape") setMenuMobileOpen(false);
        };
        const cerrarMenuEnEscritorio = () => {
            if (window.matchMedia("(min-width: 768px)").matches) setMenuMobileOpen(false);
        };

        document.addEventListener("keydown", cerrarMenuMovil);
        window.addEventListener("resize", cerrarMenuEnEscritorio);
        return () => {
            document.removeEventListener("keydown", cerrarMenuMovil);
            window.removeEventListener("resize", cerrarMenuEnEscritorio);
        };
    }, []);
    useEffect(() => {
        if (!perfilAbierto) return;

        const cerrarAlHacerClickFuera = (event: PointerEvent) => {
            if (event.target instanceof Node && !perfilRef.current?.contains(event.target)) {
                setPerfilAbierto(false);
            }
        };
        const cerrarConEscape = (event: KeyboardEvent) => {
            if (event.key === "Escape") setPerfilAbierto(false);
        };

        document.addEventListener("pointerdown", cerrarAlHacerClickFuera);
        document.addEventListener("keydown", cerrarConEscape);
        return () => {
            document.removeEventListener("pointerdown", cerrarAlHacerClickFuera);
            document.removeEventListener("keydown", cerrarConEscape);
        };
    }, [perfilAbierto]);

    return (
        <div className="min-h-dvh bg-bg-fill-secondary font-inter" style={{ "--admin-sidebar-width": anchoSidebar } as CSSProperties}>
            {menuMobileOpen && (
                <button
                    type="button"
                    aria-label="Cerrar menu de navegacion"
                    className="fixed inset-0 z-20 bg-black/30 md:hidden"
                    onClick={() => setMenuMobileOpen(false)}
                />
            )}
            <aside
                id="admin-sidebar"
                className={`group fixed inset-y-0 left-0 z-30 overflow-hidden bg-[var(--bg-surface-primary,#FAFBFC)] transition-[width,transform] duration-200 md:translate-x-0 ${menuMobileOpen ? "translate-x-0" : "-translate-x-full"}`}
                style={{ width: menuMobileOpen ? "min(280px, 86vw)" : anchoSidebar }}
                onMouseEnter={() => {
                    if (window.matchMedia("(min-width: 768px)").matches) setMenuHover(true);
                }}
                onMouseLeave={() => setMenuHover(false)}
                onFocusCapture={() => {
                    if (window.matchMedia("(min-width: 768px)").matches) setMenuFocus(true);
                }}
                onBlurCapture={(event) => {
                    if (!(event.relatedTarget instanceof Node) || !event.currentTarget.contains(event.relatedTarget)) {
                        setMenuFocus(false);
                    }
                }}
            >
                <div className="flex h-16 items-center justify-center px-2 md:justify-start md:gap-3 md:px-4">
                    <Logo className="h-7 w-auto shrink-0" />
                    <span className={`${menuExpandido ? "inline" : "hidden"} whitespace-nowrap text-xl font-semibold leading-7 text-text-primary`}>
                        NautiFlow
                    </span>
                </div>

                <nav aria-label="Navegación principal" className={`flex flex-col px-1.5 pt-8 md:px-2 ${menuExpandido ? "gap-5" : "gap-2"}`}>
                    {GRUPOS.map((grupo) => (
                        <div key={grupo.titulo} className="flex flex-col gap-1">
                            <p className={`${menuExpandido ? "block" : "hidden"} whitespace-nowrap px-3.5 text-sm font-semibold leading-5 text-text-secondary md:mb-1`}>
                                {grupo.titulo}
                            </p>

                            {grupo.items.map(({ ruta, etiqueta, Icono }) => (
                                <NavLink
                                    key={ruta}
                                    to={ruta ? `${basePath}/${ruta}` : basePath}
                                    onClick={() => setMenuMobileOpen(false)}
                                    end={!ruta}
                                    title={etiqueta}
                                    aria-label={etiqueta}
                                    className={({ isActive }) =>
                                        `flex w-full items-center gap-3 rounded-m text-left text-base text-text-primary transition-colors focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-bg-fill-primary ${menuExpandido ? "min-h-11 justify-start px-3 leading-6" : "min-h-10 justify-center px-2 leading-5"} ${isActive ? "bg-[#205362] font-semibold text-white" : "hover:bg-bg-surface-secondary"}`
                                    }
                                >
                                    <Icono className="h-5 w-5 shrink-0" aria-hidden="true" />
                                    <span className={`${menuExpandido ? "inline" : "hidden"} whitespace-nowrap`}>
                                        {etiqueta}
                                    </span>
                                </NavLink>
                            ))}
                        </div>
                    ))}
                </nav>
            </aside>

            <div className="ml-0 min-h-dvh min-w-0 transition-[margin] duration-200 md:ml-[var(--admin-sidebar-width)]">
                <header className="sticky top-0 z-10 flex min-h-16 items-center justify-between gap-3 bg-[var(--bg-surface-white,#FFFFFF)] px-3 sm:gap-4 sm:px-7 lg:px-10">
                    <button
                        type="button"
                        aria-label={menuMobileOpen ? "Cerrar menu" : "Abrir menu"}
                        aria-controls="admin-sidebar"
                        aria-expanded={menuMobileOpen}
                        onClick={() => setMenuMobileOpen((open) => !open)}
                        className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full text-text-primary transition-colors hover:bg-bg-surface-secondary focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-bg-fill-primary md:hidden"
                    >
                        {menuMobileOpen ? <BsX className="h-6 w-6" aria-hidden="true" /> : <BsList className="h-6 w-6" aria-hidden="true" />}
                    </button>
                    {esDetalleSolicitud ? (
                        <nav aria-label="Ruta de navegación" className="min-w-0">
                            <ol className="flex flex-wrap items-center gap-x-2 text-sm sm:text-base">
                                <li>
                                    <NavLink to={`${basePath}/solicitudes`} className="font-medium text-text-secondary hover:text-text-primary">
                                        Solicitudes
                                    </NavLink>
                                </li>
                                <li aria-hidden="true" className="text-text-secondary">/</li>
                                <li aria-current="page" className="truncate font-semibold text-text-primary">
                                    Detalle solicitud
                                </li>
                            </ol>
                        </nav>
                    ) : pathname !== basePath ? (
                        <nav aria-label="Ruta de navegación" className="min-w-0">
                            <ol className="flex flex-wrap items-center gap-x-2 text-sm sm:text-base">
                                <li>
                                    <NavLink to={`${basePath}`} className="font-medium text-text-secondary hover:text-text-primary">
                                        Dashboard admin
                                    </NavLink>
                                </li>
                                <li aria-hidden="true" className="text-text-secondary">-</li>
                                <li aria-current="page" className="truncate font-semibold text-text-primary">
                                    Panel de {paginaActual}
                                </li>
                            </ol>
                        </nav>
                    ) : pathname === basePath ? (
                        <h1 className="min-w-0 flex-1 truncate text-lg font-bold leading-6 text-text-primary sm:text-xl lg:text-2xl">Bienvenido a NautiFlow</h1>
                    ) : (
                        <div aria-hidden="true" className="min-w-0" />
                    )}

                    <div className="relative shrink-0" ref={perfilRef}>
                        <button
                            type="button"
                            onClick={() => setPerfilAbierto((abierto) => !abierto)}
                            aria-label={`Perfil de ${nombre}`}
                            aria-haspopup="true"
                            aria-expanded={perfilAbierto}
                            aria-controls="admin-profile-menu"
                            className="flex min-h-11 items-center gap-2 rounded-full px-2 transition-colors hover:bg-bg-surface-secondary focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-bg-fill-primary sm:gap-3 sm:px-3"
                        >
                            <span className="hidden text-right sm:block">
                                <span className="block text-sm font-semibold leading-5 text-text-primary">{nombre}</span>
                                <span className="block text-xs leading-4 text-text-secondary">{correo}</span>
                            </span>
                            <span className="flex h-10 w-10 items-center justify-center rounded-full bg-bg-fill-primary/10 text-bg-fill-primary">
                                <BsPerson className="h-5 w-5" aria-hidden="true" />
                            </span>
                            <BsChevronDown
                                className={`h-3.5 w-3.5 text-text-secondary transition-transform ${perfilAbierto ? "rotate-180" : ""}`}
                                aria-hidden="true"
                            />
                        </button>
                        {perfilAbierto && (
                            <div
                                id="admin-profile-menu"
                                className="absolute right-0 top-full z-30 mt-2 w-64 rounded-m border border-border-secondary bg-white p-2 shadow-lg"
                            >
                                <div className="border-b border-border-secondary px-3 py-2">
                                    <p className="text-sm font-semibold leading-5 text-text-primary">{nombre}</p>
                                    <p className="break-all text-xs leading-4 text-text-secondary">{correo}</p>
                                </div>
                                <button
                                    type="button"
                                    onClick={() => {
                                        setPerfilAbierto(false);
                                        onCerrarSesion();
                                    }}
                                    className="mt-1 flex min-h-10 w-full items-center gap-2 rounded-m px-3 text-left text-sm font-semibold text-text-primary transition-colors hover:bg-bg-surface-secondary focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-bg-fill-primary"
                                >
                                    <BsBoxArrowRight className="h-4 w-4" aria-hidden="true" />
                                    Cerrar sesión
                                </button>
                            </div>
                        )}
                    </div>
                </header>
                <main className="min-w-0 px-5 pb-8 sm:px-7 lg:px-10">{children}</main>
            </div>
        </div>
    );
}

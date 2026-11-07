import { useEffect, useState } from "react";
import Logo from "../Logo";

export default function LandingHeader() {
    // Header sticky (comentario #8 en Figma): transparente sobre el hero, con fondo al hacer scroll
    const [conFondo, setConFondo] = useState(() => window.scrollY > 24);

    useEffect(() => {
        const actualizar = () => setConFondo(window.scrollY > 24);
        window.addEventListener("scroll", actualizar, { passive: true });
        return () => window.removeEventListener("scroll", actualizar);
    }, []);

    return (
        <header
            className={`fixed inset-x-0 top-0 z-50 font-inter transition-colors duration-300 ${conFondo ? "bg-bg-surface-primary/90 shadow backdrop-blur" : "bg-transparent"}`}
        >
            <div className={`flex items-center justify-between px-4 transition-[padding] duration-300 md:px-[60px] ${conFondo ? "py-3" : "pt-[31px]"}`}>
                <a href="/" aria-label="NautiFlow, ir al inicio" className="rounded-sm focus-visible:ring-2 focus-visible:ring-bg-fill-primary">
                    <Logo className={`w-auto transition-[height] duration-300 ${conFondo ? "h-10" : "h-16 md:h-14"}`} />
                </a>

                <a href="/registro"
                    className="inline-flex h-10 items-center rounded-full bg-bg-fill-primary px-4 text-sm font-medium leading-5 text-text-invert hover:bg-bg-fill-primary-hover focus-visible:ring-2 focus-visible:ring-text-invert md:h-12 md:text-lg md:leading-7"
                >
                    Registrá tu lancha
                </a>
            </div>
        </header>
    );
}

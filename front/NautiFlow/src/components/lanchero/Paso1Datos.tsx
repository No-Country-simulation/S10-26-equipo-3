import type { SyntheticEvent } from "react";
import { BsChevronLeft } from "react-icons/bs";
import type { DatosLanchero } from "./types";
import Logo from "../Logo";
import Input from "./Input";
import IndicadorPasos from "./IndicadorPasos";

type Props = {
    datos: DatosLanchero;
    onChange: (cambios: Partial<DatosLanchero>) => void;
    onSiguiente: () => void;
    onAtras: () => void;
};

export default function Paso1Datos({ datos, onChange, onSiguiente, onAtras }: Props) {
    const manejarEnvio = (e: SyntheticEvent) => {
        e.preventDefault();
        onSiguiente();
    };

    return (
        <form onSubmit={manejarEnvio} className="flex flex-1 flex-col gap-6 font-inter md:gap-8">
            <header className="flex items-center gap-3 md:gap-4">
                <button type="button" onClick={onAtras} aria-label="Volver" className="rounded-sm text-text-primary focus-visible:ring-2 focus-visible:ring-bg-fill-primary">
                    <BsChevronLeft className="h-5 w-5" aria-hidden="true" />
                </button>
                <Logo className="md:h-16 md:w-auto" />
                <span className="hidden text-2xl font-medium leading-8 text-text-primary md:block">
                    NautiFlow
                </span>
            </header>

            <div className="flex flex-col gap-6 md:mx-auto md:w-full md:max-w-3xl md:gap-4">
                <IndicadorPasos paso={1} />

                <div className="flex flex-col gap-4">
                    <Input
                        label="Nombre completo"
                        type="text"
                        placeholder="Ej. Juan Pérez"
                        autoComplete="name"
                        required
                        value={datos.nombreCompleto}
                        onChange={(e) => onChange({ nombreCompleto: e.target.value })}
                    />
                    <Input
                        label="WhatsApp"
                        type="tel"
                        inputMode="numeric"
                        placeholder="9 11 1234 5678"
                        autoComplete="tel"
                        maxLength={15}
                        required
                        value={datos.whatsapp}
                        onChange={(e) => onChange({ whatsapp: e.target.value.replace(/\D/g, "") })}
                    />
                    <Input
                        label="Correo electrónico"
                        type="email"
                        placeholder="example@mail.com"
                        autoComplete="email"
                        required
                        value={datos.email}
                        onChange={(e) => onChange({ email: e.target.value })}
                    />
                </div>
            </div>

            <button type="submit" className="mt-auto w-full rounded-full bg-bg-fill-primary py-3 text-base font-semibold leading-[22px] text-text-invert hover:bg-bg-fill-primary-hover md:mx-auto md:mt-0 md:w-80">
                Siguiente
            </button>
        </form>
    );
}
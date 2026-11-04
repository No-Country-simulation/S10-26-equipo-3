import type { SyntheticEvent } from "react";
import { BsChevronLeft } from "react-icons/bs";
import type { DatosLanchero } from "./types";
import { PREFIJO_CELULAR, LARGO_CELULAR } from "./types";
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
                    <div className="flex flex-col gap-1">
                        <label htmlFor="celular" className="text-sm font-semibold leading-[22px] text-text-primary">
                            Celular
                        </label>
                        <div className="flex w-full overflow-hidden rounded-m border border-border-secondary bg-bg-surface-primary focus-within:ring-2 focus-within:ring-bg-fill-primary">
                            <span className="flex items-center border-r border-border-secondary bg-bg-surface-secondary px-3 text-sm font-semibold leading-[22px] text-text -primary">
                                {PREFIJO_CELULAR}
                            </span>
                            <input
                                id="celular"
                                type="tel"
                                inputMode="numeric"
                                autoComplete="tel-national"
                                placeholder="11 1234 5678"
                                maxLength={LARGO_CELULAR}
                                pattern={`[0-9]{${LARGO_CELULAR}}`}
                                title={`Ingresa los ${LARGO_CELULAR} dígitos de tu celular, sin 0 ni 15`}
                                required
                                value={datos.whatsapp}
                                onChange={(e) =>
                                    onChange({ whatsapp: e.target.value.replace(/\D/g, "").slice(0, LARGO_CELULAR) })
                                }
                                className="min-w-0 flex-1 bg-transparent px-3 py-2 text-sm leading-[22px] text-text-primary outline-none placeholder:text-text-disable"
                            />
                        </div>
                        <p className="text-xs leading-4 text-text-secondary">
                            Sin 0 ni 15. Ejemplo: 11 1234 5678
                        </p>
                    </div>
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
import { useState } from "react";
import type { ChangeEvent, SyntheticEvent } from "react";
import { BsCheckCircleFill, BsChevronLeft, BsCloudUpload, BsFileEarmarkImage, BsFileEarmarkPdf, BsXLg, } from "react-icons/bs";
import type { DatosLanchero } from "./types";
import Logo from "../Logo";
import IndicadorPasos from "./IndicadorPasos";

const MAX_MB = 5;
const MAX_BYTES = MAX_MB * 1024 * 1024;
const TIPOS_PERMITIDOS = ["application/pdf", "image/jpeg", "image/png", "image/webp"];

type Documento = "licencia" | "seguro" | "antecedentes";

const DOCUMENTOS: { campo: Documento; etiqueta: string }[] = [
    { campo: "licencia", etiqueta: "Licencia" },
    { campo: "seguro", etiqueta: "Seguro" },
    { campo: "antecedentes", etiqueta: "Antecedentes penales" },
];

type Props = {
    datos: DatosLanchero;
    onChange: (cambios: Partial<DatosLanchero>) => void;
    onFinalizar: () => void;
    onAtras: () => void;
};

type CampoArchivoProps = {
    id: string;
    etiqueta: string;
    archivo: File | null;
    error?: string;
    onSeleccionar: (archivo: File) => void;
    onQuitar: () => void;
};

function CampoArchivo({ id, etiqueta, archivo, error, onSeleccionar, onQuitar }: CampoArchivoProps) {
    const manejarCambio = (e: ChangeEvent<HTMLInputElement>) => {
        const elegido = e.target.files?.[0];
        e.target.value = "";
        if (elegido) onSeleccionar(elegido);
    };

    const IconoArchivo = archivo?.type === "application/pdf" ? BsFileEarmarkPdf : BsFileEarmarkImage;

    let estilo = "border-dashed border-border-quartery bg-bg-surface-primary hover:bg-bg-surface-secondary";
    if (archivo) estilo = "border-solid border-border-info bg-bg-surface-primary pr-10";
    if (error) estilo = "border-dashed border-border-error bg-bg-surface-primary hover:bg-bg-surface-secondary";

    return (
        <div className="flex flex-col gap-1">
            <span className="text-sm font-semibold leading-[22px] text-text-primary">{etiqueta}</span>

            <div className="relative">
                <label htmlFor={id}
                    className={`flex h-10 cursor-pointer items-center justify-center gap-2 rounded-m border px-3 text-sm leading-[22px] text-text-primary focus-within:ring-2 focus-within:ring-bg-fill-primary ${estilo}`}
                >
                    {archivo ? (
                        <>
                            <IconoArchivo className="h-5 w-5 shrink-0 text-bg-fill-primary" aria-hidden="true" />
                            <span className="truncate">{archivo.name}</span>
                            <BsCheckCircleFill className="h-4 w-4 shrink-0 text-bg-success-primary" aria-hidden="true" />
                        </>
                    ) : (
                        <>
                            <BsCloudUpload className="h-5 w-5 shrink-0 text-bg-fill-primary" aria-hidden="true" />
                            <span className="font-semibold">Subir archivo</span>
                        </>
                    )}
                    <input id={id} type="file" accept={TIPOS_PERMITIDOS.join(",")} onChange={manejarCambio} className="sr-only" />
                </label>

                {archivo && (
                    <button type="button" onClick={onQuitar} aria-label={`Quitar ${etiqueta}`}
                        className="absolute right-2 top-1/2 flex h-6 w-6 -translate-y-1/2 items-center justify-center rounded-full text-text-secondary hover:bg-bg-surface-secondary hover:text-text-primary focus-visible:ring-2 focus-visible:ring-bg-fill-primary"
                    >
                        <BsXLg className="h-3 w-3" aria-hidden="true" />
                    </button>
                )}
            </div>

            {error && (
                <p role="alert" className="text-xs font-semibold leading-4 text-text-error">
                    {error}
                </p>
            )}
        </div>
    );
}

export default function Paso3Documentos({ datos, onChange, onFinalizar, onAtras }: Props) {
    const [errores, setErrores] = useState<Partial<Record<Documento, string>>>({});

    const seleccionar = (campo: Documento, archivo: File) => {
        let error = "";
        if (!TIPOS_PERMITIDOS.includes(archivo.type)) {
            error = "Solo se permiten archivos PDF, JPG, PNG o WEBP.";
        } else if (archivo.size > MAX_BYTES) {
            error = `El archivo pesa más de ${MAX_MB} MB.`;
        }

        setErrores((prev) => ({ ...prev, [campo]: error }));
        onChange({ [campo]: error ? null : archivo } as Partial<DatosLanchero>);
    };

    const quitar = (campo: Documento) => {
        setErrores((prev) => ({ ...prev, [campo]: "" }));
        onChange({ [campo]: null } as Partial<DatosLanchero>);
    };

    const manejarEnvio = (e: SyntheticEvent) => {
        e.preventDefault();

        const faltantes = DOCUMENTOS.filter(({ campo }) => !datos[campo]);
        if (faltantes.length > 0) {
            setErrores((prev) => {
                const nuevos = { ...prev };
                faltantes.forEach(({ campo }) => {
                    nuevos[campo] = nuevos[campo] || "Este documento es obligatorio.";
                });
                return nuevos;
            });
            return;
        }

        onFinalizar();
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
                <IndicadorPasos paso={3} />

                <div className="flex flex-col gap-4">
                    {DOCUMENTOS.map(({ campo, etiqueta }) => (
                        <CampoArchivo
                            key={campo}
                            id={`documento-${campo}`}
                            etiqueta={etiqueta}
                            archivo={datos[campo]}
                            error={errores[campo]}
                            onSeleccionar={(archivo) => seleccionar(campo, archivo)}
                            onQuitar={() => quitar(campo)}
                        />
                    ))}
                    <p className="text-xs leading-4 text-text-secondary">
                        PDF, JPG, PNG o WEBP. Máximo {MAX_MB} MB por archivo.
                    </p>
                </div>
            </div>

            <button type="submit" className="mt-auto w-full rounded-full bg-bg-fill-primary py-3 text-base font-semibold leading-[22px] text-text-invert hover:bg-bg-fill-primary-hover md:mx-auto md:mt-0 md:w-80">
                Finalizar
            </button>
        </form>
    );
}
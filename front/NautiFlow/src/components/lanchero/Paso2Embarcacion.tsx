import { useEffect, useState } from "react";
import type { ChangeEvent, SyntheticEvent } from "react";
import { BsCheckLg, BsChevronLeft, BsCloudUpload, BsDash, BsPlus, BsXLg, } from "react-icons/bs";
import type { DatosLanchero } from "./types";
import Logo from "../Logo";
import Input from "./Input";
import IndicadorPasos from "./IndicadorPasos";

const MAX_IMAGENES = 5;
const MAX_MB = 5;
const MAX_BYTES = MAX_MB * 1024 * 1024;
const TIPOS_PERMITIDOS = ["image/jpeg", "image/png", "image/webp"];
const ZONAS = ["Primera sección", "Segunda sección", "Tercera sección"];

type Props = {
    datos: DatosLanchero;
    onChange: (cambios: Partial<DatosLanchero>) => void;
    onSiguiente: () => void;
    onAtras: () => void;
};

type MiniaturaProps = {
    archivo: File;
    onQuitar: () => void;
};

function Miniatura({ archivo, onQuitar }: MiniaturaProps) {
    const [url, setUrl] = useState("");

    useEffect(() => {
        const nuevaUrl = URL.createObjectURL(archivo);
        setUrl(nuevaUrl);
        return () => URL.revokeObjectURL(nuevaUrl);
    }, [archivo]);

    return (
        <div className="relative aspect-square overflow-hidden rounded-m bg-bg-surface-secondary">
            {url && <img src={url} alt={archivo.name} className="h-full w-full object-cover" />}
            <button type="button" onClick={onQuitar} aria-label={`Quitar ${archivo.name}`}
                className="absolute right-1 top-1 flex h-6 w-6 items-center justify-center rounded-full bg-bg-surface-primary text-text-secondary shadow hover:text-text-primary focus-visible:ring-2 focus-visible:ring-bg-fill-primary"
            >
                <BsXLg className="h-3 w-3" aria-hidden="true" />
            </button>
        </div>
    );
}

export default function Paso2Embarcacion({ datos, onChange, onSiguiente, onAtras }: Props) {
    const [errorImagenes, setErrorImagenes] = useState("");
    const [errorForm, setErrorForm] = useState("");

    const actualizar = (cambios: Partial<DatosLanchero>) => {
        setErrorForm("");
        onChange(cambios);
    };

    const cambiarCapacidad = (delta: number) => actualizar({ capacidad: Math.max(0, datos.capacidad + delta) });

    const alternarZona = (zona: string) => {
        const nuevas = datos.zonas.includes(zona) ? datos.zonas.filter((z) => z !== zona) : [...datos.zonas, zona];
        actualizar({ zonas: nuevas });
    };

    const agregarImagenes = (e: ChangeEvent<HTMLInputElement>) => {
        const seleccionados = Array.from(e.target.files ?? []);
        e.target.value = "";

        const errores: string[] = [];
        const validos: File[] = [];

        for (const archivo of seleccionados) {
            if (!TIPOS_PERMITIDOS.includes(archivo.type)) {
                errores.push(`${archivo.name}: solo se permiten imágenes JPG, PNG o WEBP.`);
            } else if (archivo.size > MAX_BYTES) {
                errores.push(`${archivo.name}: pesa más de ${MAX_MB} MB.`);
            } else {
                validos.push(archivo);
            }
        }

        const espacio = MAX_IMAGENES - datos.imagenes.length;
        if (validos.length > espacio) {
            errores.push(`Solo puedes subir hasta ${MAX_IMAGENES} imágenes.`);
        }

        actualizar({ imagenes: [...datos.imagenes, ...validos.slice(0, espacio)] });
        setErrorImagenes(errores.join(" "));
    };

    const quitarImagen = (indice: number) => {
        actualizar({ imagenes: datos.imagenes.filter((_, i) => i !== indice) });
        setErrorImagenes("");
    };

    const manejarEnvio = (e: SyntheticEvent) => {
        e.preventDefault();

        let mensaje = "";
        if (datos.capacidad < 1) mensaje = "Indica la capacidad de tu embarcación.";
        else if (datos.zonas.length === 0) mensaje = "Selecciona al menos una zona de cobertura.";
        else if (datos.imagenes.length === 0) mensaje = "Sube al menos una imagen de tu embarcación.";

        setErrorForm(mensaje);
        if (!mensaje) onSiguiente();
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
                <IndicadorPasos paso={2} />

                <div className="flex flex-col gap-4">
                    <Input
                        label="Nombre de tu embarcación"
                        type="text"
                        placeholder="Ej. Mar Azul"
                        required
                        value={datos.nombreEmbarcacion}
                        onChange={(e) => actualizar({ nombreEmbarcacion: e.target.value })}
                    />

                    <div className="flex flex-col gap-1">
                        <span className="text-sm font-semibold leading-[22px] text-text-primary">Capacidad</span>
                        <div role="group" aria-label="Capacidad" className="flex items-center justify-between rounded-m border border-border-secondary bg-bg-surface-primary">

                            <button type="button" onClick={() => cambiarCapacidad(-1)} disabled={datos.capacidad === 0} aria-label="Disminuir capacidad"
                                className="flex h-10 w-12 items-center justify-center rounded-m text-text-primary hover:text-bg-fill-primary focus-visible:ring-2 focus-visible:ring-inset focus-visible:ring-bg-fill-primary disabled:cursor-not-allowed disabled:text-text-disable disabled:hover:text-text-disable"
                            >
                                <BsDash className="h-6 w-6" aria-hidden="true" />
                            </button>
                            <span aria-live="polite" className="text-sm text-text-secondary">
                                {datos.capacidad}
                            </span>
                            <button type="button" onClick={() => cambiarCapacidad(1)} aria-label="Aumentar capacidad"
                                className="flex h-10 w-12 items-center justify-center rounded-m text-text-primary hover:text-bg-fill-primary focus-visible:ring-2 focus-visible:ring-inset focus-visible:ring-bg-fill-primary"
                            >
                                <BsPlus className="h-6 w-6" aria-hidden="true" />
                            </button>
                        </div>
                    </div>

                    <fieldset className="flex flex-col gap-1">
                        <legend className="mb-1 text-sm font-semibold leading-[22px] text-text-primary">
                            Zonas de cobertura
                        </legend>
                        <div className="flex flex-col gap-2 rounded-m border border-border-secondary bg-bg-surface-primary p-3">
                            {ZONAS.map((zona) => {
                                const marcada = datos.zonas.includes(zona);
                                return (
                                    <label key={zona} className="flex cursor-pointer items-center gap-2 text-sm leading-[22px] text-text-primary">
                                        <input type="checkbox" checked={marcada} onChange={() => alternarZona(zona)} className="peer sr-only" />
                                        <span
                                            className={`flex h-5 w-5 shrink-0 items-center justify-center rounded-sm border text-text-invert peer-focus-visible:ring-2 peer-focus-visible:ring-bg-fill-primary peer-focus-visible:ring-offset-1 ${marcada
                                                ? "border-bg-fill-primary bg-bg-fill-primary"
                                                : "border-border-tertiary bg-bg-surface-primary"
                                                }`}
                                        >
                                            {marcada && <BsCheckLg className="h-3 w-3" aria-hidden="true" />}
                                        </span>
                                        {zona}
                                    </label>
                                );
                            })}
                        </div>
                    </fieldset>

                    <div className="flex flex-col gap-2">
                        <div className="flex items-center justify-between">
                            <span className="text-sm font-semibold leading-[22px] text-text-primary">
                                Imágenes de tu embarcación
                            </span>
                            <span className="text-xs font-semibold leading-4 text-text-secondary">
                                {datos.imagenes.length}/{MAX_IMAGENES}
                            </span>
                        </div>

                        <div className="grid grid-cols-3 gap-2 md:grid-cols-5 md:gap-4">
                            {datos.imagenes.length < MAX_IMAGENES && (
                                <label className="flex aspect-square cursor-pointer flex-col items-center justify-center gap-1 rounded-m bg-bg-surface-secondary p-2 text-center text-xs font-semibold leading-4 text-text-primary hover:bg-bg-surface-tertiary focus-within:ring-2 focus-within:ring-bg-fill-primary">
                                    <BsCloudUpload className="h-6 w-6 text-bg-fill-primary" aria-hidden="true" />
                                    Subir archivos
                                    <input type="file" accept={TIPOS_PERMITIDOS.join(",")} multiple onChange={agregarImagenes} className="sr-only" />
                                </label>
                            )}

                            {datos.imagenes.map((archivo, i) => (
                                <Miniatura key={`${archivo.name}-${archivo.lastModified}-${i}`} archivo={archivo} onQuitar={() => quitarImagen(i)} />
                            ))}
                        </div>

                        <p className="text-xs leading-4 text-text-secondary">
                            JPG, PNG o WEBP. Máximo {MAX_MB} MB por imagen.
                        </p>
                        {errorImagenes && (
                            <p role="alert" className="text-xs font-semibold leading-4 text-text-error">
                                {errorImagenes}
                            </p>
                        )}
                    </div>
                </div>
            </div>

            <div className="mt-auto flex flex-col gap-2 md:mx-auto md:mt-0 md:w-80">
                {errorForm && (
                    <p role="alert" className="text-center text-sm font-semibold text-text-error">
                        {errorForm}
                    </p>
                )}
                <button type="submit" className="w-full rounded-full bg-bg-fill-primary py-3 text-base font-semibold leading-[22px] text-text-invert hover:bg-bg-fill-primary-hover">
                    Siguiente
                </button>
            </div>
        </form>
    );
}
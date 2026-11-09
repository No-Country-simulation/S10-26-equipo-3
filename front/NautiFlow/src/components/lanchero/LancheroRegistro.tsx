import { useState } from "react";
import type { DatosLanchero } from "./types";
import { datosIniciales, PREFIJO_CELULAR } from "./types";
import LancheroIntro from "./LancheroIntro";
import Paso1Datos from "./Paso1Datos";
import Paso2Embarcacion from "./Paso2Embarcacion";
import Paso3Documentos from "./Paso3Documentos";
import LancheroConfirmacion from "../LancheroConfirmacion/LancheroConfirmacion";
import { useNavigate } from "react-router-dom";

import { API_BASE_URL } from "../../config/api";

const ZONAS_IDS: Record<string, number> = {
    "Primera sección": 1,
    "Segunda sección": 2,
    "Tercera sección": 3,
};

export default function LancheroRegistro() {
    const [paso, setPaso] = useState(0);
    const navigate = useNavigate();
    const [datos, setDatos] = useState<DatosLanchero>(datosIniciales);
    const [enviando, setEnviando] = useState(false);
    const [errorEnvio, setErrorEnvio] = useState("");

    const actualizar = (cambios: Partial<DatosLanchero>) =>
        setDatos((prev) => ({ ...prev, ...cambios }));

    const siguiente = () => setPaso((p) => p + 1);
    const atras = () => setPaso((p) => p - 1);

    const finalizar = async () => {
        if (!datos.licencia || !datos.seguro || !datos.antecedentes) return;

        setEnviando(true);
        setErrorEnvio("");

        try {
            const formData = new FormData();
            formData.append(
                "solicitud",
                JSON.stringify({
                    nombreCompleto: datos.nombreCompleto,
                    whatsapp: `${PREFIJO_CELULAR}${datos.whatsapp}`,
                    correo: datos.email,
                    nombreEmbarcacion: datos.nombreEmbarcacion,
                    capacidadPasajeros: datos.capacidad,
                    zonasIds: datos.zonas.map((zona) => ZONAS_IDS[zona]),
                }),
            );
            formData.append("licencia", datos.licencia);
            formData.append("seguro", datos.seguro);
            formData.append("antecedentes", datos.antecedentes);
            datos.imagenes.forEach((foto) => formData.append("fotos", foto));

            const respuesta = await fetch(`${API_BASE_URL}/lancheros/registrar`, {
                method: "POST",
                body: formData,
            });

            if (!respuesta.ok) {
                const error = await respuesta.json().catch(() => null);
                throw new Error(error?.message ?? "No se pudo completar el registro. Intenta de nuevo.");
            }

            setPaso(4);
        } catch (e) {
            setErrorEnvio(
                e instanceof TypeError
                    ? "No se pudo conectar con el servidor. Revisa tu conexión e intenta de nuevo."
                    : e instanceof Error
                        ? e.message
                        : "No se pudo completar el registro.",
            );
        } finally {
            setEnviando(false);
        }
    };
    const volverInicio = () => {
        navigate("/");
    };

    return (
        <div className="mx-auto flex w-full max-w-md flex-1 flex-col md:max-w-none">
            {paso === 0 && <LancheroIntro onContinuar={siguiente} />}

            {paso === 1 && (<Paso1Datos datos={datos} onChange={actualizar} onSiguiente={siguiente} onAtras={atras} />)}

            {paso === 2 && (<Paso2Embarcacion datos={datos} onChange={actualizar} onSiguiente={siguiente} onAtras={atras} />)}

            {paso === 3 && (<Paso3Documentos datos={datos} onChange={actualizar} onFinalizar={finalizar} onAtras={atras} enviando={enviando} errorEnvio={errorEnvio} />)}

            {paso === 4 && <LancheroConfirmacion finalizar={volverInicio} />}

        </div>
    );
}
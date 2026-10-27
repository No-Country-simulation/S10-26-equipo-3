import { useState } from "react";
import type { DatosLanchero } from "./types";
import { datosIniciales } from "./types";
import LancheroIntro from "./LancheroIntro";
import Paso1Datos from "./Paso1Datos";
import Paso2Embarcacion from "./Paso2Embarcacion";
import Paso3Documentos from "./Paso3Documentos";
import LancheroConfirmacion from "../LancheroConfirmacion";

export default function LancheroRegistro() {
    // 0 = intro, 1-3 = pasos, 4 = confirmación
    const [paso, setPaso] = useState(0);
    const [datos, setDatos] = useState<DatosLanchero>(datosIniciales);

    const actualizar = (cambios: Partial<DatosLanchero>) =>
        setDatos((prev) => ({ ...prev, ...cambios }));

    const siguiente = () => setPaso((p) => p + 1);
    const atras = () => setPaso((p) => p - 1);

    const finalizar = () => {
        console.log("Datos del lanchero:", datos); // envio a backend
        setPaso(4);
    };

    return (
        <div className="mx-auto flex w-full max-w-md flex-1 flex-col md:max-w-none">
            {paso === 0 && <LancheroIntro onContinuar={siguiente} />}

            {paso === 1 && ( <Paso1Datos datos={datos} onChange={actualizar} onSiguiente={siguiente} onAtras={atras} />)}

            {paso === 2 && ( <Paso2Embarcacion datos={datos} onChange={actualizar} onSiguiente={siguiente} onAtras={atras} /> )}

            {paso === 3 && ( <Paso3Documentos datos={datos} onChange={actualizar} onFinalizar={finalizar} onAtras={atras} />)}
            
            {paso === 4 && <LancheroConfirmacion />}
        </div>
    );
}
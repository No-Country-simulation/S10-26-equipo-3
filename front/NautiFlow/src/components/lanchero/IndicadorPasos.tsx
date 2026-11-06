import { Fragment } from "react";

type Props = { paso: 1 | 2 | 3; };

const etiquetas = ["Tus datos", "Embarcación", "Documentos"];

export default function IndicadorPasos({ paso }: Props) {
    return (
        <div className="flex items-start justify-between rounded-m bg-bg-fill-primary/10 px-4 py-4 md:px-6">
            {etiquetas.map((etiqueta, i) => {
                const numero = i + 1;
                const activo = numero === paso;

                return (
                    <Fragment key={etiqueta}>
                        <div className="flex flex-col items-center gap-2">
                            <span className={`flex h-11 w-11 items-center justify-center rounded-full text-sm font-semibold leading-none ${activo
                                ? "bg-bg-fill-primary text-text-invert" : "bg-bg-surface-secondary text-text-primary"}`}
                            >
                                {numero}
                            </span>
                            <span className="text-sm font-semibold leading-[22px] text-text-primary">
                                {etiqueta}
                            </span>
                        </div>

                        {numero < 3 && (
                            <span aria-hidden="true" className="mt-5 h-1 w-6 rounded-full bg-bg-surface-secondary md:mx-4 md:mt-[21px] md:h-0.5 md:flex-1"/>
                        )}
                    </Fragment>
                );
            })}
        </div>
    );
}
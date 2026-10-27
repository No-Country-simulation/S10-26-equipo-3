import Logo from "../Logo";
import { BsExclamationCircle } from "react-icons/bs";

type Props = { onContinuar: () => void; };

export default function LancheroIntro({ onContinuar }: Props) {
    return (
        <section className="flex flex-1 flex-col justify-between font-inter md:justify-start md:gap-8">
            <header className="flex items-center gap-3 md:gap-4">
                <Logo className="md:h-16 md:w-auto" />
                <span className="hidden text-2xl font-medium leading-8 text-text-primary md:block">
                    NautiFlow
                </span>
            </header>

            <div className="flex flex-col gap-4 md:mx-auto md:mt-auto md:w-full md:max-w-xl">
                <p className="text-xs font-semibold uppercase leading-4 text-text-primary">
                    Registro de lancheros
                </p>

                <h1 className="text-2xl font-bold leading-[30px] text-text-primary">
                    Registra tu lancha y comienza a recibir solicitudes
                </h1>

                <p className="text-sm leading-[22px] text-text-secondary">
                    Completa tus datos y los de tu embarcación para que los turistas
                    puedan encontrar tus servicios.
                </p>

                <div className="flex items-start gap-3 rounded-m bg-bg-surface-tertiary p-3">
                    <BsExclamationCircle aria-hidden="true" className="h-5 w-5 shrink-0 text-bg-fill-primary" />
                    <p className="text-xs font-semibold leading-4 text-text-primary">
                        Ten tus documentos a mano antes de comenzar (Fotos, licencia,
                        seguro y antecedentes penales)
                    </p>
                </div>
            </div>

            <div className="flex flex-col items-center gap-4 md:mx-auto md:mb-auto md:w-80">
                <button type="button" onClick={onContinuar}
                    className="w-full rounded-full bg-bg-fill-primary py-3 text-base font-semibold leading-[22px] text-text-invert hover:bg-bg-fill-primary-hover"
                >
                    Continuar
                </button>

                <p className="text-xs leading-4 text-text-secondary md:hidden"> ¿Ya tienes cuenta?{" "}
                    <a href="#" className="font-semibold text-text-primary"> Iniciar sesión </a>
                </p>
            </div>
        </section>
    );
}
import LandingHeader from "./LandingHeader";
import Hero from "./Hero";

export default function Landing() {
    return (
        <>
            <LandingHeader />
            <main>
                <Hero />

                {/* Destino de "Explorar lanchas": el listado de lanchas verificadas se implementa en HU-20 */}
                <section id="lanchas" className="min-h-dvh scroll-mt-18 bg-bg-surface-primary px-4 py-16 font-inter md:px-[60px]">
                    <h2 className="text-2xl font-bold leading-[30px] text-text-primary">Lanchas verificadas</h2>
                </section>
            </main>
        </>
    );
}

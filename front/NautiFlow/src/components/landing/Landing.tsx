import LandingHeader from "./LandingHeader";
import Hero from "./Hero";
import LanchasDestacadasSection from "./LanchasDestacadasSection";
import ComoFuncionaSection from "./ComoFuncionaSection";
import DestinosSection from "./DestinosSection";
import RegistroLanchaSection from "./RegistroLanchaSection";
import LandingFooter from "./LandingFooter";

export default function Landing() {
    return (
        <>
            <LandingHeader />
            <main>
                <Hero />

                <LanchasDestacadasSection/>
                <ComoFuncionaSection/>
                <DestinosSection/>
                <RegistroLanchaSection/>
                <LandingFooter/>

            </main>

        </>
    );
}

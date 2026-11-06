import HeroSection from "./HeroSection";
import LanchasDestacadasSection from "./LanchasDestacadasSection";
import ComoFuncionaSection from "./ComoFuncionaSection";
import DestinosSection from "./DestinosSection";
import RegistroLanchaSection from "./RegistroLanchaSection";
import LandingFooter from "./LandingFooter";

export default function LandingPage() {
  return (
    <div className="flex flex-col items-start w-full bg-bg-surface-primary shadow-[0px_4px_3px_rgba(12,12,13,0.1),0px_2px_2px_rgba(12,12,13,0.05)]">
      <HeroSection />
      <LanchasDestacadasSection />
      <ComoFuncionaSection />
      <DestinosSection />
      <RegistroLanchaSection />
      <LandingFooter />
    </div>
  );
}

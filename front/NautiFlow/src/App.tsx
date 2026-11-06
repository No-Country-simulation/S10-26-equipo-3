import Landing from "./components/landing/Landing";
import LancheroRegistro from "./components/lanchero/LancheroRegistro";

// Ruteo mínimo hasta sumar un router: "/registro" muestra el formulario y el resto, la landing
const enRegistro = window.location.pathname.startsWith("/registro");

function App() {
  if (!enRegistro) return <Landing />;

  return (
    <div className="flex min-h-dvh flex-col bg-bg-fill-secondary p-4 md:p-16">
      <LancheroRegistro />
    </div>
  );
}

export default App;
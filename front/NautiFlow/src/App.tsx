import LancheroRegistro from "./components/lanchero/LancheroRegistro";
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Admin from "./components/Admin/Admin";
import LandingPage from "./components/landing/LandingPage";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route
          path="/registro-lanchero"
          element={
            <div className="flex min-h-dvh flex-col bg-bg-fill-secondary p-4 font-inter md:p-16">
              <LancheroRegistro />
            </div>
          }
        />
        <Route path="/admin" element={<Admin />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
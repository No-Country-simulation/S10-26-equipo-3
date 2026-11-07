import Landing from "./components/landing/Landing";
import LancheroRegistro from "./components/lanchero/LancheroRegistro";
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Admin from "./components/Admin/Admin";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Landing />} />
        <Route
          path="/registro"
          element={
            <div className="flex min-h-dvh flex-col bg-bg-fill-secondary p-4 font-inter md:p-16">
              <LancheroRegistro />
            </div>
          }
        />
        <Route path="/admin/*" element={<Admin />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;

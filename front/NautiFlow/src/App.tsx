import LancheroRegistro from "./components/lanchero/LancheroRegistro";
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Admin from "./components/Admin/Admin";

function App() {
  return (
    <BrowserRouter>
      <Routes>
     
        <Route  path="/" element={<LancheroRegistro/>} />
     
      <Route path="/admin" element={<Admin/>}/>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
import destinoSanFernando from "../../assets/landing/destino-san-fernando.jpeg";
import destinoDeltaTigre from "../../assets/landing/destino-delta-tigre.jpeg";
import destinoRioLujan from "../../assets/landing/destino-rio-lujan.jpeg";
import DestinoCard from "./DestinoCard";

const DESTINOS = [
  {
    id: "san-fernando",
    nombre: "San Fernando",
    descripcion: "Una vista distinta del Delta con múltiples paradas",
    imagen: destinoSanFernando,
  },
  {
    id: "delta-tigre",
    nombre: "Delta del Tigre",
    descripcion: "Naturaleza, islas y tranquilidad a solo minutos de la ciudad",
    imagen: destinoDeltaTigre,
  },
  {
    id: "rio-lujan",
    nombre: "Río Luján",
    descripcion: "Recorridos únicos con paisajes increíbles",
    imagen: destinoRioLujan,
  },
];

export default function DestinosSection() {
  return (
    <section className="flex flex-col gap-8 md:gap-10 items-center w-full overflow-clip px-4 sm:px-8 md:px-[60px] py-10 md:py-16 shrink-0">
      {/* Título */}
      <h2 className="font-bold text-2xl sm:text-3xl md:text-[length:var(--text-heading-2xl-size)] leading-snug md:leading-[var(--text-heading-2xl-line-height)] text-bg-fill-primary text-center w-full">
        Explora nuestros destinos
      </h2>

      {/* Lista de destinos */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6 items-center justify-center w-full max-w-6xl mx-auto">
        {DESTINOS.map((destino) => (
          <DestinoCard key={destino.id} {...destino} />
        ))}
      </div>
    </section>
  );
}

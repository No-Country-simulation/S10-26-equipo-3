import destinoSanFernando from "../assets/landing/destino-san-fernando.jpeg";
import destinoDeltaTigre from "../assets/landing/destino-delta-tigre.jpeg";
import destinoRioLujan from "../assets/landing/destino-rio-lujan.jpeg";
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
    <section className="flex flex-col gap-8 items-center w-full overflow-clip px-10 py-12 shrink-0">
      {/* Título */}
      <h2 className="font-bold text-[length:var(--text-heading-2xl-size)] leading-[var(--text-heading-2xl-line-height)] text-bg-fill-primary text-center w-full">
        Explora nuestros destinos
      </h2>

      {/* Lista de destinos */}
      <div className="flex gap-6 items-center justify-center overflow-clip p-2.5 w-full shrink-0">
        {DESTINOS.map((destino) => (
          <DestinoCard key={destino.id} {...destino} />
        ))}
      </div>
    </section>
  );
}

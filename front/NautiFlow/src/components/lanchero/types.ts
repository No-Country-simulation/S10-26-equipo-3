export type DatosLanchero = {
    nombreCompleto: string;
    whatsapp: string;
    email: string;
    nombreEmbarcacion: string;
    capacidad: number;
    zonas: string[];
    imagenes: File[];
    licencia: File | null;
    seguro: File | null;
    antecedentes: File | null;
};

export const datosIniciales: DatosLanchero = {
    nombreCompleto: "",
    whatsapp: "",
    email: "",
    nombreEmbarcacion: "",
    capacidad: 0,
    zonas: [],
    imagenes: [],
    licencia: null,
    seguro: null,
    antecedentes: null,
};
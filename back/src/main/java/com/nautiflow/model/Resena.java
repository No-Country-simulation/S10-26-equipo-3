package com.nautiflow.model;

import jakarta.persistence.*;
import com.nautiflow.util.FechaHoraUtc;

import java.time.OffsetDateTime;

/**
 * Entidad que guarda la calificación y el comentario de un viaje finalizado. Cada viaje puede tener como máximo una reseña.
 */
@Entity
@Table(name = "resena")
public class Resena {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_resena")
    private Long idResena;

    @OneToOne(optional = false)
    @JoinColumn(name = "id_viaje", nullable = false, unique = true)
    private Viaje viaje;

    @Column(nullable = false)
    private Integer estrellas;

    @Column(length = 500)
    private String comentario;

    @Column(nullable = false)
    private OffsetDateTime fecha = FechaHoraUtc.ahora();

    /**
     * Obtiene identificador de la reseña.
     */
    public Long getIdResena() { return idResena; }
    /**
     * Establece identificador de la reseña.
     */
    public void setIdResena(Long idResena) { this.idResena = idResena; }

    /**
     * Obtiene viaje asociado.
     */
    public Viaje getViaje() { return viaje; }
    /**
     * Establece viaje asociado.
     */
    public void setViaje(Viaje viaje) { this.viaje = viaje; }

    /**
     * Obtiene puntuación de la reseña, entre 1 y 5.
     */
    public Integer getEstrellas() { return estrellas; }
    /**
     * Establece puntuación de la reseña, entre 1 y 5.
     */
    public void setEstrellas(Integer estrellas) { this.estrellas = estrellas; }

    /**
     * Obtiene comentario opcional de la reseña.
     */
    public String getComentario() { return comentario; }
    /**
     * Establece comentario opcional de la reseña.
     */
    public void setComentario(String comentario) { this.comentario = comentario; }

    /**
     * Obtiene fecha y hora del registro.
     */
    public OffsetDateTime getFecha() { return fecha; }
    /**
     * Establece fecha y hora del registro.
     */
    public void setFecha(OffsetDateTime fecha) { this.fecha = fecha; }
}

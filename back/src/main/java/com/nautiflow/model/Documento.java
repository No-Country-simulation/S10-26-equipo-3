package com.nautiflow.model;

import jakarta.persistence.*;
import com.nautiflow.util.FechaHoraUtc;

import java.time.OffsetDateTime;

@Entity
@Table(name = "documento")
public class Documento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_documento")
    private Long idDocumento;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_lanchero", nullable = false)
    private Lanchero lanchero;

    @Column(name = "tipo_documento", nullable = false, length = 50)
    private String tipoDocumento;

    @Column(name = "clave_archivo", nullable = false, length = 255)
    private String claveArchivo;

    @Column(name = "tipo_contenido", nullable = false, length = 255)
    private String tipoContenido;

    @Column(nullable = false)
    private OffsetDateTime fecha = FechaHoraUtc.ahora();

    public Long getIdDocumento() { return idDocumento; }
    public void setIdDocumento(Long idDocumento) { this.idDocumento = idDocumento; }

    public Lanchero getLanchero() { return lanchero; }
    public void setLanchero(Lanchero lanchero) { this.lanchero = lanchero; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getClaveArchivo() { return claveArchivo; }
    public void setClaveArchivo(String claveArchivo) { this.claveArchivo = claveArchivo; }

    public String getTipoContenido() { return tipoContenido; }
    public void setTipoContenido(String tipoContenido) { this.tipoContenido = tipoContenido; }

    public OffsetDateTime getFecha() { return fecha; }
    public void setFecha(OffsetDateTime fecha) { this.fecha = fecha; }
}

package com.nautiflow.model;

import jakarta.persistence.*;
import com.nautiflow.util.FechaHoraUtc;

import java.time.OffsetDateTime;

/**
 * Entidad que guarda los metadatos de un archivo del lanchero. El archivo real vive en Cloudflare R2.
 */
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

    /**
     * Obtiene identificador del documento.
     */
    public Long getIdDocumento() { return idDocumento; }
    /**
     * Establece identificador del documento.
     */
    public void setIdDocumento(Long idDocumento) { this.idDocumento = idDocumento; }

    /**
     * Obtiene lanchero asociado.
     */
    public Lanchero getLanchero() { return lanchero; }
    /**
     * Establece lanchero asociado.
     */
    public void setLanchero(Lanchero lanchero) { this.lanchero = lanchero; }

    /**
     * Obtiene tipo de documento: LICENCIA, SEGURO, ANTECEDENTES o FOTO_LANCHA.
     */
    public String getTipoDocumento() { return tipoDocumento; }
    /**
     * Establece tipo de documento: LICENCIA, SEGURO, ANTECEDENTES o FOTO_LANCHA.
     */
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    /**
     * Obtiene clave del archivo en R2.
     */
    public String getClaveArchivo() { return claveArchivo; }
    /**
     * Establece clave del archivo en R2.
     */
    public void setClaveArchivo(String claveArchivo) { this.claveArchivo = claveArchivo; }

    /**
     * Obtiene tipo MIME del archivo.
     */
    public String getTipoContenido() { return tipoContenido; }
    /**
     * Establece tipo MIME del archivo.
     */
    public void setTipoContenido(String tipoContenido) { this.tipoContenido = tipoContenido; }

    /**
     * Obtiene fecha y hora del registro.
     */
    public OffsetDateTime getFecha() { return fecha; }
    /**
     * Establece fecha y hora del registro.
     */
    public void setFecha(OffsetDateTime fecha) { this.fecha = fecha; }
}

package com.nautiflow.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

/**
 * Entidad que representa una zona o ruta navegable del delta.
 */
@Entity
@Table(name = "zona")
public class Zona {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_zona")
    private Long idZona;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @ManyToMany(mappedBy = "zonas")
    private Set<Lancha> lanchas = new HashSet<>();

    @OneToMany(mappedBy = "zonaOrigen")
    private Set<Viaje> viajesOrigen = new HashSet<>();

    @OneToMany(mappedBy = "zonaDestino")
    private Set<Viaje> viajesDestino = new HashSet<>();

    @OneToMany(mappedBy = "zonaOrigen")
    private Set<TrayectoRetornoVacio> retornosOrigen = new HashSet<>();

    @OneToMany(mappedBy = "zonaDestino")
    private Set<TrayectoRetornoVacio> retornosDestino = new HashSet<>();

    /**
     * Obtiene identificador de la zona.
     */
    public Long getIdZona() { return idZona; }
    /**
     * Establece identificador de la zona.
     */
    public void setIdZona(Long idZona) { this.idZona = idZona; }

    /**
     * Obtiene nombre del registro.
     */
    public String getNombre() { return nombre; }
    /**
     * Establece nombre del registro.
     */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /**
     * Obtiene embarcaciones que cubren la zona.
     */
    public Set<Lancha> getLanchas() { return lanchas; }
    /**
     * Establece embarcaciones que cubren la zona.
     */
    public void setLanchas(Set<Lancha> lanchas) { this.lanchas = lanchas; }

    /**
     * Obtiene viajes que salen de la zona.
     */
    public Set<Viaje> getViajesOrigen() { return viajesOrigen; }
    /**
     * Establece viajes que salen de la zona.
     */
    public void setViajesOrigen(Set<Viaje> viajesOrigen) { this.viajesOrigen = viajesOrigen; }

    /**
     * Obtiene viajes que llegan a la zona.
     */
    public Set<Viaje> getViajesDestino() { return viajesDestino; }
    /**
     * Establece viajes que llegan a la zona.
     */
    public void setViajesDestino(Set<Viaje> viajesDestino) { this.viajesDestino = viajesDestino; }

    /**
     * Obtiene trayectos de retorno que salen de la zona.
     */
    public Set<TrayectoRetornoVacio> getRetornosOrigen() { return retornosOrigen; }
    /**
     * Establece trayectos de retorno que salen de la zona.
     */
    public void setRetornosOrigen(Set<TrayectoRetornoVacio> retornosOrigen) { this.retornosOrigen = retornosOrigen; }

    /**
     * Obtiene trayectos de retorno que llegan a la zona.
     */
    public Set<TrayectoRetornoVacio> getRetornosDestino() { return retornosDestino; }
    /**
     * Establece trayectos de retorno que llegan a la zona.
     */
    public void setRetornosDestino(Set<TrayectoRetornoVacio> retornosDestino) {
        this.retornosDestino = retornosDestino;
    }
}

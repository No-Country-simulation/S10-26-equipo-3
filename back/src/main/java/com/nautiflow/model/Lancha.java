package com.nautiflow.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entidad que representa la embarcación de un lanchero, con su capacidad y las zonas que cubre.
 */
@Entity
@Table(name = "lancha")
public class Lancha {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lancha")
    private Long idLancha;

    @OneToOne(optional = false)
    @JoinColumn(name = "id_lanchero", nullable = false, unique = true)
    private Lanchero lanchero;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false)
    private Integer capacidad;

    @ManyToMany
    @JoinTable(
            name = "lancha_zona",
            joinColumns = @JoinColumn(name = "id_lancha"),
            inverseJoinColumns = @JoinColumn(name = "id_zona")
    )
    private Set<Zona> zonas = new HashSet<>();

    @OneToMany(mappedBy = "lancha")
    private Set<Viaje> viajes = new HashSet<>();

    /**
     * Obtiene identificador de la lancha.
     */
    public Long getIdLancha() { return idLancha; }
    /**
     * Establece identificador de la lancha.
     */
    public void setIdLancha(Long idLancha) { this.idLancha = idLancha; }

    /**
     * Obtiene lanchero asociado.
     */
    public Lanchero getLanchero() { return lanchero; }
    /**
     * Establece lanchero asociado.
     */
    public void setLanchero(Lanchero lanchero) { this.lanchero = lanchero; }

    /**
     * Obtiene nombre del registro.
     */
    public String getNombre() { return nombre; }
    /**
     * Establece nombre del registro.
     */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /**
     * Obtiene capacidad máxima de pasajeros.
     */
    public Integer getCapacidad() { return capacidad; }
    /**
     * Establece capacidad máxima de pasajeros.
     */
    public void setCapacidad(Integer capacidad) { this.capacidad = capacidad; }

    /**
     * Obtiene nombres de las zonas asociadas.
     */
    public Set<Zona> getZonas() { return zonas; }
    /**
     * Establece nombres de las zonas asociadas.
     */
    public void setZonas(Set<Zona> zonas) { this.zonas = zonas; }

    /**
     * Agrega una zona a la embarcación y mantiene sincronizada la relación inversa.
     */
    public void agregarZona(Zona zona) {
        if (zonas.add(zona)) {
            zona.getLanchas().add(this);
        }
    }

    /**
     * Quita una zona de la embarcación y mantiene sincronizada la relación inversa.
     */
    public void quitarZona(Zona zona) {
        if (zonas.remove(zona)) {
            zona.getLanchas().remove(this);
        }
    }

    /**
     * Obtiene viajes realizados por la embarcación.
     */
    public Set<Viaje> getViajes() { return viajes; }
    /**
     * Establece viajes realizados por la embarcación.
     */
    public void setViajes(Set<Viaje> viajes) { this.viajes = viajes; }

}

package com.nautiflow.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

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

    public Long getIdLancha() { return idLancha; }
    public void setIdLancha(Long idLancha) { this.idLancha = idLancha; }

    public Lanchero getLanchero() { return lanchero; }
    public void setLanchero(Lanchero lanchero) { this.lanchero = lanchero; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Integer getCapacidad() { return capacidad; }
    public void setCapacidad(Integer capacidad) { this.capacidad = capacidad; }

    public Set<Zona> getZonas() { return zonas; }
    public void setZonas(Set<Zona> zonas) { this.zonas = zonas; }

    public void agregarZona(Zona zona) {
        if (zonas.add(zona)) {
            zona.getLanchas().add(this);
        }
    }

    public void quitarZona(Zona zona) {
        if (zonas.remove(zona)) {
            zona.getLanchas().remove(this);
        }
    }

    public Set<Viaje> getViajes() { return viajes; }
    public void setViajes(Set<Viaje> viajes) { this.viajes = viajes; }

}

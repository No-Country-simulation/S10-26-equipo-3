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

    public Long getIdZona() { return idZona; }
    public void setIdZona(Long idZona) { this.idZona = idZona; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Set<Lancha> getLanchas() { return lanchas; }
    public void setLanchas(Set<Lancha> lanchas) { this.lanchas = lanchas; }

    public Set<Viaje> getViajesOrigen() { return viajesOrigen; }
    public void setViajesOrigen(Set<Viaje> viajesOrigen) { this.viajesOrigen = viajesOrigen; }

    public Set<Viaje> getViajesDestino() { return viajesDestino; }
    public void setViajesDestino(Set<Viaje> viajesDestino) { this.viajesDestino = viajesDestino; }

    public Set<TrayectoRetornoVacio> getRetornosOrigen() { return retornosOrigen; }
    public void setRetornosOrigen(Set<TrayectoRetornoVacio> retornosOrigen) { this.retornosOrigen = retornosOrigen; }

    public Set<TrayectoRetornoVacio> getRetornosDestino() { return retornosDestino; }
    public void setRetornosDestino(Set<TrayectoRetornoVacio> retornosDestino) {
        this.retornosDestino = retornosDestino;
    }
}

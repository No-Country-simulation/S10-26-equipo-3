package com.nautiflow.model;

import jakarta.persistence.*;

@Entity
@Table(name = "lancha")
public class Lancha {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lancha")
    private Long idLancha;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_lanchero", nullable = false)
    private Lanchero lanchero;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false)
    private Integer capacidad;

    @Column(nullable = false, length = 20)
    private String estado = "PENDIENTE";

    public Long getIdLancha() { return idLancha; }
    public void setIdLancha(Long idLancha) { this.idLancha = idLancha; }

    public Lanchero getLanchero() { return lanchero; }
    public void setLanchero(Lanchero lanchero) { this.lanchero = lanchero; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Integer getCapacidad() { return capacidad; }
    public void setCapacidad(Integer capacidad) { this.capacidad = capacidad; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

}

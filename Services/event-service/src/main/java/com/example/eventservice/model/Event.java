package com.example.eventservice.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "eventos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, length = 300)
    private String ubicacion;

    @Column(name = "fecha_evento", nullable = false)
    private LocalDateTime fechaEvento;

    @Column(nullable = false)
    private Integer capacidadTotal;

    @Column(nullable = false)
    private Integer entradasDisponibles;

    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Builder.Default
    private List<TipoEntrada> tiposEntrada = new ArrayList<>();

    @Column(length = 100)
    private String categoria;

    @Column(nullable = false)
    private Boolean activo;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    public void prePersist() {
        if (activo == null) {
            activo = true;
        }
        // Las entradas disponibles se calculan desde los tipos de entrada
        calcularEntradasDisponibles();
    }

    @PreUpdate
    public void preUpdate() {
        calcularEntradasDisponibles();
    }

    private void calcularEntradasDisponibles() {
        if (tiposEntrada != null && !tiposEntrada.isEmpty()) {
            this.entradasDisponibles = tiposEntrada.stream()
                    .mapToInt(TipoEntrada::getCantidadDisponible)
                    .sum();
            this.capacidadTotal = tiposEntrada.stream()
                    .mapToInt(TipoEntrada::getCantidadTotal)
                    .sum();
        }
    }

    public void addTipoEntrada(TipoEntrada tipoEntrada) {
        tiposEntrada.add(tipoEntrada);
        tipoEntrada.setEvento(this);
    }

    public void removeTipoEntrada(TipoEntrada tipoEntrada) {
        tiposEntrada.remove(tipoEntrada);
        tipoEntrada.setEvento(null);
    }
}

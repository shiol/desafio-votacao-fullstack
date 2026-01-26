package com.soya.votacao.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "sessoes")
public class Sessao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "pauta_id", nullable = false, unique = true)
    private Pauta pauta;

    @Column(nullable = false)
    private Instant abertaEm;

    @Column(nullable = false)
    private Instant fechaEm;

    public Long getId() {
        return id;
    }

    public Pauta getPauta() {
        return pauta;
    }

    public void setPauta(Pauta pauta) {
        this.pauta = pauta;
    }

    public Instant getAbertaEm() {
        return abertaEm;
    }

    public void setAbertaEm(Instant abertaEm) {
        this.abertaEm = abertaEm;
    }

    public Instant getFechaEm() {
        return fechaEm;
    }

    public void setFechaEm(Instant fechaEm) {
        this.fechaEm = fechaEm;
    }
}


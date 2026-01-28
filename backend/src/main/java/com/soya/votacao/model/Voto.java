package com.soya.votacao.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(
        name = "votos",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_votos_pauta_associado", columnNames = {"pauta_id", "associado_id"})
        },
        indexes = {
                @Index(name = "idx_votos_pauta", columnList = "pauta_id"),
                @Index(name = "idx_votos_associado", columnList = "associado_id")
        }
)
public class Voto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pauta_id", nullable = false)
    private Pauta pauta;

    @Column(name = "associado_id", nullable = false, length = 20)
    private String associadoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private VotoValor valor;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() {
        return id;
    }

    public Pauta getPauta() {
        return pauta;
    }

    public void setPauta(Pauta pauta) {
        this.pauta = pauta;
    }

    public String getAssociadoId() {
        return associadoId;
    }

    public void setAssociadoId(String associadoId) {
        this.associadoId = associadoId;
    }

    public VotoValor getValor() {
        return valor;
    }

    public void setValor(VotoValor valor) {
        this.valor = valor;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}


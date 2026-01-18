package com.example.votacao.model;

import com.example.votacao.client.CpfStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "associados_pauta_status")
public class AssociadoPautaStatus {
    @EmbeddedId
    private AssociadoPautaId id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private CpfStatus status;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected AssociadoPautaStatus() {
    }

    public AssociadoPautaStatus(AssociadoPautaId id, CpfStatus status) {
        this.id = id;
        this.status = status;
    }

    public AssociadoPautaId getId() {
        return id;
    }

    public CpfStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

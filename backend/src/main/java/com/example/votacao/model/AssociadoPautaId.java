package com.example.votacao.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class AssociadoPautaId implements Serializable {
    @Column(name = "pauta_id", nullable = false)
    private Long pautaId;

    @Column(name = "associado_id", nullable = false, length = 20)
    private String associadoId;

    protected AssociadoPautaId() {
    }

    public AssociadoPautaId(Long pautaId, String associadoId) {
        this.pautaId = pautaId;
        this.associadoId = associadoId;
    }

    public Long getPautaId() {
        return pautaId;
    }

    public String getAssociadoId() {
        return associadoId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AssociadoPautaId that = (AssociadoPautaId) o;
        return Objects.equals(pautaId, that.pautaId)
                && Objects.equals(associadoId, that.associadoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pautaId, associadoId);
    }
}

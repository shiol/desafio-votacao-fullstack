package com.soya.votacao.dto;

import com.soya.votacao.model.VotoValor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class VotoRequest {
    @NotBlank
    @Size(max = 20)
    private String associadoId;

    @NotNull
    private VotoValor voto;

    public String getAssociadoId() {
        return associadoId;
    }

    public void setAssociadoId(String associadoId) {
        this.associadoId = associadoId;
    }

    public VotoValor getVoto() {
        return voto;
    }

    public void setVoto(VotoValor voto) {
        this.voto = voto;
    }
}


package com.soya.votacao.dto;

import jakarta.validation.constraints.Min;

public class OpenSessaoRequest {
    @Min(1)
    private Integer duracaoMinutos;

    public Integer getDuracaoMinutos() {
        return duracaoMinutos;
    }

    public void setDuracaoMinutos(Integer duracaoMinutos) {
        this.duracaoMinutos = duracaoMinutos;
    }
}


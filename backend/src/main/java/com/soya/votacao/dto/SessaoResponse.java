package com.soya.votacao.dto;

import java.time.Instant;

public class SessaoResponse {
    private Instant abertaEm;
    private Instant fechaEm;
    private boolean aberta;

    public SessaoResponse(Instant abertaEm, Instant fechaEm, boolean aberta) {
        this.abertaEm = abertaEm;
        this.fechaEm = fechaEm;
        this.aberta = aberta;
    }

    public Instant getAbertaEm() {
        return abertaEm;
    }

    public Instant getFechaEm() {
        return fechaEm;
    }

    public boolean isAberta() {
        return aberta;
    }
}


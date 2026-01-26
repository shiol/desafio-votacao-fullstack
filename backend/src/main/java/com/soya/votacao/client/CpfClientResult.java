package com.soya.votacao.client;

public class CpfClientResult {
    private boolean valid;
    private CpfStatus status;

    public CpfClientResult(boolean valid, CpfStatus status) {
        this.valid = valid;
        this.status = status;
    }

    public boolean isValid() {
        return valid;
    }

    public CpfStatus getStatus() {
        return status;
    }
}


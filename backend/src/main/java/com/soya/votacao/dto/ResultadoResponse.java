package com.soya.votacao.dto;

public class ResultadoResponse {
    private Long pautaId;
    private long totalVotos;
    private long votosSim;
    private long votosNao;
    private String status;

    public ResultadoResponse(Long pautaId, long totalVotos, long votosSim, long votosNao, String status) {
        this.pautaId = pautaId;
        this.totalVotos = totalVotos;
        this.votosSim = votosSim;
        this.votosNao = votosNao;
        this.status = status;
    }

    public Long getPautaId() {
        return pautaId;
    }

    public long getTotalVotos() {
        return totalVotos;
    }

    public long getVotosSim() {
        return votosSim;
    }

    public long getVotosNao() {
        return votosNao;
    }

    public String getStatus() {
        return status;
    }
}


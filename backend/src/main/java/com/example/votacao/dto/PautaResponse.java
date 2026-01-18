package com.example.votacao.dto;

import java.time.Instant;

public class PautaResponse {
    private Long id;
    private String titulo;
    private String descricao;
    private Instant createdAt;
    private SessaoResponse sessao;

    public PautaResponse(Long id, String titulo, String descricao, Instant createdAt, SessaoResponse sessao) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.createdAt = createdAt;
        this.sessao = sessao;
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public SessaoResponse getSessao() {
        return sessao;
    }
}

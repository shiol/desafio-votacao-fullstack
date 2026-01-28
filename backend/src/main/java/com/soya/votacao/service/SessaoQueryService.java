package com.soya.votacao.service;

import com.soya.votacao.dto.SessaoResponse;
import com.soya.votacao.model.Sessao;
import org.springframework.stereotype.Service;

@Service
public class SessaoQueryService {
    private final SessaoService sessaoService;

    public SessaoQueryService(SessaoService sessaoService) {
        this.sessaoService = sessaoService;
    }

    public SessaoResponse toResponse(Sessao sessao) {
        return new SessaoResponse(
                sessao.getAbertaEm(),
                sessao.getFechaEm(),
                sessaoService.sessaoAberta(sessao)
        );
    }
}


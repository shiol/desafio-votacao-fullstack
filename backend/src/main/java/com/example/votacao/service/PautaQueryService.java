package com.example.votacao.service;

import com.example.votacao.dto.PautaResponse;
import com.example.votacao.dto.SessaoResponse;
import com.example.votacao.model.Pauta;
import com.example.votacao.model.Sessao;
import com.example.votacao.repository.SessaoRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class PautaQueryService {
    private final PautaService pautaService;
    private final SessaoRepository sessaoRepository;
    private final SessaoService sessaoService;

    public PautaQueryService(PautaService pautaService, SessaoRepository sessaoRepository, SessaoService sessaoService) {
        this.pautaService = pautaService;
        this.sessaoRepository = sessaoRepository;
        this.sessaoService = sessaoService;
    }

    public List<PautaResponse> listar() {
        return pautaService.listar().stream()
                .map(this::toResponse)
                .toList();
    }

    public PautaResponse buscar(Long id) {
        Pauta pauta = pautaService.buscarPorId(id);
        return toResponse(pauta);
    }

    public PautaResponse toResponse(Pauta pauta) {
        Optional<Sessao> sessao = sessaoRepository.findByPautaId(pauta.getId());
        SessaoResponse sessaoResponse = sessao
                .map(item -> new SessaoResponse(item.getAbertaEm(), item.getFechaEm(), sessaoService.sessaoAberta(item)))
                .orElse(null);
        return new PautaResponse(
                pauta.getId(),
                pauta.getTitulo(),
                pauta.getDescricao(),
                pauta.getCreatedAt(),
                sessaoResponse
        );
    }
}

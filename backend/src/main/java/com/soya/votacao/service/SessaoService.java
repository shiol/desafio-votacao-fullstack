package com.soya.votacao.service;

import com.soya.votacao.dto.OpenSessaoRequest;
import com.soya.votacao.exception.ConflictException;
import com.soya.votacao.exception.NotFoundException;
import com.soya.votacao.model.Pauta;
import com.soya.votacao.model.Sessao;
import com.soya.votacao.repository.SessaoRepository;
import io.micrometer.observation.annotation.Observed;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SessaoService {
    private static final Logger log = LoggerFactory.getLogger(SessaoService.class);
    private static final int DEFAULT_MINUTES = 1;

    private final SessaoRepository sessaoRepository;
    private final PautaService pautaService;

    public SessaoService(SessaoRepository sessaoRepository, PautaService pautaService) {
        this.sessaoRepository = sessaoRepository;
        this.pautaService = pautaService;
    }

    @Observed(name = "votacao.sessao.abrir")
    public Sessao abrirSessao(Long pautaId, OpenSessaoRequest request) {
        Pauta pauta = pautaService.buscarPorId(pautaId);
        Optional<Sessao> existente = sessaoRepository.findByPautaId(pautaId);
        if (existente.isPresent()) {
            throw new ConflictException("Sessão já está aberta para esta pauta");
        }

        int minutos = request != null && request.getDuracaoMinutos() != null
                ? request.getDuracaoMinutos()
                : DEFAULT_MINUTES;
        Instant now = Instant.now();

        Sessao sessao = new Sessao();
        sessao.setPauta(pauta);
        sessao.setAbertaEm(now);
        sessao.setFechaEm(now.plus(minutos, ChronoUnit.MINUTES));

        Sessao saved = sessaoRepository.save(sessao);
        log.info("Opened session {} for pauta {}", saved.getId(), pautaId);
        return saved;
    }

    @Observed(name = "votacao.sessao.buscar")
    public Sessao buscarPorPauta(Long pautaId) {
        return sessaoRepository.findByPautaId(pautaId)
                .orElseThrow(() -> new NotFoundException("Sessão não encontrada para a pauta"));
    }

    @Observed(name = "votacao.sessao.aberta")
    public boolean sessaoAberta(Sessao sessao) {
        Instant now = Instant.now();
        return now.isBefore(sessao.getFechaEm());
    }
}



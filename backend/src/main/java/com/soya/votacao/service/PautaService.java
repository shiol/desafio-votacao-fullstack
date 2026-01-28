package com.soya.votacao.service;

import com.soya.votacao.dto.CreatePautaRequest;
import com.soya.votacao.exception.NotFoundException;
import com.soya.votacao.model.Pauta;
import com.soya.votacao.repository.PautaRepository;
import com.soya.votacao.repository.SessaoRepository;
import com.soya.votacao.repository.VotoRepository;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PautaService {
    private static final Logger log = LoggerFactory.getLogger(PautaService.class);

    private final PautaRepository pautaRepository;
    private final SessaoRepository sessaoRepository;
    private final VotoRepository votoRepository;

    public PautaService(PautaRepository pautaRepository, SessaoRepository sessaoRepository, VotoRepository votoRepository) {
        this.pautaRepository = pautaRepository;
        this.sessaoRepository = sessaoRepository;
        this.votoRepository = votoRepository;
    }

    @Observed(name = "votacao.pauta.criar")
    public Pauta criar(CreatePautaRequest request) {
        Pauta pauta = new Pauta();
        pauta.setTitulo(request.getTitulo());
        pauta.setDescricao(request.getDescricao());
        Pauta saved = pautaRepository.save(pauta);
        log.info("Created pauta {}", saved.getId());
        return saved;
    }

    @Observed(name = "votacao.pauta.listar")
    public List<Pauta> listar() {
        return pautaRepository.findAll();
    }

    @Observed(name = "votacao.pauta.buscar")
    public Pauta buscarPorId(Long id) {
        return pautaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pauta não encontrada"));
    }

    @Transactional
    @Observed(name = "votacao.pauta.remover")
    public void remover(Long id) {
        Pauta pauta = buscarPorId(id);
        votoRepository.deleteByPautaId(pauta.getId());
        sessaoRepository.deleteByPautaId(pauta.getId());
        pautaRepository.delete(pauta);
        log.info("Deleted pauta {}", pauta.getId());
    }
}



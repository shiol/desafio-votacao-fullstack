package com.example.votacao.service;

import com.example.votacao.dto.CreatePautaRequest;
import com.example.votacao.exception.NotFoundException;
import com.example.votacao.model.Pauta;
import com.example.votacao.repository.PautaRepository;
import com.example.votacao.repository.SessaoRepository;
import com.example.votacao.repository.VotoRepository;
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

    public Pauta criar(CreatePautaRequest request) {
        Pauta pauta = new Pauta();
        pauta.setTitulo(request.getTitulo());
        pauta.setDescricao(request.getDescricao());
        Pauta saved = pautaRepository.save(pauta);
        log.info("Created pauta {}", saved.getId());
        return saved;
    }

    public List<Pauta> listar() {
        return pautaRepository.findAll();
    }

    public Pauta buscarPorId(Long id) {
        return pautaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pauta não encontrada"));
    }

    @Transactional
    public void remover(Long id) {
        Pauta pauta = buscarPorId(id);
        votoRepository.deleteByPautaId(pauta.getId());
        sessaoRepository.deleteByPautaId(pauta.getId());
        pautaRepository.delete(pauta);
        log.info("Deleted pauta {}", pauta.getId());
    }
}

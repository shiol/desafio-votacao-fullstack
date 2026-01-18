package com.example.votacao.controller;

import com.example.votacao.dto.CreatePautaRequest;
import com.example.votacao.dto.OpenSessaoRequest;
import com.example.votacao.dto.PautaResponse;
import com.example.votacao.dto.ResultadoResponse;
import com.example.votacao.dto.SessaoResponse;
import com.example.votacao.dto.VotoRequest;
import com.example.votacao.model.Pauta;
import com.example.votacao.model.Sessao;
import com.example.votacao.repository.SessaoRepository;
import com.example.votacao.service.PautaService;
import com.example.votacao.service.SessaoService;
import com.example.votacao.service.VotoService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pautas")
public class PautaController {
    private final PautaService pautaService;
    private final SessaoService sessaoService;
    private final SessaoRepository sessaoRepository;
    private final VotoService votoService;

    public PautaController(PautaService pautaService, SessaoService sessaoService, SessaoRepository sessaoRepository, VotoService votoService) {
        this.pautaService = pautaService;
        this.sessaoService = sessaoService;
        this.sessaoRepository = sessaoRepository;
        this.votoService = votoService;
    }

    @PostMapping
    public ResponseEntity<PautaResponse> criar(@Valid @RequestBody CreatePautaRequest request) {
        Pauta pauta = pautaService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(pauta, Optional.empty()));
    }

    @GetMapping
    public ResponseEntity<List<PautaResponse>> listar() {
        List<PautaResponse> result = pautaService.listar().stream()
                .map(pauta -> toResponse(pauta, sessaoRepository.findByPautaId(pauta.getId())))
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PautaResponse> buscar(@PathVariable("id") Long id) {
        Pauta pauta = pautaService.buscarPorId(id);
        Optional<Sessao> sessao = sessaoRepository.findByPautaId(id);
        return ResponseEntity.ok(toResponse(pauta, sessao));
    }

    @PostMapping("/{id}/sessoes")
    public ResponseEntity<SessaoResponse> abrirSessao(@PathVariable("id") Long id, @Valid @RequestBody(required = false) OpenSessaoRequest request) {
        Sessao sessao = sessaoService.abrirSessao(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toSessaoResponse(sessao, sessaoService.sessaoAberta(sessao)));
    }

    @PostMapping("/{id}/votos")
    public ResponseEntity<Void> votar(@PathVariable("id") Long id, @Valid @RequestBody VotoRequest request) {
        votoService.votar(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}/resultado")
    public ResponseEntity<ResultadoResponse> resultado(@PathVariable("id") Long id) {
        return ResponseEntity.ok(votoService.resultado(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable("id") Long id) {
        pautaService.remover(id);
        return ResponseEntity.noContent().build();
    }

    private PautaResponse toResponse(Pauta pauta, Optional<Sessao> sessao) {
        SessaoResponse sessaoResponse = sessao.map(s -> toSessaoResponse(s, sessaoService.sessaoAberta(s))).orElse(null);
        return new PautaResponse(pauta.getId(), pauta.getTitulo(), pauta.getDescricao(), pauta.getCreatedAt(), sessaoResponse);
    }

    private SessaoResponse toSessaoResponse(Sessao sessao, boolean aberta) {
        return new SessaoResponse(sessao.getAbertaEm(), sessao.getFechaEm(), aberta);
    }
}

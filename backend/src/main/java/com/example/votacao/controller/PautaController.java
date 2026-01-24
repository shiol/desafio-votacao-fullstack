package com.example.votacao.controller;

import com.example.votacao.dto.CreatePautaRequest;
import com.example.votacao.dto.OpenSessaoRequest;
import com.example.votacao.dto.PautaResponse;
import com.example.votacao.dto.ResultadoResponse;
import com.example.votacao.dto.SessaoResponse;
import com.example.votacao.dto.VotoRequest;
import com.example.votacao.model.Pauta;
import com.example.votacao.model.Sessao;
import com.example.votacao.service.PautaQueryService;
import com.example.votacao.service.PautaService;
import com.example.votacao.service.SessaoQueryService;
import com.example.votacao.service.SessaoService;
import com.example.votacao.service.VotoService;
import jakarta.validation.Valid;
import java.util.List;
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
    private final VotoService votoService;
    private final PautaQueryService pautaQueryService;
    private final SessaoQueryService sessaoQueryService;

    public PautaController(
            PautaService pautaService,
            SessaoService sessaoService,
            VotoService votoService,
            PautaQueryService pautaQueryService,
            SessaoQueryService sessaoQueryService
    ) {
        this.pautaService = pautaService;
        this.sessaoService = sessaoService;
        this.votoService = votoService;
        this.pautaQueryService = pautaQueryService;
        this.sessaoQueryService = sessaoQueryService;
    }

    @PostMapping
    public ResponseEntity<PautaResponse> criar(@Valid @RequestBody CreatePautaRequest request) {
        Pauta pauta = pautaService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(pautaQueryService.toResponse(pauta));
    }

    @GetMapping
    public ResponseEntity<List<PautaResponse>> listar() {
        return ResponseEntity.ok(pautaQueryService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PautaResponse> buscar(@PathVariable("id") Long id) {
        return ResponseEntity.ok(pautaQueryService.buscar(id));
    }

    @PostMapping("/{id}/sessoes")
    public ResponseEntity<SessaoResponse> abrirSessao(@PathVariable("id") Long id, @Valid @RequestBody(required = false) OpenSessaoRequest request) {
        Sessao sessao = sessaoService.abrirSessao(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sessaoQueryService.toResponse(sessao));
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
}

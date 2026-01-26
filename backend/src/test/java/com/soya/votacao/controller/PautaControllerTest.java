package com.soya.votacao.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.soya.votacao.dto.PautaResponse;
import com.soya.votacao.dto.ResultadoResponse;
import com.soya.votacao.dto.SessaoResponse;
import com.soya.votacao.model.Pauta;
import com.soya.votacao.model.Sessao;
import com.soya.votacao.service.PautaQueryService;
import com.soya.votacao.service.PautaService;
import com.soya.votacao.service.SessaoQueryService;
import com.soya.votacao.service.SessaoService;
import com.soya.votacao.service.VotoService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PautaController.class)
class PautaControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PautaService pautaService;

    @MockBean
    private SessaoService sessaoService;

    @MockBean
    private VotoService votoService;

    @MockBean
    private PautaQueryService pautaQueryService;

    @MockBean
    private SessaoQueryService sessaoQueryService;

    @Test
    void listarRetornaPautas() throws Exception {
        PautaResponse response = new PautaResponse(1L, "Pauta", "Descricao", Instant.now(), null);
        when(pautaQueryService.listar()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/pautas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void criarRetornaPautaCriada() throws Exception {
        Pauta pauta = new Pauta();
        PautaResponse response = new PautaResponse(10L, "Nova", "Descricao", Instant.now(), null);
        when(pautaService.criar(any())).thenReturn(pauta);
        when(pautaQueryService.toResponse(pauta)).thenReturn(response);

        mockMvc.perform(post("/api/v1/pautas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Nova\",\"descricao\":\"Descricao\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L));
    }

    @Test
    void abrirSessaoRetornaSessao() throws Exception {
        Sessao sessao = new Sessao();
        SessaoResponse response = new SessaoResponse(Instant.now(), Instant.now(), true);
        when(sessaoService.abrirSessao(eq(1L), any())).thenReturn(sessao);
        when(sessaoQueryService.toResponse(sessao)).thenReturn(response);

        mockMvc.perform(post("/api/v1/pautas/1/sessoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"duracaoMinutos\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.aberta").value(true));
    }

    @Test
    void votarRetornaCreated() throws Exception {
        mockMvc.perform(post("/api/v1/pautas/1/votos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"associadoId\":\"12345678901\",\"voto\":\"SIM\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void resultadoRetornaResumo() throws Exception {
        ResultadoResponse response = new ResultadoResponse(1L, 2L, 1L, 1L, "ABERTA");
        when(votoService.resultado(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/pautas/1/resultado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVotos").value(2L));
    }

    @Test
    void removerRetornaNoContent() throws Exception {
        doNothing().when(pautaService).remover(1L);

        mockMvc.perform(delete("/api/v1/pautas/1"))
                .andExpect(status().isNoContent());
    }
}


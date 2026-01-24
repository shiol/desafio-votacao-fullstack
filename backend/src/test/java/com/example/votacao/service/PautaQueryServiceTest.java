package com.example.votacao.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.votacao.dto.PautaResponse;
import com.example.votacao.model.Pauta;
import com.example.votacao.model.Sessao;
import com.example.votacao.repository.SessaoRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PautaQueryServiceTest {
    @Mock
    private PautaService pautaService;

    @Mock
    private SessaoRepository sessaoRepository;

    @Mock
    private SessaoService sessaoService;

    @InjectMocks
    private PautaQueryService pautaQueryService;

    @Test
    void listarMapeiaSessaoQuandoExiste() {
        Pauta pauta = new Pauta();
        setId(pauta, 1L);
        pauta.setTitulo("Título");
        pauta.setDescricao("Descrição");

        Sessao sessao = new Sessao();
        sessao.setAbertaEm(Instant.parse("2026-01-01T10:10:00Z"));
        sessao.setFechaEm(Instant.parse("2026-01-01T10:20:00Z"));

        when(pautaService.listar()).thenReturn(List.of(pauta));
        when(sessaoRepository.findByPautaId(1L)).thenReturn(Optional.of(sessao));
        when(sessaoService.sessaoAberta(sessao)).thenReturn(true);

        List<PautaResponse> result = pautaQueryService.listar();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSessao()).isNotNull();
        assertThat(result.get(0).getSessao().isAberta()).isTrue();
    }

    @Test
    void buscarRetornaPautaSemSessaoQuandoNaoExiste() {
        Pauta pauta = new Pauta();
        setId(pauta, 2L);
        pauta.setTitulo("Pauta sem sessão");

        when(pautaService.buscarPorId(2L)).thenReturn(pauta);
        when(sessaoRepository.findByPautaId(2L)).thenReturn(Optional.empty());

        PautaResponse response = pautaQueryService.buscar(2L);

        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getSessao()).isNull();
    }

    private void setId(Pauta pauta, long id) {
        try {
            var field = Pauta.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(pauta, id);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Failed to set id", ex);
        }
    }
}

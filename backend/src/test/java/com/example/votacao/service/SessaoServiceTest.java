package com.example.votacao.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.votacao.dto.OpenSessaoRequest;
import com.example.votacao.exception.ConflictException;
import com.example.votacao.model.Pauta;
import com.example.votacao.model.Sessao;
import com.example.votacao.repository.SessaoRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SessaoServiceTest {
    @Mock
    private SessaoRepository sessaoRepository;

    @Mock
    private PautaService pautaService;

    @InjectMocks
    private SessaoService sessaoService;

    @Test
    void abrirSessaoUsaDuracaoDefaultQuandoNulo() {
        Pauta pauta = new Pauta();
        setId(pauta, 1L);
        when(pautaService.buscarPorId(1L)).thenReturn(pauta);
        when(sessaoRepository.findByPautaId(1L)).thenReturn(Optional.empty());
        when(sessaoRepository.save(any(Sessao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Sessao sessao = sessaoService.abrirSessao(1L, null);

        assertThat(sessao.getAbertaEm()).isNotNull();
        assertThat(sessao.getFechaEm()).isAfter(sessao.getAbertaEm());
        verify(sessaoRepository).save(any(Sessao.class));
    }

    @Test
    void abrirSessaoNaoPermiteDuplicada() {
        Pauta pauta = new Pauta();
        setId(pauta, 2L);
        when(pautaService.buscarPorId(2L)).thenReturn(pauta);
        when(sessaoRepository.findByPautaId(2L)).thenReturn(Optional.of(new Sessao()));

        assertThatThrownBy(() -> sessaoService.abrirSessao(2L, new OpenSessaoRequest()))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void sessaoAbertaRespondeComBaseNoHorario() {
        Sessao aberta = new Sessao();
        aberta.setFechaEm(Instant.now().plusSeconds(60));
        Sessao fechada = new Sessao();
        fechada.setFechaEm(Instant.now().minusSeconds(60));

        assertThat(sessaoService.sessaoAberta(aberta)).isTrue();
        assertThat(sessaoService.sessaoAberta(fechada)).isFalse();
    }

    @Test
    void abrirSessaoSalvaCamposEsperados() {
        Pauta pauta = new Pauta();
        setId(pauta, 3L);
        when(pautaService.buscarPorId(3L)).thenReturn(pauta);
        when(sessaoRepository.findByPautaId(3L)).thenReturn(Optional.empty());
        when(sessaoRepository.save(any(Sessao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OpenSessaoRequest request = new OpenSessaoRequest();
        request.setDuracaoMinutos(5);

        sessaoService.abrirSessao(3L, request);

        ArgumentCaptor<Sessao> captor = ArgumentCaptor.forClass(Sessao.class);
        verify(sessaoRepository).save(captor.capture());
        Sessao saved = captor.getValue();
        assertThat(saved.getPauta()).isEqualTo(pauta);
        assertThat(saved.getFechaEm()).isAfter(saved.getAbertaEm());
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

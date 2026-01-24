package com.example.votacao.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.votacao.dto.CreatePautaRequest;
import com.example.votacao.exception.NotFoundException;
import com.example.votacao.model.Pauta;
import com.example.votacao.repository.PautaRepository;
import com.example.votacao.repository.SessaoRepository;
import com.example.votacao.repository.VotoRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PautaServiceTest {
    @Mock
    private PautaRepository pautaRepository;

    @Mock
    private SessaoRepository sessaoRepository;

    @Mock
    private VotoRepository votoRepository;

    @InjectMocks
    private PautaService pautaService;

    @Test
    void criarPersistePauta() {
        CreatePautaRequest request = new CreatePautaRequest();
        request.setTitulo("Nova pauta");
        request.setDescricao("Descrição");

        when(pautaRepository.save(any(Pauta.class))).thenAnswer(invocation -> {
            Pauta pauta = invocation.getArgument(0);
            setId(pauta, 10L);
            return pauta;
        });

        Pauta saved = pautaService.criar(request);

        assertThat(saved.getId()).isEqualTo(10L);
        assertThat(saved.getTitulo()).isEqualTo("Nova pauta");
        assertThat(saved.getDescricao()).isEqualTo("Descrição");
    }

    @Test
    void buscarPorIdDisparaErroQuandoNaoEncontra() {
        when(pautaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pautaService.buscarPorId(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void removerLimpaDadosRelacionados() {
        Pauta pauta = new Pauta();
        setId(pauta, 5L);
        when(pautaRepository.findById(5L)).thenReturn(Optional.of(pauta));

        pautaService.remover(5L);

        verify(votoRepository).deleteByPautaId(5L);
        verify(sessaoRepository).deleteByPautaId(5L);
        verify(pautaRepository).delete(pauta);
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

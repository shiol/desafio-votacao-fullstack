package com.soya.votacao.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.soya.votacao.client.CpfClient;
import com.soya.votacao.client.CpfClientResult;
import com.soya.votacao.client.CpfStatus;
import com.soya.votacao.dto.VotoRequest;
import com.soya.votacao.exception.BadRequestException;
import com.soya.votacao.exception.ConflictException;
import com.soya.votacao.exception.NotFoundException;
import com.soya.votacao.model.AssociadoPautaId;
import com.soya.votacao.model.AssociadoPautaStatus;
import com.soya.votacao.model.Pauta;
import com.soya.votacao.model.Sessao;
import com.soya.votacao.model.Voto;
import com.soya.votacao.model.VotoValor;
import com.soya.votacao.repository.AssociadoPautaStatusRepository;
import com.soya.votacao.repository.VotoRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class VotoServiceUnitTest {
    @Mock
    private VotoRepository votoRepository;

    @Mock
    private PautaService pautaService;

    @Mock
    private SessaoService sessaoService;

    @Mock
    private CpfClient cpfClient;

    @Mock
    private AssociadoPautaStatusRepository associadoPautaStatusRepository;

    private SimpleMeterRegistry meterRegistry;
    private VotoService votoService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        votoService = new VotoService(
                votoRepository,
                pautaService,
                sessaoService,
                cpfClient,
                associadoPautaStatusRepository,
                meterRegistry
        );
    }

    @Test
    void votoRejeitadoQuandoSessaoEncerrada() {
        when(pautaService.buscarPorId(1L)).thenReturn(new Pauta());
        when(sessaoService.buscarPorPauta(1L)).thenReturn(new Sessao());
        when(sessaoService.sessaoAberta(any())).thenReturn(false);

        VotoRequest request = new VotoRequest();
        request.setAssociadoId("12345678901");
        request.setVoto(VotoValor.SIM);

        assertThatThrownBy(() -> votoService.votar(1L, request))
                .isInstanceOf(BadRequestException.class);

        assertThat(meterRegistry.find("votacao.voto.rejeitado").counter().count()).isEqualTo(1.0);
    }

    @Test
    void votoRejeitadoQuandoDuplicado() {
        when(pautaService.buscarPorId(2L)).thenReturn(new Pauta());
        when(sessaoService.buscarPorPauta(2L)).thenReturn(new Sessao());
        when(sessaoService.sessaoAberta(any())).thenReturn(true);
        when(votoRepository.findByPautaIdAndAssociadoId(2L, "12345678901")).thenReturn(Optional.of(new Voto()));

        VotoRequest request = new VotoRequest();
        request.setAssociadoId("12345678901");
        request.setVoto(VotoValor.SIM);

        assertThatThrownBy(() -> votoService.votar(2L, request))
                .isInstanceOf(ConflictException.class);

        assertThat(meterRegistry.find("votacao.voto.rejeitado").counter().count()).isEqualTo(1.0);
    }

    @Test
    void votoRejeitadoQuandoCpfInvalido() {
        when(pautaService.buscarPorId(3L)).thenReturn(new Pauta());
        when(sessaoService.buscarPorPauta(3L)).thenReturn(new Sessao());
        when(sessaoService.sessaoAberta(any())).thenReturn(true);
        when(votoRepository.findByPautaIdAndAssociadoId(3L, "12345678901")).thenReturn(Optional.empty());
        when(associadoPautaStatusRepository.findById(any())).thenReturn(Optional.empty());
        when(cpfClient.consultar("12345678901")).thenReturn(new CpfClientResult(false, CpfStatus.UNABLE_TO_VOTE));

        VotoRequest request = new VotoRequest();
        request.setAssociadoId("12345678901");
        request.setVoto(VotoValor.SIM);

        assertThatThrownBy(() -> votoService.votar(3L, request))
                .isInstanceOf(NotFoundException.class);

        assertThat(meterRegistry.find("votacao.voto.rejeitado").counter().count()).isEqualTo(1.0);
    }

    @Test
    void votoRejeitadoQuandoAssociadoNaoPodeVotar() {
        when(pautaService.buscarPorId(4L)).thenReturn(new Pauta());
        when(sessaoService.buscarPorPauta(4L)).thenReturn(new Sessao());
        when(sessaoService.sessaoAberta(any())).thenReturn(true);
        when(votoRepository.findByPautaIdAndAssociadoId(4L, "12345678901")).thenReturn(Optional.empty());
        AssociadoPautaId id = new AssociadoPautaId(4L, "12345678901");
        when(associadoPautaStatusRepository.findById(id))
                .thenReturn(Optional.of(new AssociadoPautaStatus(id, CpfStatus.UNABLE_TO_VOTE)));

        VotoRequest request = new VotoRequest();
        request.setAssociadoId("12345678901");
        request.setVoto(VotoValor.SIM);

        assertThatThrownBy(() -> votoService.votar(4L, request))
                .isInstanceOf(NotFoundException.class);

        assertThat(meterRegistry.find("votacao.voto.rejeitado").counter().count()).isEqualTo(1.0);
    }

    @Test
    void votoSalvoIncrementaContador() {
        when(pautaService.buscarPorId(5L)).thenReturn(new Pauta());
        when(sessaoService.buscarPorPauta(5L)).thenReturn(new Sessao());
        when(sessaoService.sessaoAberta(any())).thenReturn(true);
        when(votoRepository.findByPautaIdAndAssociadoId(5L, "12345678901")).thenReturn(Optional.empty());
        when(associadoPautaStatusRepository.findById(any())).thenReturn(Optional.empty());
        when(cpfClient.consultar("12345678901")).thenReturn(new CpfClientResult(true, CpfStatus.ABLE_TO_VOTE));
        when(votoRepository.save(any(Voto.class))).thenReturn(new Voto());

        VotoRequest request = new VotoRequest();
        request.setAssociadoId("12345678901");
        request.setVoto(VotoValor.SIM);

        votoService.votar(5L, request);

        assertThat(meterRegistry.find("votacao.voto.salvo").counter().count()).isEqualTo(1.0);
    }

    @Test
    void votoRejeitadoQuandoIntegrityViolation() {
        when(pautaService.buscarPorId(6L)).thenReturn(new Pauta());
        when(sessaoService.buscarPorPauta(6L)).thenReturn(new Sessao());
        when(sessaoService.sessaoAberta(any())).thenReturn(true);
        when(votoRepository.findByPautaIdAndAssociadoId(6L, "12345678901")).thenReturn(Optional.empty());
        when(associadoPautaStatusRepository.findById(any())).thenReturn(Optional.empty());
        when(cpfClient.consultar("12345678901")).thenReturn(new CpfClientResult(true, CpfStatus.ABLE_TO_VOTE));
        when(votoRepository.save(any(Voto.class))).thenThrow(new DataIntegrityViolationException("dupe"));

        VotoRequest request = new VotoRequest();
        request.setAssociadoId("12345678901");
        request.setVoto(VotoValor.SIM);

        assertThatThrownBy(() -> votoService.votar(6L, request))
                .isInstanceOf(ConflictException.class);

        assertThat(meterRegistry.find("votacao.voto.rejeitado").counter().count()).isEqualTo(1.0);
    }
}


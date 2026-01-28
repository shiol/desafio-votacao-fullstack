package com.soya.votacao.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.soya.votacao.client.CpfClient;
import com.soya.votacao.client.CpfClientResult;
import com.soya.votacao.client.CpfStatus;
import com.soya.votacao.dto.CreatePautaRequest;
import com.soya.votacao.dto.OpenSessaoRequest;
import com.soya.votacao.dto.ResultadoResponse;
import com.soya.votacao.dto.VotoRequest;
import com.soya.votacao.exception.ConflictException;
import com.soya.votacao.model.Pauta;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext
class VotoServiceTest {
    @Autowired
    private PautaService pautaService;

    @Autowired
    private SessaoService sessaoService;

    @Autowired
    private VotoService votoService;

    @Test
    void voteOncePerAssociado() {
        Pauta pauta = criarPauta();
        abrirSessao(pauta.getId());

        VotoRequest request = new VotoRequest();
        request.setAssociadoId("12345678901");
        request.setVoto(com.soya.votacao.model.VotoValor.SIM);

        votoService.votar(pauta.getId(), request);
        assertThatThrownBy(() -> votoService.votar(pauta.getId(), request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void resultadoContabilizaVotos() {
        Pauta pauta = criarPauta();
        abrirSessao(pauta.getId());

        VotoRequest voto1 = new VotoRequest();
        voto1.setAssociadoId("11111111111");
        voto1.setVoto(com.soya.votacao.model.VotoValor.SIM);

        VotoRequest voto2 = new VotoRequest();
        voto2.setAssociadoId("22222222222");
        voto2.setVoto(com.soya.votacao.model.VotoValor.NAO);

        votoService.votar(pauta.getId(), voto1);
        votoService.votar(pauta.getId(), voto2);

        ResultadoResponse resultado = votoService.resultado(pauta.getId());
        assertThat(resultado.getTotalVotos()).isEqualTo(2);
        assertThat(resultado.getVotosSim()).isEqualTo(1);
        assertThat(resultado.getVotosNao()).isEqualTo(1);
    }

    private Pauta criarPauta() {
        CreatePautaRequest request = new CreatePautaRequest();
        request.setTitulo("Pauta A");
        request.setDescricao("Descrição");
        return pautaService.criar(request);
    }

    private void abrirSessao(Long pautaId) {
        OpenSessaoRequest request = new OpenSessaoRequest();
        request.setDuracaoMinutos(1);
        sessaoService.abrirSessao(pautaId, request);
    }

    @TestConfiguration
    static class FixedCpfClientConfig {
        @Bean
        @Primary
        CpfClient cpfClient() {
            return cpf -> new CpfClientResult(true, CpfStatus.ABLE_TO_VOTE);
        }
    }
}



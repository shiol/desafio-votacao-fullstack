package com.soya.votacao.perf;

import static org.assertj.core.api.Assertions.assertThat;

import com.soya.votacao.client.CpfClient;
import com.soya.votacao.client.CpfClientResult;
import com.soya.votacao.client.CpfStatus;
import com.soya.votacao.dto.CreatePautaRequest;
import com.soya.votacao.dto.OpenSessaoRequest;
import com.soya.votacao.dto.VotoRequest;
import com.soya.votacao.model.Pauta;
import com.soya.votacao.model.VotoValor;
import com.soya.votacao.service.PautaService;
import com.soya.votacao.service.SessaoService;
import com.soya.votacao.service.VotoService;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest
@DirtiesContext
class VotoPerformanceTest {
    private static final Logger log = LoggerFactory.getLogger(VotoPerformanceTest.class);

    @Autowired
    private PautaService pautaService;

    @Autowired
    private SessaoService sessaoService;

    @Autowired
    private VotoService votoService;

    @Test
    @EnabledIfSystemProperty(named = "perf", matches = "true")
    void stressVoteInsertions() {
        Pauta pauta = criarPauta();
        abrirSessao(pauta.getId());

        int totalVotes = Integer.getInteger("perf.votes", 20000);
        Instant start = Instant.now();

        for (int i = 0; i < totalVotes; i++) {
            VotoRequest request = new VotoRequest();
            request.setAssociadoId("cpf-" + i);
            request.setVoto((i % 2 == 0) ? VotoValor.SIM : VotoValor.NAO);
            votoService.votar(pauta.getId(), request);
        }

        Duration elapsed = Duration.between(start, Instant.now());
        log.info("Inserted {} votes in {} ms", totalVotes, elapsed.toMillis());
        assertThat(elapsed.toMillis()).isGreaterThan(0);
    }

    private Pauta criarPauta() {
        CreatePautaRequest request = new CreatePautaRequest();
        request.setTitulo("Pauta Performance");
        request.setDescricao("Perf test");
        return pautaService.criar(request);
    }

    private void abrirSessao(Long pautaId) {
        OpenSessaoRequest request = new OpenSessaoRequest();
        request.setDuracaoMinutos(10);
        sessaoService.abrirSessao(pautaId, request);
    }

    @TestConfiguration
    static class PerfCpfClientConfig {
        @Bean
        @Primary
        CpfClient cpfClient() {
            return cpf -> new CpfClientResult(true, CpfStatus.ABLE_TO_VOTE);
        }
    }
}


package com.example.votacao.client;

import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FakeCpfClient implements CpfClient {
    private static final Logger log = LoggerFactory.getLogger(FakeCpfClient.class);

    @Override
    public CpfClientResult consultar(String cpf) {
        boolean valid = ThreadLocalRandom.current().nextInt(100) >= 20;
        if (!valid) {
            log.info("CPF {} inválido na checagem", cpf);
            return new CpfClientResult(false, null);
        }
        CpfStatus status = ThreadLocalRandom.current().nextBoolean()
                ? CpfStatus.ABLE_TO_VOTE
                : CpfStatus.UNABLE_TO_VOTE;
        log.info("CPF {} fake status {}", cpf, status);
        return new CpfClientResult(true, status);
    }
}

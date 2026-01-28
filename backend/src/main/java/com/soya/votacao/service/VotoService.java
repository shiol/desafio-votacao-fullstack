package com.soya.votacao.service;

import com.soya.votacao.client.CpfClient;
import com.soya.votacao.client.CpfClientResult;
import com.soya.votacao.client.CpfStatus;
import com.soya.votacao.dto.ResultadoResponse;
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
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class VotoService {
    private static final Logger log = LoggerFactory.getLogger(VotoService.class);

    private final VotoRepository votoRepository;
    private final PautaService pautaService;
    private final SessaoService sessaoService;
    private final CpfClient cpfClient;
    private final AssociadoPautaStatusRepository associadoPautaStatusRepository;
    private final Counter votoSalvoCounter;
    private final Counter votoRejeitadoCounter;

    public VotoService(
            VotoRepository votoRepository,
            PautaService pautaService,
            SessaoService sessaoService,
            CpfClient cpfClient,
            AssociadoPautaStatusRepository associadoPautaStatusRepository,
            MeterRegistry meterRegistry
    ) {
        this.votoRepository = votoRepository;
        this.pautaService = pautaService;
        this.sessaoService = sessaoService;
        this.cpfClient = cpfClient;
        this.associadoPautaStatusRepository = associadoPautaStatusRepository;
        this.votoSalvoCounter = Counter.builder("votacao.voto.salvo")
                .description("Quantidade de votos persistidos")
                .register(meterRegistry);
        this.votoRejeitadoCounter = Counter.builder("votacao.voto.rejeitado")
                .description("Quantidade de votos rejeitados por regra de negocio")
                .register(meterRegistry);
    }

    @Observed(name = "votacao.voto.registrar")
    public Voto votar(Long pautaId, VotoRequest request) {
        Pauta pauta = pautaService.buscarPorId(pautaId);
        Sessao sessao = sessaoService.buscarPorPauta(pautaId);
        if (!sessaoService.sessaoAberta(sessao)) {
            votoRejeitadoCounter.increment();
            throw new BadRequestException("Sessão encerrada");
        }

        votoRepository.findByPautaIdAndAssociadoId(pautaId, request.getAssociadoId())
                .ifPresent(voto -> {
                    votoRejeitadoCounter.increment();
                    throw new ConflictException("Associado já votou nesta pauta");
                });

        AssociadoPautaId statusId = new AssociadoPautaId(pautaId, request.getAssociadoId());
        AssociadoPautaStatus status = associadoPautaStatusRepository.findById(statusId).orElse(null);
        if (status != null) {
            if (status.getStatus() == CpfStatus.UNABLE_TO_VOTE) {
                votoRejeitadoCounter.increment();
                throw new NotFoundException("Associado não pode votar");
            }
        } else {
            CpfClientResult cpfResult = cpfClient.consultar(request.getAssociadoId());
            if (!cpfResult.isValid() || cpfResult.getStatus() == CpfStatus.UNABLE_TO_VOTE) {
                associadoPautaStatusRepository.save(new AssociadoPautaStatus(statusId, CpfStatus.UNABLE_TO_VOTE));
                votoRejeitadoCounter.increment();
                throw new NotFoundException("Associado não pode votar");
            }
            associadoPautaStatusRepository.save(new AssociadoPautaStatus(statusId, CpfStatus.ABLE_TO_VOTE));
        }

        Voto voto = new Voto();
        voto.setPauta(pauta);
        voto.setAssociadoId(request.getAssociadoId());
        voto.setValor(request.getVoto());

        try {
            Voto saved = votoRepository.save(voto);
            votoSalvoCounter.increment();
            log.info("Vote {} saved for pauta {}", saved.getId(), pautaId);
            return saved;
        } catch (DataIntegrityViolationException ex) {
            votoRejeitadoCounter.increment();
            throw new ConflictException("Associado já votou nesta pauta");
        }
    }

    @Observed(name = "votacao.voto.resultado")
    public ResultadoResponse resultado(Long pautaId) {
        Sessao sessao = sessaoService.buscarPorPauta(pautaId);
        boolean aberta = sessaoService.sessaoAberta(sessao);
        long sim = votoRepository.countByPautaIdAndValor(pautaId, VotoValor.SIM);
        long nao = votoRepository.countByPautaIdAndValor(pautaId, VotoValor.NAO);
        long total = sim + nao;
        String status = aberta ? "ABERTA" : "ENCERRADA";
        return new ResultadoResponse(pautaId, total, sim, nao, status);
    }
}



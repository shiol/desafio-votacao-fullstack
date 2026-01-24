package com.example.votacao.service;

import com.example.votacao.client.CpfClient;
import com.example.votacao.client.CpfClientResult;
import com.example.votacao.client.CpfStatus;
import com.example.votacao.dto.ResultadoResponse;
import com.example.votacao.dto.VotoRequest;
import com.example.votacao.exception.BadRequestException;
import com.example.votacao.exception.ConflictException;
import com.example.votacao.exception.NotFoundException;
import com.example.votacao.model.AssociadoPautaId;
import com.example.votacao.model.AssociadoPautaStatus;
import com.example.votacao.model.Pauta;
import com.example.votacao.model.Sessao;
import com.example.votacao.model.Voto;
import com.example.votacao.model.VotoValor;
import com.example.votacao.repository.AssociadoPautaStatusRepository;
import com.example.votacao.repository.VotoRepository;
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

    public VotoService(
            VotoRepository votoRepository,
            PautaService pautaService,
            SessaoService sessaoService,
            CpfClient cpfClient,
            AssociadoPautaStatusRepository associadoPautaStatusRepository
    ) {
        this.votoRepository = votoRepository;
        this.pautaService = pautaService;
        this.sessaoService = sessaoService;
        this.cpfClient = cpfClient;
        this.associadoPautaStatusRepository = associadoPautaStatusRepository;
    }

    public Voto votar(Long pautaId, VotoRequest request) {
        Pauta pauta = pautaService.buscarPorId(pautaId);
        Sessao sessao = sessaoService.buscarPorPauta(pautaId);
        if (!sessaoService.sessaoAberta(sessao)) {
            throw new BadRequestException("Sessão encerrada");
        }

        votoRepository.findByPautaIdAndAssociadoId(pautaId, request.getAssociadoId())
                .ifPresent(voto -> {
                    throw new ConflictException("Associado já votou nesta pauta");
                });

        AssociadoPautaId statusId = new AssociadoPautaId(pautaId, request.getAssociadoId());
        AssociadoPautaStatus status = associadoPautaStatusRepository.findById(statusId).orElse(null);
        if (status != null) {
            if (status.getStatus() == CpfStatus.UNABLE_TO_VOTE) {
                throw new NotFoundException("Associado não pode votar");
            }
        } else {
            CpfClientResult cpfResult = cpfClient.consultar(request.getAssociadoId());
            if (!cpfResult.isValid() || cpfResult.getStatus() == CpfStatus.UNABLE_TO_VOTE) {
                associadoPautaStatusRepository.save(new AssociadoPautaStatus(statusId, CpfStatus.UNABLE_TO_VOTE));
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
            log.info("Vote {} saved for pauta {}", saved.getId(), pautaId);
            return saved;
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Associado já votou nesta pauta");
        }
    }

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

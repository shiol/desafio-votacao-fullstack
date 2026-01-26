package com.soya.votacao.repository;

import com.soya.votacao.model.Sessao;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessaoRepository extends JpaRepository<Sessao, Long> {
    Optional<Sessao> findByPautaId(Long pautaId);

    void deleteByPautaId(Long pautaId);
}


package com.example.votacao.repository;

import com.example.votacao.model.Sessao;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessaoRepository extends JpaRepository<Sessao, Long> {
    Optional<Sessao> findByPautaId(Long pautaId);

    void deleteByPautaId(Long pautaId);
}

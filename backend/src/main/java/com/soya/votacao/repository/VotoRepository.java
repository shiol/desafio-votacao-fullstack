package com.soya.votacao.repository;

import com.soya.votacao.model.Voto;
import com.soya.votacao.model.VotoValor;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VotoRepository extends JpaRepository<Voto, Long> {
    Optional<Voto> findByPautaIdAndAssociadoId(Long pautaId, String associadoId);

    long countByPautaIdAndValor(Long pautaId, VotoValor valor);

    @Query("select count(v.id) from Voto v where v.pauta.id = :pautaId")
    long countByPautaId(@Param("pautaId") Long pautaId);

    void deleteByPautaId(Long pautaId);
}


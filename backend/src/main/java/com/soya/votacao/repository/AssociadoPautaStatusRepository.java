package com.soya.votacao.repository;

import com.soya.votacao.model.AssociadoPautaId;
import com.soya.votacao.model.AssociadoPautaStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssociadoPautaStatusRepository extends JpaRepository<AssociadoPautaStatus, AssociadoPautaId> {
}


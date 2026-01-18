package com.example.votacao.repository;

import com.example.votacao.model.AssociadoPautaId;
import com.example.votacao.model.AssociadoPautaStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssociadoPautaStatusRepository extends JpaRepository<AssociadoPautaStatus, AssociadoPautaId> {
}

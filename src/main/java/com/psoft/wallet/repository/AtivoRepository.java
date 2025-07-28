package com.psoft.wallet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.psoft.wallet.model.Ativo;
import java.util.Optional;

public interface AtivoRepository extends JpaRepository<Ativo, Long> {
    Optional<Ativo> findByNome(String nome);
}
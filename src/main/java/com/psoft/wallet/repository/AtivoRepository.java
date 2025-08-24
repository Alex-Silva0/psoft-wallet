package com.psoft.wallet.repository;

import com.psoft.wallet.model.Ativo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AtivoRepository extends JpaRepository<Ativo, Long> {
    Optional<Ativo> findByNome(String nome);
    List<Ativo> findByDisponivel(boolean disponivel);
}
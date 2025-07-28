package com.psoft.wallet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.psoft.wallet.model.Ativo;
import java.util.Optional;
import java.util.List;

public interface AtivoRepository extends JpaRepository<Ativo, Long> {
    Optional<Ativo> findByNome(String nome);
    
    List<Ativo> findByDisponivel(boolean disponivel);
    
    List<Ativo> findByDisponivelTrue();
    
    List<Ativo> findByDisponivelFalse();
}
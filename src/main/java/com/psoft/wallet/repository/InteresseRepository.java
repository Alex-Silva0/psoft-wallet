package com.psoft.wallet.repository;

import com.psoft.wallet.model.Ativo;
import com.psoft.wallet.model.Cliente;
import com.psoft.wallet.model.Interesse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InteresseRepository extends JpaRepository<Interesse, Long> {
    Optional<Interesse> findByClienteAndAtivo(Cliente cliente, Ativo ativo);
    List<Interesse> findAllByAtivoAndPrecoNoRegistroIsNotNull(Ativo ativo);
    List<Interesse> findAllByAtivoAndPrecoNoRegistroIsNull(Ativo ativo);
}
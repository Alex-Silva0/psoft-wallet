package com.psoft.wallet.repository;

import com.psoft.wallet.enums.TipoInteresse;
import com.psoft.wallet.model.Ativo;
import com.psoft.wallet.model.Cliente;
import com.psoft.wallet.model.Interesse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InteresseRepository extends JpaRepository<Interesse, Long> {
    List<Interesse> findByAtivoAndTipo(Ativo ativo, TipoInteresse tipo);
    Optional<Interesse> findByClienteAndAtivoAndTipo(Cliente cliente, Ativo ativo, TipoInteresse tipo);
}
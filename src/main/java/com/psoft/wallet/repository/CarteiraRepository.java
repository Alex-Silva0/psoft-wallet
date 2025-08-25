package com.psoft.wallet.repository;

import com.psoft.wallet.model.Ativo;
import com.psoft.wallet.model.Carteira;
import com.psoft.wallet.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CarteiraRepository extends JpaRepository<Carteira, Long> {
    List<Carteira> findAllByCliente(Cliente cliente);
    Optional<Carteira> findByClienteAndAtivo(Cliente cliente, Ativo ativo);
}

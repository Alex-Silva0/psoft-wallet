package com.psoft.wallet.repository;

import com.psoft.wallet.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByCodigoAcesso(String codigoAcesso);
}
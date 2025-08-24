package com.psoft.wallet.repository;

import com.psoft.wallet.model.Compra;
import com.psoft.wallet.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompraRepository extends JpaRepository<Compra, Long> {
    List<Compra> findAllByCliente(Cliente cliente);
}
package com.psoft.wallet.repository;

import com.psoft.wallet.model.Resgate;
import com.psoft.wallet.model.Cliente;
import com.psoft.wallet.enums.EstadoResgate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResgateRepository extends JpaRepository<Resgate, Long> {
    
    List<Resgate> findAllByCliente(Cliente cliente);
    
    List<Resgate> findAllByEstado(EstadoResgate estado);
    
    List<Resgate> findAllByClienteAndEstado(Cliente cliente, EstadoResgate estado);
}

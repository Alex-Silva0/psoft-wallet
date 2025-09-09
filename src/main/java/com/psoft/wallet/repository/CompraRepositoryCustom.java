package com.psoft.wallet.repository;

import com.psoft.wallet.enums.EstadoCompra;
import com.psoft.wallet.enums.TipoAtivo;
import com.psoft.wallet.model.Compra;

import java.time.LocalDate;
import java.util.List;

public interface CompraRepositoryCustom {
    List<Compra> findWithFilters(Long clienteId, TipoAtivo tipoAtivo, LocalDate dataInicio, LocalDate dataFim, EstadoCompra estado);
}
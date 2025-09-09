package com.psoft.wallet.repository;

import com.psoft.wallet.enums.EstadoResgate;
import com.psoft.wallet.enums.TipoAtivo;
import com.psoft.wallet.model.Resgate;

import java.time.LocalDate;
import java.util.List;

public interface ResgateRepositoryCustom {
    List<Resgate> findWithFilters(Long clienteId, TipoAtivo tipoAtivo, LocalDate dataInicio, LocalDate dataFim, EstadoResgate estado);
}
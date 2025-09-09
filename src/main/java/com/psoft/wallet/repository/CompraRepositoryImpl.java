package com.psoft.wallet.repository;

import com.psoft.wallet.enums.EstadoCompra;
import com.psoft.wallet.enums.TipoAtivo;
import com.psoft.wallet.model.Ativo;
import com.psoft.wallet.model.Compra;
import com.psoft.wallet.model.Cliente;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CompraRepositoryImpl implements CompraRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Compra> findWithFilters(Long clienteId, TipoAtivo tipoAtivo, LocalDate dataInicio, LocalDate dataFim, EstadoCompra estado) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Compra> query = cb.createQuery(Compra.class);
        Root<Compra> compra = query.from(Compra.class);
        Join<Compra, Ativo> ativo = compra.join("ativo");
        Join<Compra, Cliente> cliente = compra.join("cliente");

        List<Predicate> predicates = new ArrayList<>();

        if (clienteId != null) {
            predicates.add(cb.equal(cliente.get("id"), clienteId));
        }
        if (tipoAtivo != null) {
            predicates.add(cb.equal(ativo.get("tipo"), tipoAtivo));
        }
        if (dataInicio != null) {
            predicates.add(cb.greaterThanOrEqualTo(compra.get("dataSolicitacao"), dataInicio.atStartOfDay()));
        }
        if (dataFim != null) {
            predicates.add(cb.lessThanOrEqualTo(compra.get("dataSolicitacao"), dataFim.atTime(23, 59, 59)));
        }
        if (estado != null) {
            predicates.add(cb.equal(compra.get("estado"), estado));
        }

        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(compra.get("dataSolicitacao")));

        return entityManager.createQuery(query).getResultList();
    }
}
package com.psoft.wallet.repository;

import com.psoft.wallet.enums.EstadoResgate;
import com.psoft.wallet.enums.TipoAtivo;
import com.psoft.wallet.model.Ativo;
import com.psoft.wallet.model.Cliente;
import com.psoft.wallet.model.Resgate;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ResgateRepositoryImpl implements ResgateRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Resgate> findWithFilters(Long clienteId, TipoAtivo tipoAtivo, LocalDate dataInicio, LocalDate dataFim, EstadoResgate estado) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Resgate> query = cb.createQuery(Resgate.class);
        Root<Resgate> resgate = query.from(Resgate.class);
        Join<Resgate, Ativo> ativo = resgate.join("ativo");
        Join<Resgate, Cliente> cliente = resgate.join("cliente");

        List<Predicate> predicates = new ArrayList<>();

        if (clienteId != null) {
            predicates.add(cb.equal(cliente.get("id"), clienteId));
        }
        if (tipoAtivo != null) {
            predicates.add(cb.equal(ativo.get("tipo"), tipoAtivo));
        }
        if (dataInicio != null) {
            predicates.add(cb.greaterThanOrEqualTo(resgate.get("dataSolicitacao"), dataInicio.atStartOfDay()));
        }
        if (dataFim != null) {
            predicates.add(cb.lessThanOrEqualTo(resgate.get("dataSolicitacao"), dataFim.atTime(23, 59, 59)));
        }
        if (estado != null) {
            predicates.add(cb.equal(resgate.get("estado"), estado));
        }

        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(resgate.get("dataSolicitacao")));

        return entityManager.createQuery(query).getResultList();
    }
}
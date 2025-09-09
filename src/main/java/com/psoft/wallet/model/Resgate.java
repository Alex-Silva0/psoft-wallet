package com.psoft.wallet.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.psoft.wallet.enums.EstadoResgate;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Resgate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ativo_id", nullable = false)
    private Ativo ativo;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal valorUnitario;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal valorTotal;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal valorAquisicao;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal lucro;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal imposto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoResgate estado;

    @Column(nullable = false)
    private LocalDateTime dataSolicitacao;

    private LocalDateTime dataConfirmacao;

    private LocalDateTime dataFinalizacao;
}

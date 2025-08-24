package com.psoft.wallet.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.psoft.wallet.enums.TipoAtivo;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Ativo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do ativo não pode ser vazio.")
    @Column(nullable = false, unique = true)
    private String nome;

    @NotNull(message = "O tipo do ativo não pode ser nulo.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAtivo tipo;

    private String descricao;
    private boolean disponivel;
    @NotNull(message = "O valor atual do ativo não pode ser nulo.")
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal valorAtual;
}

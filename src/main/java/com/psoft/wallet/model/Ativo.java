package com.psoft.wallet.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Ativo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Enumerated(EnumType.STRING)
    private TipoAtivo tipo;

    private String descricao;
    private boolean disponivel;
    private float valorAtual;
}

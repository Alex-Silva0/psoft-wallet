package com.psoft.wallet.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Entity
@Data
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomeCompleto;
    private String enderecoPrincipal;

    @Enumerated(EnumType.STRING)
    private TipoPlano plano;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String codigoAcesso;
}
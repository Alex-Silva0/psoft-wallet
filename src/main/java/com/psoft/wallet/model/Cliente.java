package com.psoft.wallet.model;

import jakarta.persistence.*;
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
    
    private String codigoAcesso; // 6 dígitos
} 
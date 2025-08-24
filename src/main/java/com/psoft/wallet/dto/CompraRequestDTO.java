package com.psoft.wallet.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompraRequestDTO {
    private Long ativoId;
    private Integer quantidade;
    private String codigoAcessoCliente;
}
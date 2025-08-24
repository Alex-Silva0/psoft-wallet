package com.psoft.wallet.dto;

import com.psoft.wallet.enums.TipoInteresse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InteresseDTO {
    private Long ativoId;
    private String codigoAcessoCliente;
    private TipoInteresse tipoInteresse;
}
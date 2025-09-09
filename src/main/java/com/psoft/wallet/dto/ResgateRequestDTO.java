package com.psoft.wallet.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResgateRequestDTO {
    
    @NotNull(message = "O ID do ativo não pode ser nulo.")
    private Long ativoId;
    
    @NotNull(message = "A quantidade não pode ser nula.")
    @Positive(message = "A quantidade deve ser positiva.")
    private Integer quantidade;
    
    @NotNull(message = "O código de acesso não pode ser nulo.")
    private String codigoAcesso;
}

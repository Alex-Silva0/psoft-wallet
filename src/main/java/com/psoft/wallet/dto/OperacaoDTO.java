package com.psoft.wallet.dto;

import com.psoft.wallet.enums.TipoAtivo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperacaoDTO {
    private String tipoOperacao;
    private Long operacaoId;
    private String nomeAtivo;
    private TipoAtivo tipoAtivo;
    private Integer quantidade;
    private BigDecimal valorTotal;
    private String status;
    private LocalDateTime data;
}
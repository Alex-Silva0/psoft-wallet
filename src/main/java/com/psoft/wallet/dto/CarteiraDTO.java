package com.psoft.wallet.dto;

import com.psoft.wallet.enums.TipoAtivo;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class CarteiraDTO {
    private Long ativoId;
    private String nomeAtivo;
    private TipoAtivo tipoAtivo;
    private Integer quantidade;
    private BigDecimal valorAquisicao;
    private BigDecimal valorAtual;
    private BigDecimal desempenho;
    private LocalDateTime dataEntradaCarteira;
}

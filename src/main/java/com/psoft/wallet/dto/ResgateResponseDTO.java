package com.psoft.wallet.dto;

import com.psoft.wallet.enums.EstadoResgate;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ResgateResponseDTO {
    
    private Long id;
    private Long clienteId;
    private String nomeCliente;
    private Long ativoId;
    private String nomeAtivo;
    private Integer quantidade;
    private BigDecimal valorUnitario;
    private BigDecimal valorTotal;
    private BigDecimal valorAquisicao;
    private BigDecimal lucro;
    private BigDecimal imposto;
    private EstadoResgate estado;
    private LocalDateTime dataSolicitacao;
    private LocalDateTime dataConfirmacao;
    private LocalDateTime dataFinalizacao;
}

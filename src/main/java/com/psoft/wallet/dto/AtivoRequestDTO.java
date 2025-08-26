package com.psoft.wallet.dto;

import com.psoft.wallet.enums.TipoAtivo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

public class AtivoRequestDTO {
    @NotBlank(message = "Nome é obrigatório")
    private String nome;
    
    @NotNull(message = "Tipo é obrigatório")
    private TipoAtivo tipo;
    
    @NotBlank(message = "Descrição é obrigatória")
    private String descricao;
    
    @NotNull(message = "Disponibilidade é obrigatória")
    private Boolean disponivel;
    
    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.0", inclusive = false, message = "Valor deve ser maior que zero")
    private BigDecimal valor;

    public AtivoRequestDTO() {}

    public AtivoRequestDTO(String nome, TipoAtivo tipo, String descricao, Boolean disponivel, BigDecimal valor) {
        this.nome = nome;
        this.tipo = tipo;
        this.descricao = descricao;
        this.disponivel = disponivel;
        this.valor = valor;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public TipoAtivo getTipo() { return tipo; }
    public void setTipo(TipoAtivo tipo) { this.tipo = tipo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Boolean getDisponivel() { return disponivel; }
    public void setDisponivel(Boolean disponivel) { this.disponivel = disponivel; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
}

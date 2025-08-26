package com.psoft.wallet.dto;

import com.psoft.wallet.enums.TipoAtivo;
import java.math.BigDecimal;

public class AtivoResponseDTO {
    private Long id;
    private String nome;
    private TipoAtivo tipo;
    private String descricao;
    private Boolean disponivel;
    private BigDecimal valorAtual;

    public AtivoResponseDTO() {}

    public AtivoResponseDTO(Long id, String nome, TipoAtivo tipo, String descricao, Boolean disponivel, BigDecimal valorAtual) {
        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
        this.descricao = descricao;
        this.disponivel = disponivel;
        this.valorAtual = valorAtual;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public TipoAtivo getTipo() { return tipo; }
    public void setTipo(TipoAtivo tipo) { this.tipo = tipo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Boolean getDisponivel() { return disponivel; }
    public void setDisponivel(Boolean disponivel) { this.disponivel = disponivel; }

    public BigDecimal getValorAtual() { return valorAtual; }
    public void setValorAtual(BigDecimal valorAtual) { this.valorAtual = valorAtual; }
}

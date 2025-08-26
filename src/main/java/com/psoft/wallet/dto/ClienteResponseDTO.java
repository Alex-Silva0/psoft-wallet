package com.psoft.wallet.dto;

import com.psoft.wallet.enums.TipoPlano;

public class ClienteResponseDTO {
    private Long id;
    private String nomeCompleto;
    private String enderecoPrincipal;
    private TipoPlano plano;

    public ClienteResponseDTO() {}

    public ClienteResponseDTO(Long id, String nomeCompleto, String enderecoPrincipal, TipoPlano plano) {
        this.id = id;
        this.nomeCompleto = nomeCompleto;
        this.enderecoPrincipal = enderecoPrincipal;
        this.plano = plano;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNomeCompleto() { return nomeCompleto; }
    public void setNomeCompleto(String nomeCompleto) { this.nomeCompleto = nomeCompleto; }

    public String getEnderecoPrincipal() { return enderecoPrincipal; }
    public void setEnderecoPrincipal(String enderecoPrincipal) { this.enderecoPrincipal = enderecoPrincipal; }

    public TipoPlano getPlano() { return plano; }
    public void setPlano(TipoPlano plano) { this.plano = plano; }
}

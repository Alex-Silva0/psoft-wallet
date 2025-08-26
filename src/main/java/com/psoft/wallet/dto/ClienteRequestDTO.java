package com.psoft.wallet.dto;

import com.psoft.wallet.enums.TipoPlano;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class ClienteRequestDTO {
    @NotBlank(message = "Nome completo é obrigatório")
    private String nomeCompleto;
    
    @NotBlank(message = "Endereço principal é obrigatório")
    private String enderecoPrincipal;
    
    @NotNull(message = "Plano é obrigatório")
    private TipoPlano plano;
    
    @NotBlank(message = "Código de acesso é obrigatório")
    @Pattern(regexp = "^\\d{6}$", message = "Código de acesso deve ter exatamente 6 dígitos")
    private String codigoAcesso;

    public ClienteRequestDTO() {}

    public ClienteRequestDTO(String nomeCompleto, String enderecoPrincipal, TipoPlano plano, String codigoAcesso) {
        this.nomeCompleto = nomeCompleto;
        this.enderecoPrincipal = enderecoPrincipal;
        this.plano = plano;
        this.codigoAcesso = codigoAcesso;
    }

    public String getNomeCompleto() { return nomeCompleto; }
    public void setNomeCompleto(String nomeCompleto) { this.nomeCompleto = nomeCompleto; }

    public String getEnderecoPrincipal() { return enderecoPrincipal; }
    public void setEnderecoPrincipal(String enderecoPrincipal) { this.enderecoPrincipal = enderecoPrincipal; }

    public TipoPlano getPlano() { return plano; }
    public void setPlano(TipoPlano plano) { this.plano = plano; }

    public String getCodigoAcesso() { return codigoAcesso; }
    public void setCodigoAcesso(String codigoAcesso) { this.codigoAcesso = codigoAcesso; }
}

package com.psoft.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psoft.wallet.dto.InteresseDTO;
import com.psoft.wallet.enums.TipoAtivo;
import com.psoft.wallet.enums.TipoInteresse;
import com.psoft.wallet.enums.TipoPlano;
import com.psoft.wallet.model.Ativo;
import com.psoft.wallet.model.Cliente;
import com.psoft.wallet.repository.AtivoRepository;
import com.psoft.wallet.repository.ClienteRepository;
import com.psoft.wallet.repository.InteresseRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class InteresseControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    InteresseRepository interesseRepository;

    @Autowired
    AtivoRepository ativoRepository;

    @Autowired
    ClienteRepository clienteRepository;

    Cliente clientePremium;
    Cliente clienteNormal;
    Ativo ativoDisponivel;
    Ativo ativoIndisponivel;

    @BeforeEach
    void setUp() {
        clientePremium = new Cliente();
        clientePremium.setNomeCompleto("Cliente Premium");
        clientePremium.setPlano(TipoPlano.PREMIUM);
        clientePremium.setCodigoAcesso("111111");
        clienteRepository.save(clientePremium);

        clienteNormal = new Cliente();
        clienteNormal.setNomeCompleto("Cliente Normal");
        clienteNormal.setPlano(TipoPlano.NORMAL);
        clienteNormal.setCodigoAcesso("222222");
        clienteRepository.save(clienteNormal);

        ativoDisponivel = new Ativo();
        ativoDisponivel.setNome("Ação Disponível");
        ativoDisponivel.setTipo(TipoAtivo.ACAO);
        ativoDisponivel.setDisponivel(true);
        ativoDisponivel.setValorAtual(new BigDecimal("100.00"));
        ativoRepository.save(ativoDisponivel);

        ativoIndisponivel = new Ativo();
        ativoIndisponivel.setNome("Ação Indisponível");
        ativoIndisponivel.setTipo(TipoAtivo.ACAO);
        ativoIndisponivel.setDisponivel(false);
        ativoIndisponivel.setValorAtual(new BigDecimal("50.00"));
        ativoRepository.save(ativoIndisponivel);
    }

    @AfterEach
    void tearDown() {
        interesseRepository.deleteAll();
        ativoRepository.deleteAll();
        clienteRepository.deleteAll();
    }

    // US06 - Testes de Interesse em Variação de Preço

    @Test
    void quandoRegistrarInteresseVariacaoPreco_comClientePremiumEAtivoDisponivel_entaoRetornaCreated() throws Exception {
        InteresseDTO dto = new InteresseDTO();
        dto.setAtivoId(ativoDisponivel.getId());
        dto.setCodigoAcessoCliente(clientePremium.getCodigoAcesso());
        dto.setTipoInteresse(TipoInteresse.VARIACAO_PRECO);

        mockMvc.perform(post("/api/interesses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.tipo").value("VARIACAO_PRECO"));

        assertEquals(1, interesseRepository.count());
    }

    @Test
    void quandoRegistrarInteresseVariacaoPreco_comClienteNormal_entaoRetornaForbidden() throws Exception {
        InteresseDTO dto = new InteresseDTO();
        dto.setAtivoId(ativoDisponivel.getId());
        dto.setCodigoAcessoCliente(clienteNormal.getCodigoAcesso());
        dto.setTipoInteresse(TipoInteresse.VARIACAO_PRECO);

        mockMvc.perform(post("/api/interesses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Apenas clientes Premium podem registrar interesse na variação de preço."));
    }

    // US07 - Testes de Interesse em Disponibilidade

    @Test
    void quandoRegistrarInteresseDisponibilidade_comAtivoIndisponivel_entaoRetornaCreated() throws Exception {
        InteresseDTO dto = new InteresseDTO();
        dto.setAtivoId(ativoIndisponivel.getId());
        dto.setCodigoAcessoCliente(clienteNormal.getCodigoAcesso());
        dto.setTipoInteresse(TipoInteresse.DISPONIBILIDADE);

        mockMvc.perform(post("/api/interesses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.tipo").value("DISPONIBILIDADE"));

        assertEquals(1, interesseRepository.count());
    }

    // Testes Gerais

    @Test
    void quandoRemoverInteresse_comDadosValidos_entaoRetornaNoContent() throws Exception {
        // Given: Register an interest first
        quandoRegistrarInteresseDisponibilidade_comAtivoIndisponivel_entaoRetornaCreated();
        assertEquals(1, interesseRepository.count());

        // When & Then: Remove the interest
        InteresseDTO dto = new InteresseDTO();
        dto.setAtivoId(ativoIndisponivel.getId());
        dto.setCodigoAcessoCliente(clienteNormal.getCodigoAcesso());
        dto.setTipoInteresse(TipoInteresse.DISPONIBILIDADE);

        mockMvc.perform(delete("/api/interesses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent());

        assertFalse(interesseRepository.findByClienteAndAtivoAndTipo(clienteNormal, ativoIndisponivel, TipoInteresse.DISPONIBILIDADE).isPresent());
    }
}
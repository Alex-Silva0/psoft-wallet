package com.psoft.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psoft.wallet.dto.CompraRequestDTO;
import com.psoft.wallet.enums.EstadoCompra;
import com.psoft.wallet.enums.TipoAtivo;
import com.psoft.wallet.enums.TipoPlano;
import com.psoft.wallet.model.Ativo;
import com.psoft.wallet.model.Cliente;
import com.psoft.wallet.model.Compra;
import com.psoft.wallet.repository.AtivoRepository;
import com.psoft.wallet.repository.ClienteRepository;
import com.psoft.wallet.repository.CompraRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CompraControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    AtivoRepository ativoRepository;

    @Autowired
    ClienteRepository clienteRepository;

    @Autowired
    CompraRepository compraRepository;

    Cliente cliente;
    Ativo ativo;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setNomeCompleto("Cliente Teste");
        cliente.setPlano(TipoPlano.PREMIUM);
        cliente.setCodigoAcesso("112233");
        cliente.setSaldo(new BigDecimal("1000.00"));
        clienteRepository.save(cliente);

        ativo = new Ativo();
        ativo.setNome("Ação Teste");
        ativo.setTipo(TipoAtivo.ACAO);
        ativo.setDisponivel(true);
        ativo.setValorAtual(new BigDecimal("100.00"));
        ativoRepository.save(ativo);
    }

    @AfterEach
    void tearDown() {
        compraRepository.deleteAll();
        ativoRepository.deleteAll();
        clienteRepository.deleteAll();
    }

    @Test
    void quandoSolicitarCompra_comDadosValidos_entaoRetornaCompraCriada() throws Exception {
        // Given
        CompraRequestDTO compraRequest = new CompraRequestDTO();
        compraRequest.setAtivoId(ativo.getId());
        compraRequest.setQuantidade(5);
        compraRequest.setCodigoAcessoCliente("112233");

        // When & Then
        mockMvc.perform(post("/api/compras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(compraRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ativo.id").value(ativo.getId()))
                .andExpect(jsonPath("$.cliente.id").value(cliente.getId()))
                .andExpect(jsonPath("$.quantidade").value(5))
                .andExpect(jsonPath("$.valorTotal").value(500.00))
                .andExpect(jsonPath("$.estado").value("SOLICITADO"));

        // VERIFICAÇÃO: O saldo do cliente NÃO deve ser debitado na solicitação inicial.
        Cliente clienteAtualizado = clienteRepository.findById(cliente.getId()).orElseThrow();
        // Saldo inicial (1000.00) deve permanecer o mesmo.
        assertEquals(0, new BigDecimal("1000.00").compareTo(clienteAtualizado.getSaldo()), "O saldo do cliente não deveria ter sido alterado na solicitação.");
    }

    @Test
    void quandoSolicitarCompra_comSaldoInsuficiente_entaoRetornaBadRequest() throws Exception {
        // Given
        cliente.setSaldo(new BigDecimal("50.00")); // Saldo menor que o valor da compra
        clienteRepository.save(cliente);

        CompraRequestDTO compraRequest = new CompraRequestDTO();
        compraRequest.setAtivoId(ativo.getId());
        compraRequest.setQuantidade(1); // Valor da compra = 100.00
        compraRequest.setCodigoAcessoCliente("112233");

        // When & Then
        mockMvc.perform(post("/api/compras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(compraRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Saldo insuficiente para realizar a compra."));
    }

    @Test
    void quandoSolicitarCompra_comAtivoIndisponivel_entaoRetornaBadRequest() throws Exception {
        // Given
        ativo.setDisponivel(false);
        ativoRepository.save(ativo);

        CompraRequestDTO compraRequest = new CompraRequestDTO();
        compraRequest.setAtivoId(ativo.getId());
        compraRequest.setQuantidade(1);
        compraRequest.setCodigoAcessoCliente("112233");

        // When & Then
        mockMvc.perform(post("/api/compras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(compraRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("O ativo 'Ação Teste' não está disponível para compra."));
    }

    @Test
    void quandoSolicitarCompra_comClienteInexistente_entaoRetornaNotFound() throws Exception {
        // Given
        CompraRequestDTO compraRequest = new CompraRequestDTO();
        compraRequest.setAtivoId(ativo.getId());
        compraRequest.setQuantidade(1);
        compraRequest.setCodigoAcessoCliente("999999"); // Código inexistente

        // When & Then
        mockMvc.perform(post("/api/compras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(compraRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void quandoListarComprasDoCliente_comComprasExistentes_entaoRetornaListaDeCompras() throws Exception {
        // Given
        Compra compra1 = new Compra();
        compra1.setCliente(cliente);
        compra1.setAtivo(ativo);
        compra1.setQuantidade(1);
        compra1.setValorTotal(new BigDecimal("100.00"));
        compra1.setValorUnitarioNaCompra(new BigDecimal("100.00"));
        compra1.setEstado(EstadoCompra.SOLICITADO);
        compra1.setDataSolicitacao(LocalDateTime.now());
        compraRepository.save(compra1);

        // When & Then
        mockMvc.perform(get("/api/compras/cliente/{codigoAcesso}", "112233"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(compra1.getId()));
    }

    @Test
    void quandoListarComprasDoCliente_semCompras_entaoRetornaListaVazia() throws Exception {
        // Given: Nenhuma compra foi criada para o cliente

        // When & Then
        mockMvc.perform(get("/api/compras/cliente/{codigoAcesso}", "112233"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void quandoListarComprasDoCliente_comCodigoInexistente_entaoRetornaNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/compras/cliente/{codigoAcesso}", "999999"))
                .andExpect(status().isNotFound());
    }
}
package com.psoft.wallet.controller;

import com.psoft.wallet.enums.TipoAtivo;
import com.psoft.wallet.enums.TipoPlano;
import com.psoft.wallet.model.Ativo;
import com.psoft.wallet.model.Carteira;
import com.psoft.wallet.model.Cliente;
import com.psoft.wallet.repository.AtivoRepository;
import com.psoft.wallet.repository.CarteiraRepository;
import com.psoft.wallet.repository.ClienteRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CarteiraControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AtivoRepository ativoRepository;

    @Autowired
    ClienteRepository clienteRepository;

    @Autowired
    CarteiraRepository carteiraRepository;

    Cliente cliente;
    Ativo ativo1;
    Ativo ativo2;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setNomeCompleto("Cliente Teste");
        cliente.setPlano(TipoPlano.PREMIUM);
        cliente.setCodigoAcesso("112233");
        cliente.setSaldo(new BigDecimal("1000.00"));
        clienteRepository.save(cliente);

        ativo1 = new Ativo();
        ativo1.setNome("Ação Teste 1");
        ativo1.setTipo(TipoAtivo.ACAO);
        ativo1.setDisponivel(true);
        ativo1.setValorAtual(new BigDecimal("100.00"));
        ativoRepository.save(ativo1);

        ativo2 = new Ativo();
        ativo2.setNome("Criptomoeda Teste");
        ativo2.setTipo(TipoAtivo.CRIPTOMOEDA);
        ativo2.setDisponivel(true);
        ativo2.setValorAtual(new BigDecimal("50.00"));
        ativoRepository.save(ativo2);
    }

    @AfterEach
    void tearDown() {
        carteiraRepository.deleteAll();
        ativoRepository.deleteAll();
        clienteRepository.deleteAll();
    }

    @Test
    void quandoVisualizarCarteira_comAtivosExistentes_entaoRetornaListaDeAtivos() throws Exception {
        // Given: Criar ativos na carteira
        Carteira carteira1 = new Carteira();
        carteira1.setCliente(cliente);
        carteira1.setAtivo(ativo1);
        carteira1.setQuantidade(5);
        carteira1.setValorAquisicao(new BigDecimal("95.00"));
        carteira1.setValorAtual(new BigDecimal("100.00"));
        carteira1.setDesempenho(new BigDecimal("25.00")); // (100-95) * 5
        carteira1.setDataEntradaCarteira(LocalDateTime.now());
        carteira1.setDataUltimaAtualizacao(LocalDateTime.now());
        carteiraRepository.save(carteira1);

        Carteira carteira2 = new Carteira();
        carteira2.setCliente(cliente);
        carteira2.setAtivo(ativo2);
        carteira2.setQuantidade(10);
        carteira2.setValorAquisicao(new BigDecimal("45.00"));
        carteira2.setValorAtual(new BigDecimal("50.00"));
        carteira2.setDesempenho(new BigDecimal("50.00")); // (50-45) * 10
        carteira2.setDataEntradaCarteira(LocalDateTime.now());
        carteira2.setDataUltimaAtualizacao(LocalDateTime.now());
        carteiraRepository.save(carteira2);

        // When & Then
        mockMvc.perform(get("/api/carteira/cliente/{codigoAcesso}", "112233"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nomeAtivo").value("Ação Teste 1"))
                .andExpect(jsonPath("$[0].tipoAtivo").value("ACAO"))
                .andExpect(jsonPath("$[0].quantidade").value(5))
                .andExpect(jsonPath("$[0].valorAquisicao").value(95.00))
                .andExpect(jsonPath("$[0].valorAtual").value(100.00))
                .andExpect(jsonPath("$[0].desempenho").value(25.00))
                .andExpect(jsonPath("$[1].nomeAtivo").value("Criptomoeda Teste"))
                .andExpect(jsonPath("$[1].tipoAtivo").value("CRIPTOMOEDA"))
                .andExpect(jsonPath("$[1].quantidade").value(10))
                .andExpect(jsonPath("$[1].valorAquisicao").value(45.00))
                .andExpect(jsonPath("$[1].valorAtual").value(50.00))
                .andExpect(jsonPath("$[1].desempenho").value(50.00));
    }

    @Test
    void quandoVisualizarCarteira_semAtivos_entaoRetornaListaVazia() throws Exception {
        // Given: Nenhum ativo na carteira

        // When & Then
        mockMvc.perform(get("/api/carteira/cliente/{codigoAcesso}", "112233"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void quandoVisualizarCarteira_comCodigoInexistente_entaoRetornaNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/carteira/cliente/{codigoAcesso}", "999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void quandoVisualizarCarteira_comAtivoUnico_entaoRetornaDadosCorretos() throws Exception {
        // Given: Criar um ativo na carteira
        Carteira carteira = new Carteira();
        carteira.setCliente(cliente);
        carteira.setAtivo(ativo1);
        carteira.setQuantidade(3);
        carteira.setValorAquisicao(new BigDecimal("90.00"));
        carteira.setValorAtual(new BigDecimal("100.00"));
        carteira.setDesempenho(new BigDecimal("30.00")); // (100-90) * 3
        carteira.setDataEntradaCarteira(LocalDateTime.now());
        carteira.setDataUltimaAtualizacao(LocalDateTime.now());
        carteiraRepository.save(carteira);

        // When & Then
        mockMvc.perform(get("/api/carteira/cliente/{codigoAcesso}", "112233"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].ativoId").value(ativo1.getId()))
                .andExpect(jsonPath("$[0].nomeAtivo").value("Ação Teste 1"))
                .andExpect(jsonPath("$[0].tipoAtivo").value("ACAO"))
                .andExpect(jsonPath("$[0].quantidade").value(3))
                .andExpect(jsonPath("$[0].valorAquisicao").value(90.00))
                .andExpect(jsonPath("$[0].valorAtual").value(100.00))
                .andExpect(jsonPath("$[0].desempenho").value(30.00));
    }
}

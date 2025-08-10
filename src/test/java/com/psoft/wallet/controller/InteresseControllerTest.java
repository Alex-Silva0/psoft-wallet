package com.psoft.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psoft.wallet.model.*;
import com.psoft.wallet.repository.AtivoRepository;
import com.psoft.wallet.repository.ClienteRepository;
import com.psoft.wallet.repository.InteresseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class InteresseControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AtivoRepository ativoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private InteresseRepository interesseRepository;

    private MockMvc mockMvc;

    private Cliente clientePremium;
    private Cliente clienteNormal;
    private Ativo acaoDisponivel;
    private Ativo criptoDisponivel;
    private Ativo tesouroDisponivel;
    private Ativo acaoIndisponivel;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        interesseRepository.deleteAll();
        ativoRepository.deleteAll();
        clienteRepository.deleteAll();

        // Clientes
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

        // Ativos
        acaoDisponivel = new Ativo();
        acaoDisponivel.setNome("Ação Disponível");
        acaoDisponivel.setTipo(TipoAtivo.ACAO);
        acaoDisponivel.setDisponivel(true);
        acaoDisponivel.setValorAtual(100.0f);
        ativoRepository.save(acaoDisponivel);

        criptoDisponivel = new Ativo();
        criptoDisponivel.setNome("Cripto Disponível");
        criptoDisponivel.setTipo(TipoAtivo.CRIPTOMOEDA);
        criptoDisponivel.setDisponivel(true);
        criptoDisponivel.setValorAtual(5000.0f);
        ativoRepository.save(criptoDisponivel);

        tesouroDisponivel = new Ativo();
        tesouroDisponivel.setNome("Tesouro Disponível");
        tesouroDisponivel.setTipo(TipoAtivo.TESOURO_DIRETO);
        tesouroDisponivel.setDisponivel(true);
        tesouroDisponivel.setValorAtual(100.0f);
        ativoRepository.save(tesouroDisponivel);

        acaoIndisponivel = new Ativo();
        acaoIndisponivel.setNome("Ação Indisponível");
        acaoIndisponivel.setTipo(TipoAtivo.ACAO);
        acaoIndisponivel.setDisponivel(false);
        acaoIndisponivel.setValorAtual(50.0f);
        ativoRepository.save(acaoIndisponivel);
    }

    // US06 - Testes para marcar interesse em variação de preço

    @Test
    void testClientePremiumMarcaInteresseEmVariacaoPrecoComSucesso() throws Exception {
        mockMvc.perform(post("/interesses/{ativoId}", acaoDisponivel.getId())
                        .param("codigoAcesso", clientePremium.getCodigoAcesso()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cliente.nomeCompleto").value("Cliente Premium"))
                .andExpect(jsonPath("$.ativo.nome").value("Ação Disponível"))
                .andExpect(jsonPath("$.precoNoRegistro").value(100.0));

        Interesse interesse = interesseRepository.findAll().get(0);
        assertThat(interesse.getPrecoNoRegistro(), is(notNullValue()));
        assertEquals(100.0f, interesse.getPrecoNoRegistro());
    }

    @Test
    void testClienteNormalTentaMarcarInteresseEmVariacaoPreco() throws Exception {
        mockMvc.perform(post("/interesses/{ativoId}", acaoDisponivel.getId())
                        .param("codigoAcesso", clienteNormal.getCodigoAcesso()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Funcionalidade disponível apenas para clientes Premium."));
    }

    @Test
    void testClientePremiumTentaMarcarInteresseEmVariacaoPrecoDeTesouroDireto() throws Exception {
        mockMvc.perform(post("/interesses/{ativoId}", tesouroDisponivel.getId())
                        .param("codigoAcesso", clientePremium.getCodigoAcesso()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Interesse por variação de preço só pode ser marcado para Ações ou Criptomoedas."));
    }

    @Test
    void testTentarMarcarInteresseEmVariacaoPrecoDeAtivoIndisponivel() throws Exception {
        // Este cenário deve criar um interesse de DISPONIBILIDADE, não de preço.
        mockMvc.perform(post("/interesses/{ativoId}", acaoIndisponivel.getId())
                        .param("codigoAcesso", clientePremium.getCodigoAcesso()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.precoNoRegistro").doesNotExist());

        Interesse interesse = interesseRepository.findAll().get(0);
        assertNull(interesse.getPrecoNoRegistro());
    }

    @Test
    void testTentarMarcarInteresseDuplicado() throws Exception {
        // Marcar interesse pela primeira vez
        mockMvc.perform(post("/interesses/{ativoId}", acaoDisponivel.getId())
                        .param("codigoAcesso", clientePremium.getCodigoAcesso()))
                .andExpect(status().isCreated());

        // Tentar marcar novamente
        mockMvc.perform(post("/interesses/{ativoId}", acaoDisponivel.getId())
                        .param("codigoAcesso", clientePremium.getCodigoAcesso()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Cliente já possui interesse neste ativo."));
    }

    // US07 - Testes para marcar interesse em disponibilidade

    @Test
    void testClienteNormalMarcaInteresseEmDisponibilidadeComSucesso() throws Exception {
        mockMvc.perform(post("/interesses/{ativoId}", acaoIndisponivel.getId())
                        .param("codigoAcesso", clienteNormal.getCodigoAcesso()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cliente.nomeCompleto").value("Cliente Normal"))
                .andExpect(jsonPath("$.ativo.nome").value("Ação Indisponível"))
                .andExpect(jsonPath("$.precoNoRegistro").doesNotExist());

        Interesse interesse = interesseRepository.findAll().get(0);
        assertNull(interesse.getPrecoNoRegistro());
    }

    @Test
    void testClientePremiumMarcaInteresseEmDisponibilidadeComSucesso() throws Exception {
        mockMvc.perform(post("/interesses/{ativoId}", acaoIndisponivel.getId())
                        .param("codigoAcesso", clientePremium.getCodigoAcesso()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cliente.nomeCompleto").value("Cliente Premium"))
                .andExpect(jsonPath("$.ativo.nome").value("Ação Indisponível"))
                .andExpect(jsonPath("$.precoNoRegistro").doesNotExist());

        Interesse interesse = interesseRepository.findAll().get(0);
        assertNull(interesse.getPrecoNoRegistro());
    }

    @Test
    void testTentarMarcarInteresseEmDisponibilidadeDeAtivoJaDisponivel() throws Exception {
        // Este cenário deve criar um interesse de VARIAÇÃO DE PREÇO, não de disponibilidade.
        mockMvc.perform(post("/interesses/{ativoId}", acaoDisponivel.getId())
                        .param("codigoAcesso", clientePremium.getCodigoAcesso()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.precoNoRegistro").value(100.0));

        Interesse interesse = interesseRepository.findAll().get(0);
        assertThat(interesse.getPrecoNoRegistro(), is(notNullValue()));
    }

    // Testes de Validação Geral

    @Test
    void testMarcarInteresseComCodigoAcessoIncorreto() throws Exception {
        mockMvc.perform(post("/interesses/{ativoId}", acaoDisponivel.getId())
                        .param("codigoAcesso", "999999"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Código de acesso incorreto"));
    }

    @Test
    void testMarcarInteresseSemCodigoAcesso() throws Exception {
        mockMvc.perform(post("/interesses/{ativoId}", acaoDisponivel.getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testMarcarInteresseEmAtivoInexistente() throws Exception {
        mockMvc.perform(post("/interesses/999")
                        .param("codigoAcesso", clientePremium.getCodigoAcesso()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ativo com ID 999 não encontrado"));
    }
}
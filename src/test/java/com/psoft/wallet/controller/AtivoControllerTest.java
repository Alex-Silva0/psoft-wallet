package com.psoft.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psoft.wallet.model.Ativo;
import com.psoft.wallet.model.TipoAtivo;
import com.psoft.wallet.repository.AtivoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class AtivoControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AtivoRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    // US01 - Testes para criar, editar e remover ativos

    @Test
    void testCriarAtivoComSucesso() throws Exception {
        // Given
        Ativo ativo = new Ativo();
        ativo.setNome("Petrobras");
        ativo.setTipo(TipoAtivo.ACAO);
        ativo.setDescricao("Ação da Petrobras");
        ativo.setDisponivel(true);
        ativo.setValorAtual(25.50f);

        // When & Then
        String response = mockMvc.perform(post("/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Petrobras"))
                .andExpect(jsonPath("$.tipo").value("ACAO"))
                .andExpect(jsonPath("$.descricao").value("Ação da Petrobras"))
                .andExpect(jsonPath("$.disponivel").value(true))
                .andExpect(jsonPath("$.valorAtual").value(25.50))
                .andReturn().getResponse().getContentAsString();

        // Verificar se foi salvo no banco
        Ativo ativoSalvo = objectMapper.readValue(response, Ativo.class);
        assertTrue(repository.findById(ativoSalvo.getId()).isPresent());
    }

    @Test
    void testCriarAtivoTesouroDireto() throws Exception {
        // Given
        Ativo tesouro = new Ativo();
        tesouro.setNome("Tesouro Selic 2026");
        tesouro.setTipo(TipoAtivo.TESOURO_DIRETO);
        tesouro.setDescricao("Tesouro Direto Selic 2026");
        tesouro.setDisponivel(true);
        tesouro.setValorAtual(100.00f);

        // When & Then
        mockMvc.perform(post("/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tesouro)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("TESOURO_DIRETO"))
                .andExpect(jsonPath("$.valorAtual").value(100.00));
    }

    @Test
    void testCriarAtivoCriptomoeda() throws Exception {
        // Given
        Ativo cripto = new Ativo();
        cripto.setNome("Bitcoin");
        cripto.setTipo(TipoAtivo.CRIPTOMOEDA);
        cripto.setDescricao("Bitcoin - primeira criptomoeda");
        cripto.setDisponivel(true);
        cripto.setValorAtual(150000.00f);

        // When & Then
        mockMvc.perform(post("/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cripto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("CRIPTOMOEDA"))
                .andExpect(jsonPath("$.valorAtual").value(150000.00));
    }

    @Test
    void testCriarAtivoComNomeDuplicado() throws Exception {
        // Given - Criar primeiro ativo
        Ativo ativo1 = new Ativo();
        ativo1.setNome("Petrobras");
        ativo1.setTipo(TipoAtivo.ACAO);
        ativo1.setValorAtual(25.50f);

        mockMvc.perform(post("/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo1)))
                .andExpect(status().isOk());

        // When & Then - Tentar criar segundo ativo com mesmo nome
        Ativo ativo2 = new Ativo();
        ativo2.setNome("Petrobras");
        ativo2.setTipo(TipoAtivo.ACAO);
        ativo2.setValorAtual(30.00f);

        mockMvc.perform(post("/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Já existe um ativo com o nome 'Petrobras'"));
    }

    @Test
    void testRemoverAtivoComSucesso() throws Exception {
        // Given - Criar um ativo
        Ativo ativo = new Ativo();
        ativo.setNome("Petrobras");
        ativo.setTipo(TipoAtivo.ACAO);
        ativo.setValorAtual(25.50f);

        String response = mockMvc.perform(post("/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Ativo ativoSalvo = objectMapper.readValue(response, Ativo.class);
        Long id = ativoSalvo.getId();

        // When & Then - Remover o ativo
        mockMvc.perform(delete("/ativos/{id}", id))
                .andExpect(status().isOk());

        // Verificar se foi removido do banco
        assertFalse(repository.existsById(id));
    }

    @Test
    void testRemoverAtivoNaoEncontrado() throws Exception {
        // When & Then
        mockMvc.perform(delete("/ativos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ativo com ID 999 não encontrado"));
    }

    // US03 - Testes para atualizar valor de cotação com variação mínima de 1%

    @Test
    void testAtualizarValorAcaoComSucesso() throws Exception {
        // Given - Criar um ativo
        Ativo ativo = new Ativo();
        ativo.setNome("Petrobras");
        ativo.setTipo(TipoAtivo.ACAO);
        ativo.setValorAtual(25.50f);

        String response = mockMvc.perform(post("/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Ativo ativoSalvo = objectMapper.readValue(response, Ativo.class);
        Long id = ativoSalvo.getId();

        // When & Then - Atualizar com variação maior que 1%
        mockMvc.perform(patch("/ativos/{id}/valor", id)
                .param("novoValor", "30.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorAtual").value(30.00));

        // Verificar se foi atualizado no banco
        Ativo ativoAtualizado = repository.findById(id).orElse(null);
        assertNotNull(ativoAtualizado);
        assertEquals(30.00f, ativoAtualizado.getValorAtual(), 0.01f);
    }

    @Test
    void testAtualizarValorCriptomoedaComSucesso() throws Exception {
        // Given - Criar uma criptomoeda
        Ativo bitcoin = new Ativo();
        bitcoin.setNome("Bitcoin");
        bitcoin.setTipo(TipoAtivo.CRIPTOMOEDA);
        bitcoin.setValorAtual(150000.00f);

        String response = mockMvc.perform(post("/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bitcoin)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Ativo ativoSalvo = objectMapper.readValue(response, Ativo.class);
        Long id = ativoSalvo.getId();

        // When & Then - Atualizar com variação maior que 1%
        mockMvc.perform(patch("/ativos/{id}/valor", id)
                .param("novoValor", "160000.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorAtual").value(160000.00));
    }

    @Test
    void testAtualizarValorComVariacaoMenorQue1Porcento() throws Exception {
        // Given - Criar um ativo
        Ativo ativo = new Ativo();
        ativo.setNome("Petrobras");
        ativo.setTipo(TipoAtivo.ACAO);
        ativo.setValorAtual(25.50f);

        String response = mockMvc.perform(post("/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Ativo ativoSalvo = objectMapper.readValue(response, Ativo.class);
        Long id = ativoSalvo.getId();

        // When & Then - Tentar atualizar com variação menor que 1%
        mockMvc.perform(patch("/ativos/{id}/valor", id)
                .param("novoValor", "25.60"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Variação mínima de 1% não atingida"));

        // Verificar se o valor não foi alterado no banco
        Ativo ativoNaoAlterado = repository.findById(id).orElse(null);
        assertNotNull(ativoNaoAlterado);
        assertEquals(25.50f, ativoNaoAlterado.getValorAtual(), 0.01f);
    }

    @Test
    void testAtualizarValorAtivoNaoEncontrado() throws Exception {
        // When & Then
        mockMvc.perform(patch("/ativos/999/valor")
                .param("novoValor", "30.00"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ativo com ID 999 não encontrado"));
    }

    @Test
    void testAtualizarValorComReducaoMaiorQue1Porcento() throws Exception {
        // Given - Criar um ativo
        Ativo ativo = new Ativo();
        ativo.setNome("Petrobras");
        ativo.setTipo(TipoAtivo.ACAO);
        ativo.setValorAtual(25.50f);

        String response = mockMvc.perform(post("/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Ativo ativoSalvo = objectMapper.readValue(response, Ativo.class);
        Long id = ativoSalvo.getId();

        // When & Then - Atualizar com redução maior que 1%
        mockMvc.perform(patch("/ativos/{id}/valor", id)
                .param("novoValor", "20.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorAtual").value(20.00));
    }

    @Test
    void testAtualizarValorComVariacaoExataDe1Porcento() throws Exception {
        // Given - Criar um ativo
        Ativo ativo = new Ativo();
        ativo.setNome("Petrobras");
        ativo.setTipo(TipoAtivo.ACAO);
        ativo.setValorAtual(100.00f);

        String response = mockMvc.perform(post("/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Ativo ativoSalvo = objectMapper.readValue(response, Ativo.class);
        Long id = ativoSalvo.getId();

        // When & Then - Atualizar com variação exata de 1% (deve passar)
        mockMvc.perform(patch("/ativos/{id}/valor", id)
                .param("novoValor", "101.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorAtual").value(101.00));
    }

    @Test
    void testAtualizarValorComVariacaoLigeiramenteMaiorQue1Porcento() throws Exception {
        // Given - Criar um ativo
        Ativo ativo = new Ativo();
        ativo.setNome("Petrobras");
        ativo.setTipo(TipoAtivo.ACAO);
        ativo.setValorAtual(100.00f);

        String response = mockMvc.perform(post("/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Ativo ativoSalvo = objectMapper.readValue(response, Ativo.class);
        Long id = ativoSalvo.getId();

        // When & Then - Atualizar com variação ligeiramente maior que 1%
        mockMvc.perform(patch("/ativos/{id}/valor", id)
                .param("novoValor", "101.01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorAtual").value(101.01));
    }

    @Test
    void testCriarMultiplosAtivos() throws Exception {
        // Given
        Ativo ativo1 = new Ativo();
        ativo1.setNome("Petrobras");
        ativo1.setTipo(TipoAtivo.ACAO);
        ativo1.setValorAtual(25.50f);

        Ativo ativo2 = new Ativo();
        ativo2.setNome("Vale");
        ativo2.setTipo(TipoAtivo.ACAO);
        ativo2.setValorAtual(30.00f);

        Ativo ativo3 = new Ativo();
        ativo3.setNome("Tesouro Selic");
        ativo3.setTipo(TipoAtivo.TESOURO_DIRETO);
        ativo3.setValorAtual(100.00f);

        // When & Then - Criar primeiro ativo
        mockMvc.perform(post("/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Petrobras"));

        // When & Then - Criar segundo ativo
        mockMvc.perform(post("/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Vale"));

        // When & Then - Criar terceiro ativo
        mockMvc.perform(post("/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo3)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Tesouro Selic"))
                .andExpect(jsonPath("$.tipo").value("TESOURO_DIRETO"));

        // Verificar se todos foram salvos
        assertEquals(3, repository.count());
    }

    @Test
    void testAtualizarValorComParametroInvalido() throws Exception {
        // Given - Criar um ativo
        Ativo ativo = new Ativo();
        ativo.setNome("Petrobras");
        ativo.setTipo(TipoAtivo.ACAO);
        ativo.setValorAtual(25.50f);

        String response = mockMvc.perform(post("/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Ativo ativoSalvo = objectMapper.readValue(response, Ativo.class);
        Long id = ativoSalvo.getId();

        // When & Then - Tentar atualizar com parâmetro inválido
        mockMvc.perform(patch("/ativos/{id}/valor", id)
                .param("novoValor", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCriarAtivoComPayloadInvalido() throws Exception {
        // Given - JSON inválido
        String jsonInvalido = "{\"nome\": \"Petrobras\", \"tipo\": \"INVALIDO\"}";

        // When & Then
        mockMvc.perform(post("/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonInvalido))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCriarAtivoComDadosIncompletos() throws Exception {
        // Given - Ativo sem nome
        Ativo ativoInvalido = new Ativo();
        ativoInvalido.setTipo(TipoAtivo.ACAO);
        ativoInvalido.setValorAtual(25.50f);

        // When & Then - Deve funcionar pois o controller não valida, apenas repassa
        mockMvc.perform(post("/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativoInvalido)))
                .andExpect(status().isOk());
    }

    @Test
    void testFluxoCompletoCriarAtualizarRemover() throws Exception {
        // Given - Criar um ativo
        Ativo ativo = new Ativo();
        ativo.setNome("Teste Fluxo");
        ativo.setTipo(TipoAtivo.ACAO);
        ativo.setValorAtual(100.00f);

        // When & Then - 1. Criar ativo
        String response = mockMvc.perform(post("/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Ativo ativoSalvo = objectMapper.readValue(response, Ativo.class);
        Long id = ativoSalvo.getId();

        // Verificar se foi criado
        assertTrue(repository.existsById(id));

        // When & Then - 2. Atualizar valor
        mockMvc.perform(patch("/ativos/{id}/valor", id)
                .param("novoValor", "110.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorAtual").value(110.00));

        // Verificar se foi atualizado
        Ativo ativoAtualizado = repository.findById(id).orElse(null);
        assertNotNull(ativoAtualizado);
        assertEquals(110.00f, ativoAtualizado.getValorAtual(), 0.01f);

        // When & Then - 3. Remover ativo
        mockMvc.perform(delete("/ativos/{id}", id))
                .andExpect(status().isOk());

        // Verificar se foi removido
        assertFalse(repository.existsById(id));
    }
} 
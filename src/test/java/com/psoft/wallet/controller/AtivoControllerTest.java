package com.psoft.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psoft.wallet.dto.AtivoRequestDTO;
import com.psoft.wallet.dto.AtivoResponseDTO;
import com.psoft.wallet.model.Ativo;
import com.psoft.wallet.dto.InteresseDTO;
import com.psoft.wallet.enums.TipoAtivo;
import com.psoft.wallet.enums.TipoInteresse;
import com.psoft.wallet.enums.TipoPlano;
import com.psoft.wallet.model.Cliente;
import com.psoft.wallet.repository.AtivoRepository;
import com.psoft.wallet.repository.ClienteRepository;
import com.psoft.wallet.repository.InteresseRepository;
import com.psoft.wallet.service.InteresseService;
import com.psoft.wallet.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;

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
    private ClienteRepository clienteRepository;

    @Autowired
    private InteresseRepository interesseRepository;

    @Autowired
    private InteresseService interesseService;

    @Autowired
    private ObjectMapper objectMapper;

    @SuppressWarnings("removal")
    @MockBean
    private NotificationService notificationService;

    private MockMvc mockMvc;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        interesseRepository.deleteAll();
        repository.deleteAll();
        clienteRepository.deleteAll();
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    // US01 - Testes para criar, editar e remover ativos

    @Test
    void testCriarAtivoComSucesso() throws Exception {
        // Given
        AtivoRequestDTO ativoRequest = new AtivoRequestDTO();
        ativoRequest.setNome("Petrobras");
        ativoRequest.setTipo(TipoAtivo.ACAO);
        ativoRequest.setDescricao("Ação da Petrobras");
        ativoRequest.setDisponivel(true);
        ativoRequest.setValor(new BigDecimal("25.50"));

        // When & Then
        String response = mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativoRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Petrobras"))
                .andExpect(jsonPath("$.tipo").value("ACAO"))
                .andExpect(jsonPath("$.descricao").value("Ação da Petrobras"))
                .andExpect(jsonPath("$.disponivel").value(true))
                .andExpect(jsonPath("$.valorAtual").value(25.50))
                .andReturn().getResponse().getContentAsString();

        // Verificar se foi salvo no banco
        AtivoResponseDTO ativoSalvo = objectMapper.readValue(response, AtivoResponseDTO.class);
        assertTrue(repository.findById(ativoSalvo.getId()).isPresent());
    }

    @Test
    void testCriarAtivoTesouroDireto() throws Exception {
        // Given
        AtivoRequestDTO tesouro = new AtivoRequestDTO();
        tesouro.setNome("Tesouro Selic 2026");
        tesouro.setTipo(TipoAtivo.TESOURO_DIRETO);
        tesouro.setDescricao("Tesouro Direto Selic 2026");
        tesouro.setDisponivel(true);
        tesouro.setValor(new BigDecimal("100.00"));

        // When & Then
        mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tesouro)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("TESOURO_DIRETO"))
                .andExpect(jsonPath("$.valorAtual").value(100.00));
    }

    @Test
    void testCriarAtivoCriptomoeda() throws Exception {
        // Given
        AtivoRequestDTO cripto = new AtivoRequestDTO();
        cripto.setNome("Bitcoin");
        cripto.setTipo(TipoAtivo.CRIPTOMOEDA);
        cripto.setDescricao("Bitcoin - primeira criptomoeda");
        cripto.setDisponivel(true);
        cripto.setValor(new BigDecimal("150000.00"));

        // When & Then
        mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cripto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("CRIPTOMOEDA"))
                .andExpect(jsonPath("$.valorAtual").value(150000.00));
    }

    @Test
    void testCriarAtivoComNomeDuplicado() throws Exception {
        // Given - Criar primeiro ativo
        AtivoRequestDTO ativo1 = new AtivoRequestDTO();
        ativo1.setNome("Petrobras");
        ativo1.setTipo(TipoAtivo.ACAO);
        ativo1.setDescricao("Ação da Petrobras");
        ativo1.setDisponivel(true);
        ativo1.setValor(new BigDecimal("25.50"));

        mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo1)))
                .andExpect(status().isCreated());

        // When & Then - Tentar criar segundo ativo com mesmo nome
        AtivoRequestDTO ativo2 = new AtivoRequestDTO();
        ativo2.setNome("Petrobras");
        ativo2.setTipo(TipoAtivo.ACAO);
        ativo2.setDescricao("Ação da Petrobras");
        ativo2.setDisponivel(true);
        ativo2.setValor(new BigDecimal("30.00"));

        mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Já existe um ativo com o nome: Petrobras"));
    }

    @Test
    void testRemoverAtivoComSucesso() throws Exception {
        // Given - Criar um ativo
        AtivoRequestDTO ativo = new AtivoRequestDTO();
        ativo.setNome("Petrobras");
        ativo.setTipo(TipoAtivo.ACAO);
        ativo.setDescricao("Ação da Petrobras");
        ativo.setDisponivel(true);
        ativo.setValor(new BigDecimal("25.50"));

        String response = mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        AtivoResponseDTO ativoSalvo = objectMapper.readValue(response, AtivoResponseDTO.class);
        Long id = ativoSalvo.getId();

        // When & Then - Remover o ativo
        mockMvc.perform(delete("/api/ativos/{id}", id))
                .andExpect(status().isNoContent());

        // Verificar se foi removido do banco
        assertFalse(repository.existsById(id));
    }

    @Test
    void testRemoverAtivoNaoEncontrado() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/ativos/999"))
                .andExpect(status().isNotFound());
    }

    // US03 - Testes para atualizar valor de cotação com variação mínima de 1%

    @Test
    void testAtualizarValorAcaoComSucesso() throws Exception {
        // Given - Criar um ativo
        AtivoRequestDTO ativo = new AtivoRequestDTO();
        ativo.setNome("Petrobras");
        ativo.setTipo(TipoAtivo.ACAO);
        ativo.setDescricao("Ação da Petrobras");
        ativo.setDisponivel(true);
        ativo.setValor(new BigDecimal("25.50"));

        String response = mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        AtivoResponseDTO ativoSalvo = objectMapper.readValue(response, AtivoResponseDTO.class);
        Long id = ativoSalvo.getId();

        // When & Then - Atualizar com variação maior que 1%
        mockMvc.perform(patch("/api/ativos/{id}/valor", id)
                .param("novoValor", "30.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorAtual").value(30.00));

        // Verificar se foi atualizado no banco
        Ativo ativoAtualizado = repository.findById(id).orElse(null);
        assertNotNull(ativoAtualizado);
        // Para BigDecimal, a comparação é feita com compareTo. 0 significa que são iguais.
        assertEquals(0, new BigDecimal("30.00").compareTo(ativoAtualizado.getValorAtual()));
    }
    
    @Test
    void testAtualizarValorCriptomoedaComSucesso() throws Exception {
        // Given - Criar uma criptomoeda
        AtivoRequestDTO bitcoin = new AtivoRequestDTO();
        bitcoin.setNome("Bitcoin");
        bitcoin.setTipo(TipoAtivo.CRIPTOMOEDA);
        bitcoin.setDescricao("Bitcoin - primeira criptomoeda");
        bitcoin.setDisponivel(true);
        bitcoin.setValor(new BigDecimal("150000.00"));

        String response = mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bitcoin)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        AtivoResponseDTO ativoSalvo = objectMapper.readValue(response, AtivoResponseDTO.class);
        Long id = ativoSalvo.getId();

        // When & Then - Atualizar com variação maior que 1%
        mockMvc.perform(patch("/api/ativos/{id}/valor", id)
                .param("novoValor", "160000.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorAtual").value(160000.00));
    }

    @Test
    void testAtualizarValorComVariacaoMenorQue1Porcento() throws Exception {
        // Given - Criar um ativo
        AtivoRequestDTO ativo = new AtivoRequestDTO();
        ativo.setNome("Petrobras");
        ativo.setTipo(TipoAtivo.ACAO);
        ativo.setDescricao("Ação da Petrobras");
        ativo.setDisponivel(true);
        ativo.setValor(new BigDecimal("25.50"));

        String response = mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        AtivoResponseDTO ativoSalvo = objectMapper.readValue(response, AtivoResponseDTO.class);
        Long id = ativoSalvo.getId();

        // When & Then - Tentar atualizar com variação menor que 1%
        // NOTA: Este teste falhará até que a lógica de validação de 1% seja implementada no AtivoService.
        mockMvc.perform(patch("/api/ativos/{id}/valor", id)
                .param("novoValor", "25.60"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Variação mínima de 1% não atingida"));

        // Verificar se o valor não foi alterado no banco
        Ativo ativoNaoAlterado = repository.findById(id).orElse(null);
        assertNotNull(ativoNaoAlterado);
        assertEquals(0, new BigDecimal("25.50").compareTo(ativoNaoAlterado.getValorAtual()));
    }

    @Test
    void testAtualizarValorAtivoNaoEncontrado() throws Exception {
        // When & Then
        mockMvc.perform(patch("/api/ativos/999/valor")
                .param("novoValor", "30.00"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAtualizarValorComReducaoMaiorQue1Porcento() throws Exception {
        // Given - Criar um ativo
        AtivoRequestDTO ativo = new AtivoRequestDTO();
        ativo.setNome("Petrobras");
        ativo.setTipo(TipoAtivo.ACAO);
        ativo.setDescricao("Ação da Petrobras");
        ativo.setDisponivel(true);
        ativo.setValor(new BigDecimal("25.50"));

        String response = mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        AtivoResponseDTO ativoSalvo = objectMapper.readValue(response, AtivoResponseDTO.class);
        Long id = ativoSalvo.getId();

        // When & Then - Atualizar com redução maior que 1%
        mockMvc.perform(patch("/api/ativos/{id}/valor", id)
                .param("novoValor", "20.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorAtual").value(20.00));
    }

    @Test
    void testAtualizarValorComVariacaoExataDe1Porcento() throws Exception {
        // Given - Criar um ativo
        AtivoRequestDTO ativo = new AtivoRequestDTO();
        ativo.setNome("Petrobras");
        ativo.setTipo(TipoAtivo.ACAO);
        ativo.setDescricao("Ação da Petrobras");
        ativo.setDisponivel(true);
        ativo.setValor(new BigDecimal("100.00"));

        String response = mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        AtivoResponseDTO ativoSalvo = objectMapper.readValue(response, AtivoResponseDTO.class);
        Long id = ativoSalvo.getId();

        // When & Then - Atualizar com variação exata de 1% (deve passar)
        mockMvc.perform(patch("/api/ativos/{id}/valor", id)
                .param("novoValor", "101.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorAtual").value(101.00));
    }

    @Test
    void testAtualizarValorComVariacaoLigeiramenteMaiorQue1Porcento() throws Exception {
        // Given - Criar um ativo
        AtivoRequestDTO ativo = new AtivoRequestDTO();
        ativo.setNome("Petrobras");
        ativo.setTipo(TipoAtivo.ACAO);
        ativo.setDescricao("Ação da Petrobras");
        ativo.setDisponivel(true);
        ativo.setValor(new BigDecimal("100.00"));

        String response = mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        AtivoResponseDTO ativoSalvo = objectMapper.readValue(response, AtivoResponseDTO.class);
        Long id = ativoSalvo.getId();

        // When & Then - Atualizar com variação ligeiramente maior que 1%
        mockMvc.perform(patch("/api/ativos/{id}/valor", id)
                .param("novoValor", "101.01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorAtual").value(101.01));
    }

    @Test
    void testCriarMultiplosAtivos() throws Exception {
        // Given
        AtivoRequestDTO ativo1 = new AtivoRequestDTO();
        ativo1.setNome("Petrobras");
        ativo1.setTipo(TipoAtivo.ACAO);
        ativo1.setDescricao("Ação da Petrobras");
        ativo1.setDisponivel(true);
        ativo1.setValor(new BigDecimal("25.50"));

        AtivoRequestDTO ativo2 = new AtivoRequestDTO();
        ativo2.setNome("Vale");
        ativo2.setTipo(TipoAtivo.ACAO);
        ativo2.setDescricao("Ação da Vale");
        ativo2.setDisponivel(true);
        ativo2.setValor(new BigDecimal("30.00"));

        AtivoRequestDTO ativo3 = new AtivoRequestDTO();
        ativo3.setNome("Tesouro Selic");
        ativo3.setTipo(TipoAtivo.TESOURO_DIRETO);
        ativo3.setDescricao("Tesouro Direto Selic");
        ativo3.setDisponivel(true);
        ativo3.setValor(new BigDecimal("100.00"));

        // When & Then - Criar primeiro ativo
        mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Petrobras"));

        // When & Then - Criar segundo ativo
        mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Vale"));

        // When & Then - Criar terceiro ativo
        mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo3)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Tesouro Selic"))
                .andExpect(jsonPath("$.tipo").value("TESOURO_DIRETO"));

        // Verificar se todos foram salvos
        assertEquals(3, repository.count());
    }

    @Test
    void testAtualizarValorComParametroInvalido() throws Exception {
        // Given - Criar um ativo
        AtivoRequestDTO ativo = new AtivoRequestDTO();
        ativo.setNome("Petrobras");
        ativo.setTipo(TipoAtivo.ACAO);
        ativo.setDescricao("Ação da Petrobras");
        ativo.setDisponivel(true);
        ativo.setValor(new BigDecimal("25.50"));

        String response = mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        AtivoResponseDTO ativoSalvo = objectMapper.readValue(response, AtivoResponseDTO.class);
        Long id = ativoSalvo.getId();

        // When & Then - Tentar atualizar com parâmetro inválido
        mockMvc.perform(patch("/api/ativos/{id}/valor", id)
                .param("novoValor", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCriarAtivoComPayloadInvalido() throws Exception {
        // Given - JSON inválido
        String jsonInvalido = "{\"nome\": \"Petrobras\", \"tipo\": \"INVALIDO\"}";

        // When & Then
        mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonInvalido))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCriarAtivoComDadosIncompletos() throws Exception {
        // Given - Ativo sem nome
        AtivoRequestDTO ativoInvalido = new AtivoRequestDTO();
        ativoInvalido.setTipo(TipoAtivo.ACAO);
        ativoInvalido.setValor(new BigDecimal("25.50"));

        // When & Then - Deve falhar por causa da validação do DTO
        mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativoInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testFluxoCompletoCriarAtualizarRemover() throws Exception {
        // Given - Criar um ativo
        AtivoRequestDTO ativo = new AtivoRequestDTO();
        ativo.setNome("Teste Fluxo");
        ativo.setTipo(TipoAtivo.ACAO);
        ativo.setDescricao("Ativo para teste de fluxo");
        ativo.setDisponivel(true);
        ativo.setValor(new BigDecimal("100.00"));

        // When & Then - 1. Criar ativo
        String response = mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        AtivoResponseDTO ativoSalvo = objectMapper.readValue(response, AtivoResponseDTO.class);
        Long id = ativoSalvo.getId();

        // Verificar se foi criado
        assertTrue(repository.existsById(id));

        // When & Then - 2. Atualizar valor
        mockMvc.perform(patch("/api/ativos/{id}/valor", id)
                .param("novoValor", "110.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorAtual").value(110.00));

        // Verificar se foi atualizado
        Ativo ativoAtualizado = repository.findById(id).orElse(null);
        assertNotNull(ativoAtualizado);
        assertEquals(0, new BigDecimal("110.00").compareTo(ativoAtualizado.getValorAtual()));

        // When & Then - 3. Remover ativo
        mockMvc.perform(delete("/api/ativos/{id}", id))
                .andExpect(status().isNoContent());

        // Verificar se foi removido
        assertFalse(repository.existsById(id));
    }

    // US02 - Testes para ativar/desativar ativos

    @Test
    void testAtivarAtivoComSucesso() throws Exception {
        // Given - Criar um ativo desativado
        AtivoRequestDTO ativo = new AtivoRequestDTO();
        ativo.setNome("Petrobras");
        ativo.setTipo(TipoAtivo.ACAO);
        ativo.setDescricao("Ação da Petrobras");
        ativo.setDisponivel(false);
        ativo.setValor(new BigDecimal("25.50"));

        String response = mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        AtivoResponseDTO ativoSalvo = objectMapper.readValue(response, AtivoResponseDTO.class);
        Long id = ativoSalvo.getId();

        // When & Then - Ativar o ativo
        mockMvc.perform(patch("/api/ativos/{id}/status", id)
                .param("disponivel", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponivel").value(true));

        // Verificar se foi ativado no banco
        Ativo ativoAtivado = repository.findById(id).orElse(null);
        assertNotNull(ativoAtivado);
        assertTrue(ativoAtivado.isDisponivel());
    }

    @Test
    void testDesativarAtivoComSucesso() throws Exception {
        // Given - Criar um ativo ativado
        AtivoRequestDTO ativo = new AtivoRequestDTO();
        ativo.setNome("Vale");
        ativo.setTipo(TipoAtivo.ACAO);
        ativo.setDescricao("Ação da Vale");
        ativo.setDisponivel(true);
        ativo.setValor(new BigDecimal("30.00"));

        String response = mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        AtivoResponseDTO ativoSalvo = objectMapper.readValue(response, AtivoResponseDTO.class);
        Long id = ativoSalvo.getId();

        // When & Then - Desativar o ativo
        mockMvc.perform(patch("/api/ativos/{id}/status", id)
                .param("disponivel", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponivel").value(false));

        // Verificar se foi desativado no banco
        Ativo ativoDesativado = repository.findById(id).orElse(null);
        assertNotNull(ativoDesativado);
        assertFalse(ativoDesativado.isDisponivel());
    }

    @Test
    void testAtivarDesativarAtivoNaoEncontrado() throws Exception {
        // When & Then - Tentar ativar ativo inexistente
        mockMvc.perform(patch("/api/ativos/999/status")
                .param("disponivel", "true"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testListarAtivosDisponiveis() throws Exception {
        // Given - Criar ativos com diferentes status
        AtivoRequestDTO ativo1 = new AtivoRequestDTO();
        ativo1.setNome("Petrobras");
        ativo1.setTipo(TipoAtivo.ACAO);
        ativo1.setDescricao("Ação da Petrobras");
        ativo1.setDisponivel(true);
        ativo1.setValor(new BigDecimal("25.50"));

        AtivoRequestDTO ativo2 = new AtivoRequestDTO();
        ativo2.setNome("Vale");
        ativo2.setTipo(TipoAtivo.ACAO);
        ativo2.setDescricao("Ação da Vale");
        ativo2.setDisponivel(false);
        ativo2.setValor(new BigDecimal("30.00"));

        AtivoRequestDTO ativo3 = new AtivoRequestDTO();
        ativo3.setNome("Bitcoin");
        ativo3.setTipo(TipoAtivo.CRIPTOMOEDA);
        ativo3.setDescricao("Bitcoin - primeira criptomoeda");
        ativo3.setDisponivel(true);
        ativo3.setValor(new BigDecimal("150000.00"));

        // Criar os ativos
        mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo2)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo3)))
                .andExpect(status().isCreated());

        // When & Then - Listar apenas ativos disponíveis
        mockMvc.perform(get("/api/ativos/disponiveis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nome").value("Petrobras"))
                .andExpect(jsonPath("$[1].nome").value("Bitcoin"));
    }

    @Test
    void testListarAtivosIndisponiveis() throws Exception {
        // Given - Criar ativos com diferentes status
        AtivoRequestDTO ativo1 = new AtivoRequestDTO();
        ativo1.setNome("Petrobras");
        ativo1.setTipo(TipoAtivo.ACAO);
        ativo1.setDescricao("Ação da Petrobras");
        ativo1.setDisponivel(true);
        ativo1.setValor(new BigDecimal("25.50"));

        AtivoRequestDTO ativo2 = new AtivoRequestDTO();
        ativo2.setNome("Vale");
        ativo2.setTipo(TipoAtivo.ACAO);
        ativo2.setDescricao("Ação da Vale");
        ativo2.setDisponivel(false);
        ativo2.setValor(new BigDecimal("30.00"));

        AtivoRequestDTO ativo3 = new AtivoRequestDTO();
        ativo3.setNome("Tesouro Selic");
        ativo3.setTipo(TipoAtivo.TESOURO_DIRETO);
        ativo3.setDescricao("Tesouro Direto Selic");
        ativo3.setDisponivel(false);
        ativo3.setValor(new BigDecimal("100.00"));

        // Criar os ativos
        mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo2)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo3)))
                .andExpect(status().isCreated());

        // When & Then - Listar apenas ativos indisponíveis
        mockMvc.perform(get("/api/ativos/indisponiveis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nome").value("Vale"))
                .andExpect(jsonPath("$[1].nome").value("Tesouro Selic"));
    }

    @Test
    void testListarTodosAtivos() throws Exception {
        // Given - Criar múltiplos ativos
        AtivoRequestDTO ativo1 = new AtivoRequestDTO();
        ativo1.setNome("Petrobras");
        ativo1.setTipo(TipoAtivo.ACAO);
        ativo1.setDescricao("Ação da Petrobras");
        ativo1.setDisponivel(true);
        ativo1.setValor(new BigDecimal("25.50"));

        AtivoRequestDTO ativo2 = new AtivoRequestDTO();
        ativo2.setNome("Vale");
        ativo2.setTipo(TipoAtivo.ACAO);
        ativo2.setDescricao("Ação da Vale");
        ativo2.setDisponivel(false);
        ativo2.setValor(new BigDecimal("30.00"));

        AtivoRequestDTO ativo3 = new AtivoRequestDTO();
        ativo3.setNome("Bitcoin");
        ativo3.setTipo(TipoAtivo.CRIPTOMOEDA);
        ativo3.setDescricao("Bitcoin - primeira criptomoeda");
        ativo3.setDisponivel(true);
        ativo3.setValor(new BigDecimal("150000.00"));

        // Criar os ativos
        mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo2)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo3)))
                .andExpect(status().isCreated());

        // When & Then - Listar todos os ativos
        mockMvc.perform(get("/api/ativos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void testBuscarAtivoPorIdComSucesso() throws Exception {
        // Given - Criar um ativo completo
        AtivoRequestDTO ativo = new AtivoRequestDTO();
        ativo.setNome("ETF Brasil");
        ativo.setTipo(TipoAtivo.ACAO);
        ativo.setDescricao("Fundo de índice brasileiro");
        ativo.setDisponivel(true);
        ativo.setValor(new BigDecimal("123.45"));

        String response = mockMvc.perform(post("/api/ativos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ativo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        AtivoResponseDTO salvo = objectMapper.readValue(response, AtivoResponseDTO.class);

        // When & Then - Buscar por ID e verificar campos detalhados
        mockMvc.perform(get("/api/ativos/{id}", salvo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("ETF Brasil"))
                .andExpect(jsonPath("$.tipo").value("ACAO"))
                .andExpect(jsonPath("$.descricao").value("Fundo de índice brasileiro"))
                .andExpect(jsonPath("$.disponivel").value(true))
                .andExpect(jsonPath("$.valorAtual").value(123.45));
    }

    @Test
    void testBuscarAtivoPorIdNaoEncontrado() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/ativos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ativo com ID 999 não encontrado"));
    }

    @Test
    void testFluxoCompletoAtivarDesativar() throws Exception {
        // Given - Criar um ativo
        AtivoRequestDTO ativo = new AtivoRequestDTO();
        ativo.setNome("Teste Fluxo");
        ativo.setTipo(TipoAtivo.ACAO);
        ativo.setDescricao("Ativo para teste de fluxo de ativação/desativação");
        ativo.setDisponivel(true);
        ativo.setValor(new BigDecimal("100.00"));

        // When & Then - 1. Criar ativo
        String response = mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        AtivoResponseDTO ativoSalvo = objectMapper.readValue(response, AtivoResponseDTO.class);
        Long id = ativoSalvo.getId();

        // Verificar se foi criado como disponível
        assertTrue(repository.findById(id).orElse(null).isDisponivel());

        // When & Then - 2. Desativar ativo
        mockMvc.perform(patch("/api/ativos/{id}/status", id)
                .param("disponivel", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponivel").value(false));

        // Verificar se foi desativado
        assertFalse(repository.findById(id).orElse(null).isDisponivel());

        // When & Then - 3. Reativar ativo
        mockMvc.perform(patch("/api/ativos/{id}/status", id)
                .param("disponivel", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponivel").value(true));

        // Verificar se foi reativado
        assertTrue(repository.findById(id).orElse(null).isDisponivel());
    }

    @Test
    void testAtivarDesativarComParametroInvalido() throws Exception {
        // Given - Criar um ativo
        AtivoRequestDTO ativo = new AtivoRequestDTO();
        ativo.setNome("Teste Parametro");
        ativo.setTipo(TipoAtivo.ACAO);
        ativo.setDescricao("Ativo para teste de parâmetro inválido");
        ativo.setDisponivel(true);
        ativo.setValor(new BigDecimal("100.00"));

        String response = mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        AtivoResponseDTO ativoSalvo = objectMapper.readValue(response, AtivoResponseDTO.class);
        Long id = ativoSalvo.getId();

        // When & Then - Tentar ativar com parâmetro inválido
        mockMvc.perform(patch("/api/ativos/{id}/status", id)
                .param("disponivel", "invalido"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAtivosDesativadosNaoAparecemEmDisponiveis() throws Exception {
        // Given - Criar ativos mistos
        AtivoRequestDTO ativo1 = new AtivoRequestDTO();
        ativo1.setNome("Disponível");
        ativo1.setTipo(TipoAtivo.ACAO);
        ativo1.setDescricao("Ativo disponível para teste");
        ativo1.setDisponivel(true);
        ativo1.setValor(new BigDecimal("25.50"));

        AtivoRequestDTO ativo2 = new AtivoRequestDTO();
        ativo2.setNome("Indisponível");
        ativo2.setTipo(TipoAtivo.ACAO);
        ativo2.setDescricao("Ativo indisponível para teste");
        ativo2.setDisponivel(false);
        ativo2.setValor(new BigDecimal("30.00"));

        // Criar os ativos
        mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo2)))
                .andExpect(status().isCreated());

        // When & Then - Verificar que apenas disponíveis aparecem na lista de disponíveis
        mockMvc.perform(get("/api/ativos/disponiveis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome").value("Disponível"));

        // When & Then - Verificar que apenas indisponíveis aparecem na lista de indisponíveis
        mockMvc.perform(get("/api/ativos/indisponiveis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome").value("Indisponível"));
    }

    @Test
    void testAtivosDesativadosAindaAparecemEmTodos() throws Exception {
        // Given - Criar ativos mistos
        AtivoRequestDTO ativo1 = new AtivoRequestDTO();
        ativo1.setNome("Disponível");
        ativo1.setTipo(TipoAtivo.ACAO);
        ativo1.setDescricao("Ativo disponível para teste");
        ativo1.setDisponivel(true);
        ativo1.setValor(new BigDecimal("25.50"));

        AtivoRequestDTO ativo2 = new AtivoRequestDTO();
        ativo2.setNome("Indisponível");
        ativo2.setTipo(TipoAtivo.ACAO);
        ativo2.setDescricao("Ativo indisponível para teste");
        ativo2.setDisponivel(false);
        ativo2.setValor(new BigDecimal("30.00"));

        // Criar os ativos
        mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativo2)))
                .andExpect(status().isCreated());

        // When & Then - Verificar que todos aparecem na lista geral
        mockMvc.perform(get("/api/ativos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    // US06 & US07 - Testes de Notificação

    @Test
    void quandoAtualizarValorAtivo_comVariacaoMaiorQue10Porcento_entaoNotificaClienteComInteresse() throws Exception {
        // Given - Criar cliente premium e ativo
        Cliente clientePremium = new Cliente();
        clientePremium.setNomeCompleto("Notificado Premium");
        clientePremium.setPlano(TipoPlano.PREMIUM);
        clientePremium.setCodigoAcesso("333333");
        clienteRepository.save(clientePremium);

        AtivoRequestDTO ativoRequest = new AtivoRequestDTO();
        ativoRequest.setNome("Ação para Notificar");
        ativoRequest.setTipo(TipoAtivo.ACAO);
        ativoRequest.setDescricao("Ação para teste de notificação de variação de preço");
        ativoRequest.setDisponivel(true);
        ativoRequest.setValor(new BigDecimal("100.00"));
        
        String response = mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativoRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        
        AtivoResponseDTO ativo = objectMapper.readValue(response, AtivoResponseDTO.class);

        // Given - Registrar interesse na variação de preço
        InteresseDTO interesseDTO = new InteresseDTO();
        interesseDTO.setAtivoId(ativo.getId());
        interesseDTO.setCodigoAcessoCliente(clientePremium.getCodigoAcesso());
        interesseDTO.setTipoInteresse(TipoInteresse.VARIACAO_PRECO);
        interesseService.registrarInteresse(interesseDTO);

        // When & Then - Atualizar valor com variação de +20%
        mockMvc.perform(patch("/api/ativos/{id}/valor", ativo.getId())
                        .param("novoValor", "120.00"))
                .andExpect(status().isOk());

        // Assert - Verificar se a notificação foi impressa
        // Usamos contains() para ignorar os logs do Hibernate que também são capturados no System.out.
        String expectedNotification = String.format(
                "[NOTIFICAÇÃO DE PREÇO] Olá, %s! O ativo '%s' que você tem interesse subiu 20%% e agora está cotado em R$ 120.00.",
                clientePremium.getNomeCompleto(),
                ativo.getNome()
        );
        assertTrue(outContent.toString().contains(expectedNotification));
    }

    @Test
    void quandoAtivarAtivo_entaoNotificaClienteComInteresseERemoveInteresse() throws Exception {
        // Given - Criar cliente e ativo indisponível
        Cliente cliente = new Cliente();
        cliente.setNomeCompleto("Notificado Disponibilidade");
        cliente.setPlano(TipoPlano.NORMAL);
        cliente.setCodigoAcesso("444444");
        clienteRepository.save(cliente);

        AtivoRequestDTO ativoRequest = new AtivoRequestDTO();
        ativoRequest.setNome("Ação para Ativar");
        ativoRequest.setTipo(TipoAtivo.ACAO);
        ativoRequest.setDescricao("Ação para teste de notificação de disponibilidade");
        ativoRequest.setDisponivel(false);
        ativoRequest.setValor(new BigDecimal("50.00"));
        
        String response = mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ativoRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        
        AtivoResponseDTO ativo = objectMapper.readValue(response, AtivoResponseDTO.class);

        // Given - Registrar interesse na disponibilidade
        InteresseDTO interesseDTO = new InteresseDTO();
        interesseDTO.setAtivoId(ativo.getId());
        interesseDTO.setCodigoAcessoCliente(cliente.getCodigoAcesso());
        interesseDTO.setTipoInteresse(TipoInteresse.DISPONIBILIDADE);
        interesseService.registrarInteresse(interesseDTO);
        assertEquals(1, interesseRepository.count());

        // When & Then - Ativar o ativo
        mockMvc.perform(patch("/api/ativos/{id}/status", ativo.getId())
                        .param("disponivel", "true"))
                .andExpect(status().isOk());

        // Assert - Verificar se o interesse foi removido (após a notificação)
        assertEquals(0, interesseRepository.count());
    }
} 
package com.psoft.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psoft.wallet.dto.ResgateRequestDTO;
import com.psoft.wallet.dto.ResgateResponseDTO;
import com.psoft.wallet.enums.EstadoResgate;
import com.psoft.wallet.enums.TipoAtivo;
import com.psoft.wallet.enums.TipoPlano;
import com.psoft.wallet.model.Ativo;
import com.psoft.wallet.model.Carteira;
import com.psoft.wallet.model.Cliente;
import com.psoft.wallet.repository.AtivoRepository;
import com.psoft.wallet.repository.CarteiraRepository;
import com.psoft.wallet.repository.ClienteRepository;
import com.psoft.wallet.repository.ResgateRepository;
import org.junit.jupiter.api.AfterEach;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class ResgateControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ResgateRepository resgateRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private AtivoRepository ativoRepository;

    @Autowired
    private CarteiraRepository carteiraRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    private Cliente cliente;
    private Ativo ativo;
    private Carteira carteira;

    @BeforeEach
    void setUp() {
        resgateRepository.deleteAll();
        carteiraRepository.deleteAll();
        ativoRepository.deleteAll();
        clienteRepository.deleteAll();
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Criar cliente
        cliente = new Cliente();
        cliente.setNomeCompleto("Cliente Teste");
        cliente.setPlano(TipoPlano.PREMIUM);
        cliente.setCodigoAcesso("112233");
        cliente.setSaldo(new BigDecimal("1000.00"));
        clienteRepository.save(cliente);

        // Criar ativo
        ativo = new Ativo();
        ativo.setNome("Ação Teste");
        ativo.setTipo(TipoAtivo.ACAO);
        ativo.setDisponivel(true);
        ativo.setValorAtual(new BigDecimal("100.00"));
        ativoRepository.save(ativo);

        // Criar carteira
        carteira = new Carteira();
        carteira.setCliente(cliente);
        carteira.setAtivo(ativo);
        carteira.setQuantidade(10);
        carteira.setValorAquisicao(new BigDecimal("90.00"));
        carteira.setValorAtual(ativo.getValorAtual());
        carteira.setDesempenho(ativo.getValorAtual().subtract(new BigDecimal("90.00")));
        carteira.setDataEntradaCarteira(LocalDateTime.now());
        carteira.setDataUltimaAtualizacao(LocalDateTime.now());
        carteiraRepository.save(carteira);
    }

    @AfterEach
    void tearDown() {
        resgateRepository.deleteAll();
        carteiraRepository.deleteAll();
        ativoRepository.deleteAll();
        clienteRepository.deleteAll();
    }

    // US14 - Testes para solicitar resgate

    @Test
    void testSolicitarResgateComSucesso() throws Exception {
        // Given
        ResgateRequestDTO request = new ResgateRequestDTO();
        request.setAtivoId(ativo.getId());
        request.setQuantidade(5);
        request.setCodigoAcesso("112233");

        // When & Then
        mockMvc.perform(post("/resgates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clienteId").value(cliente.getId()))
                .andExpect(jsonPath("$.ativoId").value(ativo.getId()))
                .andExpect(jsonPath("$.quantidade").value(5))
                .andExpect(jsonPath("$.valorUnitario").value(100.00))
                .andExpect(jsonPath("$.valorTotal").value(500.00))
                .andExpect(jsonPath("$.valorAquisicao").value(450.00))
                .andExpect(jsonPath("$.lucro").value(50.00))
                .andExpect(jsonPath("$.imposto").value(7.50)) // 15% de 50
                .andExpect(jsonPath("$.estado").value("SOLICITADO"))
                .andExpect(jsonPath("$.dataSolicitacao").exists())
                .andExpect(jsonPath("$.dataConfirmacao").doesNotExist())
                .andExpect(jsonPath("$.dataFinalizacao").doesNotExist());
    }

    @Test
    void testSolicitarResgateComClienteInexistente() throws Exception {
        // Given
        ResgateRequestDTO request = new ResgateRequestDTO();
        request.setAtivoId(ativo.getId());
        request.setQuantidade(5);
        request.setCodigoAcesso("999999");

        // When & Then
        mockMvc.perform(post("/resgates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cliente não encontrado."));
    }

    @Test
    void testSolicitarResgateComAtivoInexistente() throws Exception {
        // Given
        ResgateRequestDTO request = new ResgateRequestDTO();
        request.setAtivoId(999L);
        request.setQuantidade(5);
        request.setCodigoAcesso("112233");

        // When & Then
        mockMvc.perform(post("/resgates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ativo não encontrado."));
    }

    @Test
    void testSolicitarResgateComQuantidadeInsuficiente() throws Exception {
        // Given
        ResgateRequestDTO request = new ResgateRequestDTO();
        request.setAtivoId(ativo.getId());
        request.setQuantidade(15); // Mais que o disponível (10)
        request.setCodigoAcesso("112233");

        // When & Then
        mockMvc.perform(post("/resgates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Quantidade insuficiente na carteira."));
    }

    @Test
    void testSolicitarResgateSemAtivoNaCarteira() throws Exception {
        // Given - Criar outro ativo que não está na carteira
        Ativo outroAtivo = new Ativo();
        outroAtivo.setNome("Outro Ativo");
        outroAtivo.setTipo(TipoAtivo.ACAO);
        outroAtivo.setDisponivel(true);
        outroAtivo.setValorAtual(new BigDecimal("50.00"));
        ativoRepository.save(outroAtivo);

        ResgateRequestDTO request = new ResgateRequestDTO();
        request.setAtivoId(outroAtivo.getId());
        request.setQuantidade(5);
        request.setCodigoAcesso("112233");

        // When & Then
        mockMvc.perform(post("/resgates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cliente não possui este ativo na carteira."));
    }

    @Test
    void testSolicitarResgateComDadosInvalidos() throws Exception {
        // Given - Request com dados inválidos
        ResgateRequestDTO request = new ResgateRequestDTO();
        request.setAtivoId(ativo.getId());
        request.setQuantidade(-1); // Quantidade negativa
        request.setCodigoAcesso("112233");

        // When & Then
        mockMvc.perform(post("/resgates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // US16 - Testes para visualizar resgates

    @Test
    void testVisualizarResgatesComSucesso() throws Exception {
        // Given - Criar resgate
        ResgateRequestDTO request = new ResgateRequestDTO();
        request.setAtivoId(ativo.getId());
        request.setQuantidade(3);
        request.setCodigoAcesso("112233");

        mockMvc.perform(post("/resgates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // When & Then
        mockMvc.perform(get("/resgates/cliente/{codigoAcesso}", "112233"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].quantidade").value(3))
                .andExpect(jsonPath("$[0].estado").value("SOLICITADO"))
                .andExpect(jsonPath("$[0].dataSolicitacao").exists());
    }

    @Test
    void testVisualizarResgatesSemResgates() throws Exception {
        // When & Then
        mockMvc.perform(get("/resgates/cliente/{codigoAcesso}", "112233"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testVisualizarResgatesComClienteInexistente() throws Exception {
        // When & Then
        mockMvc.perform(get("/resgates/cliente/{codigoAcesso}", "999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cliente não encontrado."));
    }

    @Test
    void testVisualizarResgatesComMultiplosResgates() throws Exception {
        // Given - Criar múltiplos resgates
        ResgateRequestDTO request1 = new ResgateRequestDTO();
        request1.setAtivoId(ativo.getId());
        request1.setQuantidade(2);
        request1.setCodigoAcesso("112233");

        ResgateRequestDTO request2 = new ResgateRequestDTO();
        request2.setAtivoId(ativo.getId());
        request2.setQuantidade(3);
        request2.setCodigoAcesso("112233");

        mockMvc.perform(post("/resgates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/resgates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());

        // When & Then
        mockMvc.perform(get("/resgates/cliente/{codigoAcesso}", "112233"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].quantidade").value(2))
                .andExpect(jsonPath("$[1].quantidade").value(3));
    }

    // US17 - Testes para confirmar e finalizar resgates

    @Test
    void testConfirmarResgateComSucesso() throws Exception {
        // Given - Criar resgate
        ResgateRequestDTO request = new ResgateRequestDTO();
        request.setAtivoId(ativo.getId());
        request.setQuantidade(3);
        request.setCodigoAcesso("112233");

        String response = mockMvc.perform(post("/resgates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        ResgateResponseDTO resgate = objectMapper.readValue(response, ResgateResponseDTO.class);

        // When & Then
        mockMvc.perform(put("/resgates/{id}/confirmar", resgate.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONFIRMADO"))
                .andExpect(jsonPath("$.dataConfirmacao").exists())
                .andExpect(jsonPath("$.dataSolicitacao").exists())
                .andExpect(jsonPath("$.dataFinalizacao").doesNotExist());
    }

    @Test
    void testConfirmarResgateInexistente() throws Exception {
        // When & Then
        mockMvc.perform(put("/resgates/{id}/confirmar", 999L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Resgate não encontrado."));
    }

    @Test
    void testConfirmarResgateJaConfirmado() throws Exception {
        // Given - Criar e confirmar resgate
        ResgateRequestDTO request = new ResgateRequestDTO();
        request.setAtivoId(ativo.getId());
        request.setQuantidade(3);
        request.setCodigoAcesso("112233");

        String response = mockMvc.perform(post("/resgates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        ResgateResponseDTO resgate = objectMapper.readValue(response, ResgateResponseDTO.class);

        mockMvc.perform(put("/resgates/{id}/confirmar", resgate.getId()))
                .andExpect(status().isOk());

        // When & Then - Tentar confirmar novamente
        mockMvc.perform(put("/resgates/{id}/confirmar", resgate.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Apenas resgates solicitados podem ser confirmados."));
    }

    @Test
    void testFinalizarResgateComSucesso() throws Exception {
        // Given - Criar e confirmar resgate
        ResgateRequestDTO request = new ResgateRequestDTO();
        request.setAtivoId(ativo.getId());
        request.setQuantidade(3);
        request.setCodigoAcesso("112233");

        String response = mockMvc.perform(post("/resgates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        ResgateResponseDTO resgate = objectMapper.readValue(response, ResgateResponseDTO.class);

        mockMvc.perform(put("/resgates/{id}/confirmar", resgate.getId()))
                .andExpect(status().isOk());

        // When & Then
        mockMvc.perform(put("/resgates/{id}/finalizar", resgate.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EM_CONTA"))
                .andExpect(jsonPath("$.dataFinalizacao").exists())
                .andExpect(jsonPath("$.dataConfirmacao").exists())
                .andExpect(jsonPath("$.dataSolicitacao").exists());
    }

    @Test
    void testFinalizarResgateInexistente() throws Exception {
        // When & Then
        mockMvc.perform(put("/resgates/{id}/finalizar", 999L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Resgate não encontrado."));
    }

    @Test
    void testFinalizarResgateNaoConfirmado() throws Exception {
        // Given - Criar resgate sem confirmar
        ResgateRequestDTO request = new ResgateRequestDTO();
        request.setAtivoId(ativo.getId());
        request.setQuantidade(3);
        request.setCodigoAcesso("112233");

        String response = mockMvc.perform(post("/resgates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        ResgateResponseDTO resgate = objectMapper.readValue(response, ResgateResponseDTO.class);

        // When & Then
        mockMvc.perform(put("/resgates/{id}/finalizar", resgate.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Apenas resgates confirmados podem ser finalizados."));
    }

    @Test
    void testListarResgatesSolicitadosComSucesso() throws Exception {
        // Given - Criar resgates com diferentes estados
        ResgateRequestDTO request1 = new ResgateRequestDTO();
        request1.setAtivoId(ativo.getId());
        request1.setQuantidade(2);
        request1.setCodigoAcesso("112233");

        ResgateRequestDTO request2 = new ResgateRequestDTO();
        request2.setAtivoId(ativo.getId());
        request2.setQuantidade(3);
        request2.setCodigoAcesso("112233");

        String response1 = mockMvc.perform(post("/resgates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        mockMvc.perform(post("/resgates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());

        // Confirmar um dos resgates
        ResgateResponseDTO resgate1 = objectMapper.readValue(response1, ResgateResponseDTO.class);
        mockMvc.perform(put("/resgates/{id}/confirmar", resgate1.getId()))
                .andExpect(status().isOk());

        // When & Then
        mockMvc.perform(get("/resgates/solicitados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].estado").value("SOLICITADO"));
    }

    @Test
    void testListarResgatesSolicitadosSemResgates() throws Exception {
        // When & Then
        mockMvc.perform(get("/resgates/solicitados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testFluxoCompletoResgate() throws Exception {
        // Given
        ResgateRequestDTO request = new ResgateRequestDTO();
        request.setAtivoId(ativo.getId());
        request.setQuantidade(5);
        request.setCodigoAcesso("112233");

        // When & Then - 1. Solicitar resgate
        String response = mockMvc.perform(post("/resgates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("SOLICITADO"))
                .andReturn().getResponse().getContentAsString();

        ResgateResponseDTO resgate = objectMapper.readValue(response, ResgateResponseDTO.class);

        // When & Then - 2. Confirmar resgate
        mockMvc.perform(put("/resgates/{id}/confirmar", resgate.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONFIRMADO"));

        // When & Then - 3. Finalizar resgate
        mockMvc.perform(put("/resgates/{id}/finalizar", resgate.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EM_CONTA"));

        // When & Then - 4. Verificar resgates do cliente
        mockMvc.perform(get("/resgates/cliente/{codigoAcesso}", "112233"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].estado").value("EM_CONTA"));
    }

    @Test
    void testSolicitarResgateComCodigoAcessoIncorreto() throws Exception {
        // Given
        ResgateRequestDTO request = new ResgateRequestDTO();
        request.setAtivoId(ativo.getId());
        request.setQuantidade(5);
        request.setCodigoAcesso("000000"); // Código incorreto

        // When & Then
        mockMvc.perform(post("/resgates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cliente não encontrado."));
    }

    @Test
    void testSolicitarResgateComPayloadInvalido() throws Exception {
        // Given - JSON inválido
        String jsonInvalido = "{\"ativoId\": \"abc\", \"quantidade\": 5}";

        // When & Then
        mockMvc.perform(post("/resgates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonInvalido))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSolicitarResgateComDadosIncompletos() throws Exception {
        // Given - Request sem campos obrigatórios
        ResgateRequestDTO request = new ResgateRequestDTO();
        request.setAtivoId(ativo.getId());
        // Sem quantidade e código de acesso

        // When & Then
        mockMvc.perform(post("/resgates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}

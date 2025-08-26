package com.psoft.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psoft.wallet.dto.AtivoRequestDTO;
import com.psoft.wallet.enums.TipoAtivo;
import com.psoft.wallet.enums.TipoPlano;
import com.psoft.wallet.repository.AtivoRepository;
import com.psoft.wallet.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AtivoClienteControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AtivoRepository ativoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ativoRepository.deleteAll();
        clienteRepository.deleteAll();
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private void criarCliente(String nome, String endereco, TipoPlano plano, String codigoAcesso) throws Exception {
        // Criamos o JSON manualmente para garantir que o campo 'codigoAcesso' (marcado como WRITE_ONLY)
        // seja incluído no payload da requisição, contornando a lógica de serialização do ObjectMapper.
        String clienteJson = String.format(
                "{\"nomeCompleto\":\"%s\",\"enderecoPrincipal\":\"%s\",\"plano\":\"%s\",\"codigoAcesso\":\"%s\"}",
                nome, endereco, plano.name(), codigoAcesso
        );
        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clienteJson))
                .andExpect(status().isOk());
    }

    // US05 - Testes para visualizar ativos disponíveis para o plano

    @Test
    void testClienteNormalVeApenasTesouroDireto() throws Exception {
        // Given - Criar cliente Normal
        criarCliente("João Silva", "Rua das Flores, 123", TipoPlano.NORMAL, "123456");

        // Given - Criar ativos de diferentes tipos
        AtivoRequestDTO tesouro = new AtivoRequestDTO();
        tesouro.setNome("Tesouro Selic 2026");
        tesouro.setTipo(TipoAtivo.TESOURO_DIRETO);
        tesouro.setDescricao("Tesouro Direto Selic 2026");
        tesouro.setDisponivel(true);
        tesouro.setValor(new BigDecimal("100.00"));

        AtivoRequestDTO acao = new AtivoRequestDTO();
        acao.setNome("Petrobras");
        acao.setTipo(TipoAtivo.ACAO);
        acao.setDescricao("Ação da Petrobras");
        acao.setDisponivel(true);
        acao.setValor(new BigDecimal("25.50"));

        AtivoRequestDTO cripto = new AtivoRequestDTO();
        cripto.setNome("Bitcoin");
        cripto.setTipo(TipoAtivo.CRIPTOMOEDA);
        cripto.setDescricao("Bitcoin - primeira criptomoeda");
        cripto.setDisponivel(true);
        cripto.setValor(new BigDecimal("150000.00"));

        // Criar os ativos
        mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tesouro)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(acao)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cripto)))
                .andExpect(status().isCreated());

        // When & Then - Cliente Normal deve ver apenas Tesouro Direto
        mockMvc.perform(get("/cliente/ativos/disponiveis")
                .param("codigoAcesso", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome").value("Tesouro Selic 2026"))
                .andExpect(jsonPath("$[0].tipo").value("TESOURO_DIRETO"));
    }

    @Test
    void testClientePremiumVeTodosOsTipos() throws Exception {
        // Given - Criar cliente Premium
        criarCliente("Maria Santos", "Av. Principal, 456", TipoPlano.PREMIUM, "654321");

        // Given - Criar ativos de diferentes tipos
        AtivoRequestDTO tesouro = new AtivoRequestDTO();
        tesouro.setNome("Tesouro Selic 2026");
        tesouro.setTipo(TipoAtivo.TESOURO_DIRETO);
        tesouro.setDescricao("Tesouro Direto Selic 2026");
        tesouro.setDisponivel(true);
        tesouro.setValor(new BigDecimal("100.00"));

        AtivoRequestDTO acao = new AtivoRequestDTO();
        acao.setNome("Petrobras");
        acao.setTipo(TipoAtivo.ACAO);
        acao.setDescricao("Ação da Petrobras");
        acao.setDisponivel(true);
        acao.setValor(new BigDecimal("25.50"));

        AtivoRequestDTO cripto = new AtivoRequestDTO();
        cripto.setNome("Bitcoin");
        cripto.setTipo(TipoAtivo.CRIPTOMOEDA);
        cripto.setDescricao("Bitcoin - primeira criptomoeda");
        cripto.setDisponivel(true);
        cripto.setValor(new BigDecimal("150000.00"));

        // Criar os ativos
        mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tesouro)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(acao)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cripto)))
                .andExpect(status().isCreated());

        // When & Then - Cliente Premium deve ver todos os tipos
        mockMvc.perform(get("/cliente/ativos/disponiveis")
                .param("codigoAcesso", "654321"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].nome").value("Tesouro Selic 2026"))
                .andExpect(jsonPath("$[0].tipo").value("TESOURO_DIRETO"))
                .andExpect(jsonPath("$[1].nome").value("Petrobras"))
                .andExpect(jsonPath("$[1].tipo").value("ACAO"))
                .andExpect(jsonPath("$[2].nome").value("Bitcoin"))
                .andExpect(jsonPath("$[2].tipo").value("CRIPTOMOEDA"));
    }

    @Test
    void testClienteNormalNaoVeAcoesOuCriptomoedas() throws Exception {
        // Given - Criar cliente Normal
        criarCliente("João Silva", "Rua das Flores, 123", TipoPlano.NORMAL, "123456");

        // Given - Criar apenas ações e criptomoedas (sem Tesouro Direto)
        AtivoRequestDTO acao = new AtivoRequestDTO();
        acao.setNome("Petrobras");
        acao.setTipo(TipoAtivo.ACAO);
        acao.setDescricao("Ação da Petrobras");
        acao.setDisponivel(true);
        acao.setValor(new BigDecimal("25.50"));

        AtivoRequestDTO cripto = new AtivoRequestDTO();
        cripto.setNome("Bitcoin");
        cripto.setTipo(TipoAtivo.CRIPTOMOEDA);
        cripto.setDescricao("Bitcoin - primeira criptomoeda");
        cripto.setDisponivel(true);
        cripto.setValor(new BigDecimal("150000.00"));

        // Criar os ativos
        mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(acao)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cripto)))
                .andExpect(status().isCreated());

        // When & Then - Cliente Normal não deve ver nada (lista vazia)
        mockMvc.perform(get("/cliente/ativos/disponiveis")
                .param("codigoAcesso", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testClientePremiumVeApenasTesouroDiretoQuandoSoExisteTesouro() throws Exception {
        // Given - Criar cliente Premium
        criarCliente("Maria Santos", "Av. Principal, 456", TipoPlano.PREMIUM, "654321");

        // Given - Criar apenas Tesouro Direto
        AtivoRequestDTO tesouro = new AtivoRequestDTO();
        tesouro.setNome("Tesouro Selic 2026");
        tesouro.setTipo(TipoAtivo.TESOURO_DIRETO);
        tesouro.setDescricao("Tesouro Direto Selic 2026");
        tesouro.setDisponivel(true);
        tesouro.setValor(new BigDecimal("100.00"));

        // Criar o ativo
        mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tesouro)))
                .andExpect(status().isCreated());

        // When & Then - Cliente Premium deve ver o Tesouro Direto
        mockMvc.perform(get("/cliente/ativos/disponiveis")
                .param("codigoAcesso", "654321"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome").value("Tesouro Selic 2026"))
                .andExpect(jsonPath("$[0].tipo").value("TESOURO_DIRETO"));
    }

    @Test
    void testClienteNaoVeAtivosIndisponiveis() throws Exception {
        // Given - Criar cliente Normal
        criarCliente("João Silva", "Rua das Flores, 123", TipoPlano.NORMAL, "123456");

        // Given - Criar Tesouro Direto indisponível
        AtivoRequestDTO tesouroIndisponivel = new AtivoRequestDTO();
        tesouroIndisponivel.setNome("Tesouro Selic 2026");
        tesouroIndisponivel.setTipo(TipoAtivo.TESOURO_DIRETO);
        tesouroIndisponivel.setDescricao("Tesouro Direto Selic 2026");
        tesouroIndisponivel.setDisponivel(false);
        tesouroIndisponivel.setValor(new BigDecimal("100.00"));

        // Criar o ativo indisponível
        mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tesouroIndisponivel)))
                .andExpect(status().isCreated());

        // When & Then - Cliente não deve ver ativos indisponíveis
        mockMvc.perform(get("/cliente/ativos/disponiveis")
                .param("codigoAcesso", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testAcessoComCodigoIncorreto() throws Exception {
        // When & Then - Tentar acessar com código incorreto
        mockMvc.perform(get("/cliente/ativos/disponiveis")
                .param("codigoAcesso", "999999"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAcessoSemCodigo() throws Exception {
        // When & Then - Tentar acessar sem código
        mockMvc.perform(get("/cliente/ativos/disponiveis"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testClientePremiumVeMultiplosAtivos() throws Exception {
        // Given - Criar cliente Premium
        criarCliente("Maria Santos", "Av. Principal, 456", TipoPlano.PREMIUM, "654321");

        // Given - Criar múltiplos ativos de diferentes tipos
        AtivoRequestDTO tesouro1 = new AtivoRequestDTO();
        tesouro1.setNome("Tesouro Selic 2026");
        tesouro1.setTipo(TipoAtivo.TESOURO_DIRETO);
        tesouro1.setDescricao("Tesouro Direto Selic 2026");
        tesouro1.setDisponivel(true);
        tesouro1.setValor(new BigDecimal("100.00"));

        AtivoRequestDTO tesouro2 = new AtivoRequestDTO();
        tesouro2.setNome("Tesouro IPCA 2030");
        tesouro2.setTipo(TipoAtivo.TESOURO_DIRETO);
        tesouro2.setDescricao("Tesouro Direto IPCA 2030");
        tesouro2.setDisponivel(true);
        tesouro2.setValor(new BigDecimal("150.00"));

        AtivoRequestDTO acao1 = new AtivoRequestDTO();
        acao1.setNome("Petrobras");
        acao1.setTipo(TipoAtivo.ACAO);
        acao1.setDescricao("Ação da Petrobras");
        acao1.setDisponivel(true);
        acao1.setValor(new BigDecimal("25.50"));

        AtivoRequestDTO acao2 = new AtivoRequestDTO();
        acao2.setNome("Vale");
        acao2.setTipo(TipoAtivo.ACAO);
        acao2.setDescricao("Ação da Vale");
        acao2.setDisponivel(true);
        acao2.setValor(new BigDecimal("30.00"));

        AtivoRequestDTO cripto1 = new AtivoRequestDTO();
        cripto1.setNome("Bitcoin");
        cripto1.setTipo(TipoAtivo.CRIPTOMOEDA);
        cripto1.setDescricao("Bitcoin - primeira criptomoeda");
        cripto1.setDisponivel(true);
        cripto1.setValor(new BigDecimal("150000.00"));

        AtivoRequestDTO cripto2 = new AtivoRequestDTO();
        cripto2.setNome("Ethereum");
        cripto2.setTipo(TipoAtivo.CRIPTOMOEDA);
        cripto2.setDescricao("Ethereum - segunda criptomoeda");
        cripto2.setDisponivel(true);
        cripto2.setValor(new BigDecimal("8000.00"));

        // Criar todos os ativos
        mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tesouro1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tesouro2)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(acao1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(acao2)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cripto1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cripto2)))
                .andExpect(status().isCreated());

        // When & Then - Cliente Premium deve ver todos os 6 ativos
        mockMvc.perform(get("/cliente/ativos/disponiveis")
                .param("codigoAcesso", "654321"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)));
    }

    @Test
    void testClienteNormalVeApenasTesourosDisponiveis() throws Exception {
        // Given - Criar cliente Normal
        criarCliente("João Silva", "Rua das Flores, 123", TipoPlano.NORMAL, "123456");

        // Given - Criar múltiplos Tesouros Diretos (alguns indisponíveis)
        AtivoRequestDTO tesouro1 = new AtivoRequestDTO();
        tesouro1.setNome("Tesouro Selic 2026");
        tesouro1.setTipo(TipoAtivo.TESOURO_DIRETO);
        tesouro1.setDescricao("Tesouro Direto Selic 2026");
        tesouro1.setDisponivel(true);
        tesouro1.setValor(new BigDecimal("100.00"));

        AtivoRequestDTO tesouro2 = new AtivoRequestDTO();
        tesouro2.setNome("Tesouro IPCA 2030");
        tesouro2.setTipo(TipoAtivo.TESOURO_DIRETO);
        tesouro2.setDescricao("Tesouro Direto IPCA 2030");
        tesouro2.setDisponivel(false); // Indisponível
        tesouro2.setValor(new BigDecimal("150.00"));

        AtivoRequestDTO tesouro3 = new AtivoRequestDTO();
        tesouro3.setNome("Tesouro Prefixado 2025");
        tesouro3.setTipo(TipoAtivo.TESOURO_DIRETO);
        tesouro3.setDescricao("Tesouro Direto Prefixado 2025");
        tesouro3.setDisponivel(true);
        tesouro3.setValor(new BigDecimal("120.00"));

        // Criar os ativos
        mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tesouro1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tesouro2)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tesouro3)))
                .andExpect(status().isCreated());

        // When & Then - Cliente Normal deve ver apenas os 2 Tesouros disponíveis
        mockMvc.perform(get("/cliente/ativos/disponiveis")
                .param("codigoAcesso", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nome").value("Tesouro Selic 2026"))
                .andExpect(jsonPath("$[1].nome").value("Tesouro Prefixado 2025"));
    }
} 
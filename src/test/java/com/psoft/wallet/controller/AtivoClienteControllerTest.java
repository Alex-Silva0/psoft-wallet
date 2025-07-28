package com.psoft.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psoft.wallet.model.Ativo;
import com.psoft.wallet.model.Cliente;
import com.psoft.wallet.model.TipoAtivo;
import com.psoft.wallet.model.TipoPlano;
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

    // US05 - Testes para visualizar ativos disponíveis para o plano

    @Test
    void testClienteNormalVeApenasTesouroDireto() throws Exception {
        // Given - Criar cliente Normal
        Cliente cliente = new Cliente();
        cliente.setNomeCompleto("João Silva");
        cliente.setEndereco("Rua das Flores, 123");
        cliente.setPlano(TipoPlano.NORMAL);
        cliente.setCodigoAcesso("123456");

        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isOk());

        // Given - Criar ativos de diferentes tipos
        Ativo tesouro = new Ativo();
        tesouro.setNome("Tesouro Selic 2026");
        tesouro.setTipo(TipoAtivo.TESOURO);
        tesouro.setDescricao("Tesouro Direto Selic 2026");
        tesouro.setDisponivel(true);
        tesouro.setValorAtual(100.00f);

        Ativo acao = new Ativo();
        acao.setNome("Petrobras");
        acao.setTipo(TipoAtivo.ACAO);
        acao.setDescricao("Ação da Petrobras");
        acao.setDisponivel(true);
        acao.setValorAtual(25.50f);

        Ativo cripto = new Ativo();
        cripto.setNome("Bitcoin");
        cripto.setTipo(TipoAtivo.CRIPTO);
        cripto.setDescricao("Bitcoin - primeira criptomoeda");
        cripto.setDisponivel(true);
        cripto.setValorAtual(150000.00f);

        // Criar os ativos
        mockMvc.perform(post("/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tesouro)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(acao)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cripto)))
                .andExpect(status().isOk());

        // When & Then - Cliente Normal deve ver apenas Tesouro Direto
        mockMvc.perform(get("/cliente/ativos/disponiveis")
                .param("codigoAcesso", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome").value("Tesouro Selic 2026"))
                .andExpect(jsonPath("$[0].tipo").value("TESOURO"));
    }

    @Test
    void testClientePremiumVeTodosOsTipos() throws Exception {
        // Given - Criar cliente Premium
        Cliente cliente = new Cliente();
        cliente.setNomeCompleto("Maria Santos");
        cliente.setEndereco("Av. Principal, 456");
        cliente.setPlano(TipoPlano.PREMIUM);
        cliente.setCodigoAcesso("654321");

        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isOk());

        // Given - Criar ativos de diferentes tipos
        Ativo tesouro = new Ativo();
        tesouro.setNome("Tesouro Selic 2026");
        tesouro.setTipo(TipoAtivo.TESOURO);
        tesouro.setDescricao("Tesouro Direto Selic 2026");
        tesouro.setDisponivel(true);
        tesouro.setValorAtual(100.00f);

        Ativo acao = new Ativo();
        acao.setNome("Petrobras");
        acao.setTipo(TipoAtivo.ACAO);
        acao.setDescricao("Ação da Petrobras");
        acao.setDisponivel(true);
        acao.setValorAtual(25.50f);

        Ativo cripto = new Ativo();
        cripto.setNome("Bitcoin");
        cripto.setTipo(TipoAtivo.CRIPTO);
        cripto.setDescricao("Bitcoin - primeira criptomoeda");
        cripto.setDisponivel(true);
        cripto.setValorAtual(150000.00f);

        // Criar os ativos
        mockMvc.perform(post("/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tesouro)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(acao)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cripto)))
                .andExpect(status().isOk());

        // When & Then - Cliente Premium deve ver todos os tipos
        mockMvc.perform(get("/cliente/ativos/disponiveis")
                .param("codigoAcesso", "654321"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].nome").value("Tesouro Selic 2026"))
                .andExpect(jsonPath("$[0].tipo").value("TESOURO"))
                .andExpect(jsonPath("$[1].nome").value("Petrobras"))
                .andExpect(jsonPath("$[1].tipo").value("ACAO"))
                .andExpect(jsonPath("$[2].nome").value("Bitcoin"))
                .andExpect(jsonPath("$[2].tipo").value("CRIPTO"));
    }

    @Test
    void testClienteNormalNaoVeAcoesOuCriptomoedas() throws Exception {
        // Given - Criar cliente Normal
        Cliente cliente = new Cliente();
        cliente.setNomeCompleto("João Silva");
        cliente.setEndereco("Rua das Flores, 123");
        cliente.setPlano(TipoPlano.NORMAL);
        cliente.setCodigoAcesso("123456");

        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isOk());

        // Given - Criar apenas ações e criptomoedas (sem Tesouro Direto)
        Ativo acao = new Ativo();
        acao.setNome("Petrobras");
        acao.setTipo(TipoAtivo.ACAO);
        acao.setDescricao("Ação da Petrobras");
        acao.setDisponivel(true);
        acao.setValorAtual(25.50f);

        Ativo cripto = new Ativo();
        cripto.setNome("Bitcoin");
        cripto.setTipo(TipoAtivo.CRIPTO);
        cripto.setDescricao("Bitcoin - primeira criptomoeda");
        cripto.setDisponivel(true);
        cripto.setValorAtual(150000.00f);

        // Criar os ativos
        mockMvc.perform(post("/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(acao)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cripto)))
                .andExpect(status().isOk());

        // When & Then - Cliente Normal não deve ver nada (lista vazia)
        mockMvc.perform(get("/cliente/ativos/disponiveis")
                .param("codigoAcesso", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testClientePremiumVeApenasTesouroDiretoQuandoSoExisteTesouro() throws Exception {
        // Given - Criar cliente Premium
        Cliente cliente = new Cliente();
        cliente.setNomeCompleto("Maria Santos");
        cliente.setEndereco("Av. Principal, 456");
        cliente.setPlano(TipoPlano.PREMIUM);
        cliente.setCodigoAcesso("654321");

        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isOk());

        // Given - Criar apenas Tesouro Direto
        Ativo tesouro = new Ativo();
        tesouro.setNome("Tesouro Selic 2026");
        tesouro.setTipo(TipoAtivo.TESOURO);
        tesouro.setDescricao("Tesouro Direto Selic 2026");
        tesouro.setDisponivel(true);
        tesouro.setValorAtual(100.00f);

        // Criar o ativo
        mockMvc.perform(post("/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tesouro)))
                .andExpect(status().isOk());

        // When & Then - Cliente Premium deve ver o Tesouro Direto
        mockMvc.perform(get("/cliente/ativos/disponiveis")
                .param("codigoAcesso", "654321"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome").value("Tesouro Selic 2026"))
                .andExpect(jsonPath("$[0].tipo").value("TESOURO"));
    }

    @Test
    void testClienteNaoVeAtivosIndisponiveis() throws Exception {
        // Given - Criar cliente Normal
        Cliente cliente = new Cliente();
        cliente.setNomeCompleto("João Silva");
        cliente.setEndereco("Rua das Flores, 123");
        cliente.setPlano(TipoPlano.NORMAL);
        cliente.setCodigoAcesso("123456");

        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isOk());

        // Given - Criar Tesouro Direto indisponível
        Ativo tesouroIndisponivel = new Ativo();
        tesouroIndisponivel.setNome("Tesouro Selic 2026");
        tesouroIndisponivel.setTipo(TipoAtivo.TESOURO);
        tesouroIndisponivel.setDescricao("Tesouro Direto Selic 2026");
        tesouroIndisponivel.setDisponivel(false);
        tesouroIndisponivel.setValorAtual(100.00f);

        // Criar o ativo indisponível
        mockMvc.perform(post("/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tesouroIndisponivel)))
                .andExpect(status().isOk());

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
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Código de acesso incorreto"));
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
        Cliente cliente = new Cliente();
        cliente.setNomeCompleto("Maria Santos");
        cliente.setEndereco("Av. Principal, 456");
        cliente.setPlano(TipoPlano.PREMIUM);
        cliente.setCodigoAcesso("654321");

        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isOk());

        // Given - Criar múltiplos ativos de diferentes tipos
        Ativo tesouro1 = new Ativo();
        tesouro1.setNome("Tesouro Selic 2026");
        tesouro1.setTipo(TipoAtivo.TESOURO);
        tesouro1.setDisponivel(true);
        tesouro1.setValorAtual(100.00f);

        Ativo tesouro2 = new Ativo();
        tesouro2.setNome("Tesouro IPCA 2030");
        tesouro2.setTipo(TipoAtivo.TESOURO);
        tesouro2.setDisponivel(true);
        tesouro2.setValorAtual(150.00f);

        Ativo acao1 = new Ativo();
        acao1.setNome("Petrobras");
        acao1.setTipo(TipoAtivo.ACAO);
        acao1.setDisponivel(true);
        acao1.setValorAtual(25.50f);

        Ativo acao2 = new Ativo();
        acao2.setNome("Vale");
        acao2.setTipo(TipoAtivo.ACAO);
        acao2.setDisponivel(true);
        acao2.setValorAtual(30.00f);

        Ativo cripto1 = new Ativo();
        cripto1.setNome("Bitcoin");
        cripto1.setTipo(TipoAtivo.CRIPTO);
        cripto1.setDisponivel(true);
        cripto1.setValorAtual(150000.00f);

        Ativo cripto2 = new Ativo();
        cripto2.setNome("Ethereum");
        cripto2.setTipo(TipoAtivo.CRIPTO);
        cripto2.setDisponivel(true);
        cripto2.setValorAtual(8000.00f);

        // Criar todos os ativos
        mockMvc.perform(post("/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tesouro1)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tesouro2)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(acao1)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(acao2)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cripto1)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cripto2)))
                .andExpect(status().isOk());

        // When & Then - Cliente Premium deve ver todos os 6 ativos
        mockMvc.perform(get("/cliente/ativos/disponiveis")
                .param("codigoAcesso", "654321"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)));
    }

    @Test
    void testClienteNormalVeApenasTesourosDisponiveis() throws Exception {
        // Given - Criar cliente Normal
        Cliente cliente = new Cliente();
        cliente.setNomeCompleto("João Silva");
        cliente.setEndereco("Rua das Flores, 123");
        cliente.setPlano(TipoPlano.NORMAL);
        cliente.setCodigoAcesso("123456");

        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isOk());

        // Given - Criar múltiplos Tesouros Diretos (alguns indisponíveis)
        Ativo tesouro1 = new Ativo();
        tesouro1.setNome("Tesouro Selic 2026");
        tesouro1.setTipo(TipoAtivo.TESOURO);
        tesouro1.setDisponivel(true);
        tesouro1.setValorAtual(100.00f);

        Ativo tesouro2 = new Ativo();
        tesouro2.setNome("Tesouro IPCA 2030");
        tesouro2.setTipo(TipoAtivo.TESOURO);
        tesouro2.setDisponivel(false); // Indisponível
        tesouro2.setValorAtual(150.00f);

        Ativo tesouro3 = new Ativo();
        tesouro3.setNome("Tesouro Prefixado 2025");
        tesouro3.setTipo(TipoAtivo.TESOURO);
        tesouro3.setDisponivel(true);
        tesouro3.setValorAtual(120.00f);

        // Criar os ativos
        mockMvc.perform(post("/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tesouro1)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tesouro2)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/ativos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tesouro3)))
                .andExpect(status().isOk());

        // When & Then - Cliente Normal deve ver apenas os 2 Tesouros disponíveis
        mockMvc.perform(get("/cliente/ativos/disponiveis")
                .param("codigoAcesso", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nome").value("Tesouro Selic 2026"))
                .andExpect(jsonPath("$[1].nome").value("Tesouro Prefixado 2025"));
    }
} 
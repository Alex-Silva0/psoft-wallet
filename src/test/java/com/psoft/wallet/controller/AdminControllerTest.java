package com.psoft.wallet.controller;

import com.psoft.wallet.enums.EstadoCompra;
import com.psoft.wallet.enums.EstadoResgate;
import com.psoft.wallet.enums.TipoAtivo;
import com.psoft.wallet.enums.TipoPlano;
import com.psoft.wallet.model.Ativo;
import com.psoft.wallet.model.Cliente;
import com.psoft.wallet.model.Compra;
import com.psoft.wallet.model.Resgate;
import com.psoft.wallet.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AtivoRepository ativoRepository;

    @Autowired
    ClienteRepository clienteRepository;

    @Autowired
    CompraRepository compraRepository;

    @Autowired
    ResgateRepository resgateRepository;

    Cliente cliente1;
    Cliente cliente2;
    Ativo ativoAcao;
    Ativo ativoCripto;

    @BeforeEach
    void setUp() {
        cliente1 = new Cliente();
        cliente1.setNomeCompleto("Cliente Um");
        cliente1.setPlano(TipoPlano.PREMIUM);
        cliente1.setCodigoAcesso("111111");
        cliente1.setSaldo(new BigDecimal("1000.00"));
        clienteRepository.save(cliente1);

        cliente2 = new Cliente();
        cliente2.setNomeCompleto("Cliente Dois");
        cliente2.setPlano(TipoPlano.NORMAL);
        cliente2.setCodigoAcesso("222222");
        cliente2.setSaldo(new BigDecimal("2000.00"));
        clienteRepository.save(cliente2);

        ativoAcao = new Ativo();
        ativoAcao.setNome("Ação Teste");
        ativoAcao.setTipo(TipoAtivo.ACAO);
        ativoAcao.setDisponivel(true);
        ativoAcao.setValorAtual(new BigDecimal("100.00"));
        ativoRepository.save(ativoAcao);

        ativoCripto = new Ativo();
        ativoCripto.setNome("Cripto Teste");
        ativoCripto.setTipo(TipoAtivo.CRIPTOMOEDA);
        ativoCripto.setDisponivel(true);
        ativoCripto.setValorAtual(new BigDecimal("500.00"));
        ativoRepository.save(ativoCripto);

        Compra compra1 = new Compra();
        compra1.setCliente(cliente1);
        compra1.setAtivo(ativoAcao);
        compra1.setQuantidade(2);
        compra1.setValorTotal(new BigDecimal("200.00"));
        compra1.setValorUnitarioNaCompra(new BigDecimal("100.00"));
        compra1.setEstado(EstadoCompra.EM_CARTEIRA);
        compra1.setDataSolicitacao(LocalDateTime.now().minusDays(1));
        compraRepository.save(compra1);

        Compra compra2 = new Compra();
        compra2.setCliente(cliente2);
        compra2.setAtivo(ativoCripto);
        compra2.setQuantidade(1);
        compra2.setValorTotal(new BigDecimal("500.00"));
        compra2.setValorUnitarioNaCompra(new BigDecimal("500.00"));
        compra2.setEstado(EstadoCompra.EM_CARTEIRA);
        compra2.setDataSolicitacao(LocalDateTime.now());
        compraRepository.save(compra2);

        Resgate resgate1 = new Resgate();
        resgate1.setCliente(cliente1);
        resgate1.setAtivo(ativoAcao);
        resgate1.setQuantidade(1);
        resgate1.setValorTotal(new BigDecimal("100.00"));
        resgate1.setValorUnitario(new BigDecimal("100.00"));
        resgate1.setValorAquisicao(new BigDecimal("100.00"));
        resgate1.setLucro(BigDecimal.ZERO);
        resgate1.setImposto(BigDecimal.ZERO);
        resgate1.setEstado(EstadoResgate.CONFIRMADO);
        resgate1.setDataSolicitacao(LocalDateTime.now());
        resgateRepository.save(resgate1);
    }

    @AfterEach
    void tearDown() {
        resgateRepository.deleteAll();
        compraRepository.deleteAll();
        ativoRepository.deleteAll();
        clienteRepository.deleteAll();
    }

    @Test
    void quandoConsultarHistoricoAdmin_semFiltros_entaoRetornaTodasOperacoes() throws Exception {
        mockMvc.perform(get("/api/admin/operacoes/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void quandoConsultarHistoricoAdmin_comFiltroClienteId_entaoRetornaOperacoesDoCliente() throws Exception {
        mockMvc.perform(get("/api/admin/operacoes/historico")
                        .param("clienteId", cliente1.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2))); // 1 compra e 1 resgate
    }

    @Test
    void quandoConsultarHistoricoAdmin_comFiltroTipoAtivo_entaoRetornaOperacoesDoTipo() throws Exception {
        mockMvc.perform(get("/api/admin/operacoes/historico")
                        .param("tipoAtivo", "ACAO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2))); // 1 compra e 1 resgate de ACAO
    }

    @Test
    void quandoConsultarHistoricoAdmin_comFiltroData_entaoRetornaOperacoesDaData() throws Exception {
        String hoje = LocalDate.now().toString();
        mockMvc.perform(get("/api/admin/operacoes/historico")
                        .param("data", hoje))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2))); // 1 compra e 1 resgate de hoje
    }

    @Test
    void quandoConsultarHistoricoAdmin_comFiltroTipoOperacaoCompra_entaoRetornaApenasCompras() throws Exception {
        mockMvc.perform(get("/api/admin/operacoes/historico")
                        .param("tipoOperacao", "COMPRA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void quandoConsultarHistoricoAdmin_comFiltroTipoOperacaoResgate_entaoRetornaApenasResgates() throws Exception {
        mockMvc.perform(get("/api/admin/operacoes/historico")
                        .param("tipoOperacao", "RESGATE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void quandoConsultarHistoricoAdmin_comFiltrosCombinados_entaoRetornaResultadoCorreto() throws Exception {
        mockMvc.perform(get("/api/admin/operacoes/historico")
                        .param("clienteId", cliente1.getId().toString())
                        .param("tipoAtivo", "ACAO")
                        .param("tipoOperacao", "COMPRA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void quandoConsultarHistoricoAdmin_comFiltroSemResultados_entaoRetornaListaVazia() throws Exception {
        mockMvc.perform(get("/api/admin/operacoes/historico")
                        .param("clienteId", cliente2.getId().toString()) // Cliente 2 só tem compra de cripto
                        .param("tipoAtivo", "ACAO")) // Filtro por Ação
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", empty()));
    }

    @Test
    void quandoConsultarHistoricoAdmin_comDataSemOperacoes_entaoRetornaListaVazia() throws Exception {
        String dataFutura = LocalDate.now().plusDays(5).toString();
        mockMvc.perform(get("/api/admin/operacoes/historico")
                        .param("data", dataFutura))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", empty()));
    }

    @Test
    void quandoConsultarHistoricoAdmin_comClienteInexistente_entaoRetornaListaVazia() throws Exception {
        mockMvc.perform(get("/api/admin/operacoes/historico")
                        .param("clienteId", "9999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", empty()));
    }
}
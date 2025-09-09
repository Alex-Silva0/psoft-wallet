package com.psoft.wallet.controller;

import com.psoft.wallet.enums.EstadoCompra;
import com.psoft.wallet.enums.EstadoResgate;
import com.psoft.wallet.enums.TipoAtivo;
import com.psoft.wallet.enums.TipoPlano;
import com.psoft.wallet.model.Ativo;
import com.psoft.wallet.model.Cliente;
import com.psoft.wallet.model.Compra;
import com.psoft.wallet.model.Resgate;
import com.psoft.wallet.repository.AtivoRepository;
import com.psoft.wallet.repository.ClienteRepository;
import com.psoft.wallet.repository.CompraRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.psoft.wallet.repository.ResgateRepository;
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
class HistoricoClienteControllerTest {

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

    Cliente cliente;
    Ativo ativoAcao;
    Ativo ativoCripto;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setNomeCompleto("Cliente Histórico");
        cliente.setPlano(TipoPlano.PREMIUM);
        cliente.setCodigoAcesso("334455");
        cliente.setSaldo(new BigDecimal("5000.00"));
        clienteRepository.save(cliente);

        ativoAcao = new Ativo();
        ativoAcao.setNome("Ação Histórico");
        ativoAcao.setTipo(TipoAtivo.ACAO);
        ativoAcao.setDisponivel(true);
        ativoAcao.setValorAtual(new BigDecimal("100.00"));
        ativoRepository.save(ativoAcao);

        ativoCripto = new Ativo();
        ativoCripto.setNome("Cripto Histórico");
        ativoCripto.setTipo(TipoAtivo.CRIPTOMOEDA);
        ativoCripto.setDisponivel(true);
        ativoCripto.setValorAtual(new BigDecimal("500.00"));
        ativoRepository.save(ativoCripto);

        Compra compraAcao = new Compra();
        compraAcao.setCliente(cliente);
        compraAcao.setAtivo(ativoAcao);
        compraAcao.setQuantidade(10);
        compraAcao.setValorTotal(new BigDecimal("1000.00"));
        compraAcao.setValorUnitarioNaCompra(new BigDecimal("100.00"));
        compraAcao.setEstado(EstadoCompra.EM_CARTEIRA);
        compraAcao.setDataSolicitacao(LocalDateTime.now().minusDays(1));
        compraRepository.save(compraAcao);

        Compra compraCripto = new Compra();
        compraCripto.setCliente(cliente);
        compraCripto.setAtivo(ativoCripto);
        compraCripto.setQuantidade(2);
        compraCripto.setValorTotal(new BigDecimal("1000.00"));
        compraCripto.setValorUnitarioNaCompra(new BigDecimal("500.00"));
        compraCripto.setEstado(EstadoCompra.SOLICITADO);
        compraCripto.setDataSolicitacao(LocalDateTime.now());
        compraRepository.save(compraCripto);

        Resgate resgateAcao = new Resgate();
        resgateAcao.setCliente(cliente);
        resgateAcao.setAtivo(ativoAcao);
        resgateAcao.setQuantidade(1);
        resgateAcao.setValorTotal(new BigDecimal("100.00"));
        resgateAcao.setValorUnitario(new BigDecimal("100.00"));
        resgateAcao.setValorAquisicao(new BigDecimal("100.00"));
        resgateAcao.setLucro(BigDecimal.ZERO);
        resgateAcao.setImposto(BigDecimal.ZERO);
        resgateAcao.setEstado(EstadoResgate.CONFIRMADO);
        resgateAcao.setDataSolicitacao(LocalDateTime.now());
        resgateRepository.save(resgateAcao);
    }

    @AfterEach
    void tearDown() {
        compraRepository.deleteAll();
        resgateRepository.deleteAll();
        ativoRepository.deleteAll();
        clienteRepository.deleteAll();
    }

    @Test
    void quandoConsultarHistorico_semFiltros_entaoRetornaTodasOperacoes() throws Exception {
        mockMvc.perform(get("/clientes/{codigoAcesso}/historico-completo", "334455"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void quandoConsultarHistorico_comFiltroTipoAtivo_entaoRetornaOperacoesDoTipo() throws Exception {
        mockMvc.perform(get("/clientes/{codigoAcesso}/historico-completo", "334455")
                        .param("tipoAtivo", "ACAO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].tipoAtivo").value("ACAO"));
    }

    @Test
    void quandoConsultarHistorico_comFiltroPeriodo_entaoRetornaOperacoesNoPeriodo() throws Exception {
        String dataOntem = LocalDateTime.now().minusDays(1).toLocalDate().toString();
        mockMvc.perform(get("/clientes/{codigoAcesso}/historico-completo", "334455")
                        .param("dataInicio", dataOntem)
                        .param("dataFim", dataOntem))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void quandoConsultarHistorico_comFiltroStatus_entaoRetornaOperacoesComStatus() throws Exception {
        mockMvc.perform(get("/clientes/{codigoAcesso}/historico-completo", "334455")
                        .param("status", "SOLICITADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("SOLICITADO"));
    }

    @Test
    void quandoConsultarHistorico_comCodigoAcessoInvalido_entaoRetornaNaoEncontrado() throws Exception {
        mockMvc.perform(get("/clientes/{codigoAcesso}/historico-completo", "999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void quandoConsultarHistorico_comFiltrosCombinados_entaoRetornaResultadoCorreto() throws Exception {
        mockMvc.perform(get("/clientes/{codigoAcesso}/historico-completo", "334455")
                        .param("tipoAtivo", "ACAO")
                        .param("status", "EM_CARTEIRA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tipoAtivo").value("ACAO"))
                .andExpect(jsonPath("$[0].status").value("EM_CARTEIRA"));
    }

    @Test
    void quandoConsultarHistorico_comFiltroTipoOperacaoResgate_entaoRetornaApenasResgates() throws Exception {
        mockMvc.perform(get("/resgates/cliente/{codigoAcesso}/historico", "334455"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tipoOperacao").value("RESGATE"));
    }

    @Test
    void quandoConsultarHistorico_semResultadosParaFiltro_entaoRetornaListaVazia() throws Exception {
        mockMvc.perform(get("/clientes/{codigoAcesso}/historico-completo", "334455")
                        .param("status", "INEXISTENTE")) // Nenhum com este status
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
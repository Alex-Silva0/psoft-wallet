package com.psoft.wallet.controller;

import com.psoft.wallet.enums.EstadoCompra;
import com.psoft.wallet.enums.TipoAtivo;
import com.psoft.wallet.enums.TipoPlano;
import com.psoft.wallet.model.Ativo;
import com.psoft.wallet.model.Cliente;
import com.psoft.wallet.model.Compra;
import com.psoft.wallet.model.Resgate;
import com.psoft.wallet.repository.AtivoRepository;
import com.psoft.wallet.repository.ClienteRepository;
import com.psoft.wallet.repository.CompraRepository;
import com.psoft.wallet.repository.ResgateRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ExtratoControllerTest {

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
    Ativo ativo;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setNomeCompleto("Cliente Extrato");
        cliente.setPlano(TipoPlano.PREMIUM);
        cliente.setCodigoAcesso("778899");
        cliente.setSaldo(new BigDecimal("1000.00"));
        clienteRepository.save(cliente);

        ativo = new Ativo();
        ativo.setNome("Ação para Extrato");
        ativo.setTipo(TipoAtivo.ACAO);
        ativo.setDisponivel(true);
        ativo.setValorAtual(new BigDecimal("100.00"));
        ativoRepository.save(ativo);

        Compra compra = new Compra();
        compra.setCliente(cliente);
        compra.setAtivo(ativo);
        compra.setQuantidade(5);
        compra.setValorTotal(new BigDecimal("500.00"));
        compra.setValorUnitarioNaCompra(new BigDecimal("100.00"));
        compra.setEstado(EstadoCompra.EM_CARTEIRA);
        compra.setDataSolicitacao(LocalDateTime.now());
        compraRepository.save(compra);
    }

    @AfterEach
    void tearDown() {
        resgateRepository.deleteAll();
        compraRepository.deleteAll();
        ativoRepository.deleteAll();
        clienteRepository.deleteAll();
    }

    @Test
    void quandoExportarExtratoCSV_entaoRetornaArquivoComConteudoCorreto() throws Exception {
        MvcResult result = mockMvc.perform(get("/clientes/{codigoAcesso}/extrato-csv", "778899"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=extrato.csv"))
                .andReturn();

        String csvContent = result.getResponse().getContentAsString();

        assertTrue(csvContent.startsWith("Tipo Operação,Data,Ativo,Tipo Ativo"));
        assertTrue(csvContent.contains("COMPRA"));
        assertTrue(csvContent.contains("Ação para Extrato"));
        assertTrue(csvContent.contains("EM_CARTEIRA"));
    }

    @Test
    void quandoExportarExtratoCSV_comCodigoInvalido_entaoRetornaNaoEncontrado() throws Exception {
        mockMvc.perform(get("/clientes/{codigoAcesso}/extrato-csv", "999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void quandoExportarExtratoCSV_comClienteSemOperacoes_entaoRetornaApenasCabecalho() throws Exception {
        Cliente clienteSemOperacoes = new Cliente();
        clienteSemOperacoes.setNomeCompleto("Cliente Sem Operações");
        clienteSemOperacoes.setPlano(TipoPlano.NORMAL);
        clienteSemOperacoes.setCodigoAcesso("121212");
        clienteRepository.save(clienteSemOperacoes);

        MvcResult result = mockMvc.perform(get("/clientes/{codigoAcesso}/extrato-csv", "121212"))
                .andExpect(status().isOk())
                .andReturn();

        String csvContent = result.getResponse().getContentAsString();
        String expectedHeader = "Tipo Operação,Data,Ativo,Tipo Ativo,Quantidade,Valor Unitário,Valor Total,Imposto Pago,Status";

        assertEquals(expectedHeader, csvContent.trim());
    }

    @Test
    void quandoExportarExtratoCSV_comComprasEResgates_entaoRetornaTodasOperacoes() throws Exception {
        Resgate resgate = new Resgate();
        resgate.setCliente(cliente);
        resgate.setAtivo(ativo);
        resgate.setQuantidade(1);
        resgate.setValorTotal(new BigDecimal("100.00"));
        resgate.setValorUnitario(new BigDecimal("100.00"));
        resgate.setValorAquisicao(new BigDecimal("100.00"));
        resgate.setLucro(BigDecimal.ZERO);
        resgate.setImposto(new BigDecimal("0.00"));
        resgate.setEstado(com.psoft.wallet.enums.EstadoResgate.CONFIRMADO);
        resgate.setDataSolicitacao(LocalDateTime.now().plusHours(1));
        resgateRepository.save(resgate);

        MvcResult result = mockMvc.perform(get("/clientes/{codigoAcesso}/extrato-csv", "778899"))
                .andExpect(status().isOk())
                .andReturn();

        String csvContent = result.getResponse().getContentAsString();

        assertTrue(csvContent.contains("COMPRA"));
        assertTrue(csvContent.contains("RESGATE"));
        assertTrue(csvContent.contains("Ação para Extrato"));
        assertTrue(csvContent.contains("EM_CARTEIRA"));
        assertTrue(csvContent.contains("CONFIRMADO"));

        long lineCount = csvContent.lines().count();
        assertEquals(3, lineCount);
    }
}
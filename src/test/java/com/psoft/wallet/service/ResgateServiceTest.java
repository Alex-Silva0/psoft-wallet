package com.psoft.wallet.service;

import com.psoft.wallet.dto.ResgateRequestDTO;
import com.psoft.wallet.dto.ResgateResponseDTO;
import com.psoft.wallet.enums.EstadoResgate;
import com.psoft.wallet.enums.TipoAtivo;
import com.psoft.wallet.enums.TipoPlano;
import com.psoft.wallet.exception.ClienteNaoEncontradoException;
import com.psoft.wallet.exception.CodigoAcessoIncorretoException;
import com.psoft.wallet.exception.RegraDeNegocioException;
import com.psoft.wallet.model.Ativo;
import com.psoft.wallet.model.Carteira;
import com.psoft.wallet.model.Cliente;
import com.psoft.wallet.model.Resgate;
import com.psoft.wallet.repository.AtivoRepository;
import com.psoft.wallet.repository.CarteiraRepository;
import com.psoft.wallet.repository.ClienteRepository;
import com.psoft.wallet.repository.ResgateRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ResgateServiceTest {

    @Autowired
    ResgateService resgateService;

    @Autowired
    ResgateRepository resgateRepository;

    @Autowired
    ClienteRepository clienteRepository;

    @Autowired
    AtivoRepository ativoRepository;

    @Autowired
    CarteiraRepository carteiraRepository;

    Cliente cliente;
    Ativo ativoAcao;
    Ativo ativoTesouro;
    Ativo ativoCripto;
    Carteira carteira;

    @BeforeEach
    void setUp() {
        // Criar cliente
        cliente = new Cliente();
        cliente.setNomeCompleto("Cliente Teste");
        cliente.setPlano(TipoPlano.PREMIUM);
        cliente.setCodigoAcesso("112233");
        cliente.setSaldo(new BigDecimal("1000.00"));
        clienteRepository.save(cliente);

        // Criar ativos
        ativoAcao = new Ativo();
        ativoAcao.setNome("Ação Teste");
        ativoAcao.setTipo(TipoAtivo.ACAO);
        ativoAcao.setDisponivel(true);
        ativoAcao.setValorAtual(new BigDecimal("100.00"));
        ativoRepository.save(ativoAcao);

        ativoTesouro = new Ativo();
        ativoTesouro.setNome("Tesouro Teste");
        ativoTesouro.setTipo(TipoAtivo.TESOURO_DIRETO);
        ativoTesouro.setDisponivel(true);
        ativoTesouro.setValorAtual(new BigDecimal("200.00"));
        ativoRepository.save(ativoTesouro);

        ativoCripto = new Ativo();
        ativoCripto.setNome("Cripto Teste");
        ativoCripto.setTipo(TipoAtivo.CRIPTOMOEDA);
        ativoCripto.setDisponivel(true);
        ativoCripto.setValorAtual(new BigDecimal("50000.00"));
        ativoRepository.save(ativoCripto);

        // Criar carteira com ativos
        carteira = new Carteira();
        carteira.setCliente(cliente);
        carteira.setAtivo(ativoAcao);
        carteira.setQuantidade(10);
        carteira.setValorAquisicao(new BigDecimal("90.00"));
        carteira.setValorAtual(ativoAcao.getValorAtual());
        carteira.setDesempenho(ativoAcao.getValorAtual().subtract(new BigDecimal("90.00")));
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
    void quandoSolicitarResgate_comQuantidadeValida_entaoCriaResgateSolicitado() {
        // Given
        ResgateRequestDTO request = new ResgateRequestDTO();
        request.setAtivoId(ativoAcao.getId());
        request.setQuantidade(5);
        request.setCodigoAcesso("112233");

        // When
        ResgateResponseDTO response = resgateService.solicitarResgate(request);

        // Then
        assertNotNull(response);
        assertEquals(cliente.getId(), response.getClienteId());
        assertEquals(ativoAcao.getId(), response.getAtivoId());
        assertEquals(5, response.getQuantidade());
        assertEquals(new BigDecimal("100.00"), response.getValorUnitario());
        assertEquals(new BigDecimal("500.00"), response.getValorTotal());
        assertEquals(new BigDecimal("450.00"), response.getValorAquisicao()); // 5 * 90
        assertEquals(new BigDecimal("50.00"), response.getLucro()); // 500 - 450
        assertEquals(EstadoResgate.SOLICITADO, response.getEstado());
        assertNotNull(response.getDataSolicitacao());
    }

    @Test
    void quandoSolicitarResgate_comClienteInexistente_entaoLancaExcecao() {
        // Given
        ResgateRequestDTO request = new ResgateRequestDTO();
        request.setAtivoId(ativoAcao.getId());
        request.setQuantidade(5);
        request.setCodigoAcesso("999999");

        // When & Then
        assertThrows(ClienteNaoEncontradoException.class, () -> {
            resgateService.solicitarResgate(request);
        });
    }

    @Test
    void quandoSolicitarResgate_comAtivoInexistente_entaoLancaExcecao() {
        // Given
        ResgateRequestDTO request = new ResgateRequestDTO();
        request.setAtivoId(999L);
        request.setQuantidade(5);
        request.setCodigoAcesso("112233");

        // When & Then
        assertThrows(RegraDeNegocioException.class, () -> {
            resgateService.solicitarResgate(request);
        });
    }

    @Test
    void quandoSolicitarResgate_semAtivoNaCarteira_entaoLancaExcecao() {
        // Given
        ResgateRequestDTO request = new ResgateRequestDTO();
        request.setAtivoId(ativoTesouro.getId());
        request.setQuantidade(5);
        request.setCodigoAcesso("112233");

        // When & Then
        assertThrows(RegraDeNegocioException.class, () -> {
            resgateService.solicitarResgate(request);
        });
    }

    @Test
    void quandoSolicitarResgate_comQuantidadeInsuficiente_entaoLancaExcecao() {
        // Given
        ResgateRequestDTO request = new ResgateRequestDTO();
        request.setAtivoId(ativoAcao.getId());
        request.setQuantidade(15); // Mais que o disponível (10)
        request.setCodigoAcesso("112233");

        // When & Then
        assertThrows(RegraDeNegocioException.class, () -> {
            resgateService.solicitarResgate(request);
        });
    }

    // US15 - Testes para cálculo de impostos

    @Test
    void quandoSolicitarResgate_acaoComLucro_entaoCalculaImposto15Porcento() {
        // Given
        ResgateRequestDTO request = new ResgateRequestDTO();
        request.setAtivoId(ativoAcao.getId());
        request.setQuantidade(5);
        request.setCodigoAcesso("112233");

        // When
        ResgateResponseDTO response = resgateService.solicitarResgate(request);

        // Then
        BigDecimal lucro = new BigDecimal("50.00"); // 500 - 450
        BigDecimal impostoEsperado = lucro.multiply(new BigDecimal("0.15")); // 15%
        assertEquals(impostoEsperado, response.getImposto());
    }

    @Test
    void quandoSolicitarResgate_tesouroComLucro_entaoCalculaImposto10Porcento() {
        // Given - Adicionar tesouro à carteira
        Carteira carteiraTesouro = new Carteira();
        carteiraTesouro.setCliente(cliente);
        carteiraTesouro.setAtivo(ativoTesouro);
        carteiraTesouro.setQuantidade(5);
        carteiraTesouro.setValorAquisicao(new BigDecimal("180.00"));
        carteiraTesouro.setValorAtual(ativoTesouro.getValorAtual());
        carteiraTesouro.setDesempenho(ativoTesouro.getValorAtual().subtract(new BigDecimal("180.00")));
        carteiraTesouro.setDataEntradaCarteira(LocalDateTime.now());
        carteiraTesouro.setDataUltimaAtualizacao(LocalDateTime.now());
        carteiraRepository.save(carteiraTesouro);

        ResgateRequestDTO request = new ResgateRequestDTO();
        request.setAtivoId(ativoTesouro.getId());
        request.setQuantidade(2);
        request.setCodigoAcesso("112233");

        // When
        ResgateResponseDTO response = resgateService.solicitarResgate(request);

        // Then
        BigDecimal lucro = new BigDecimal("40.00"); // (200*2) - (180*2)
        BigDecimal impostoEsperado = lucro.multiply(new BigDecimal("0.10")); // 10%
        assertEquals(impostoEsperado, response.getImposto());
    }

    @Test
    void quandoSolicitarResgate_criptoComLucroBaixo_entaoCalculaImposto15Porcento() {
        // Given - Adicionar cripto à carteira com lucro baixo (≤ 5000)
        Carteira carteiraCripto = new Carteira();
        carteiraCripto.setCliente(cliente);
        carteiraCripto.setAtivo(ativoCripto);
        carteiraCripto.setQuantidade(1);
        carteiraCripto.setValorAquisicao(new BigDecimal("45000.00")); // Lucro de 5000
        carteiraCripto.setValorAtual(ativoCripto.getValorAtual());
        carteiraCripto.setDesempenho(ativoCripto.getValorAtual().subtract(new BigDecimal("45000.00")));
        carteiraCripto.setDataEntradaCarteira(LocalDateTime.now());
        carteiraCripto.setDataUltimaAtualizacao(LocalDateTime.now());
        carteiraRepository.save(carteiraCripto);

        ResgateRequestDTO request = new ResgateRequestDTO();
        request.setAtivoId(ativoCripto.getId());
        request.setQuantidade(1);
        request.setCodigoAcesso("112233");

        // When
        ResgateResponseDTO response = resgateService.solicitarResgate(request);

        // Then
        BigDecimal lucro = new BigDecimal("5000.00"); // 50000 - 45000
        BigDecimal impostoEsperado = lucro.multiply(new BigDecimal("0.15")); // 15% (lucro ≤ 5000)
        assertEquals(impostoEsperado, response.getImposto());
    }

    @Test
    void quandoSolicitarResgate_criptoComLucroAlto_entaoCalculaImposto22_5Porcento() {
        // Given - Adicionar cripto à carteira com lucro alto
        Carteira carteiraCripto = new Carteira();
        carteiraCripto.setCliente(cliente);
        carteiraCripto.setAtivo(ativoCripto);
        carteiraCripto.setQuantidade(1);
        carteiraCripto.setValorAquisicao(new BigDecimal("30000.00"));
        carteiraCripto.setValorAtual(ativoCripto.getValorAtual());
        carteiraCripto.setDesempenho(ativoCripto.getValorAtual().subtract(new BigDecimal("30000.00")));
        carteiraCripto.setDataEntradaCarteira(LocalDateTime.now());
        carteiraCripto.setDataUltimaAtualizacao(LocalDateTime.now());
        carteiraRepository.save(carteiraCripto);

        ResgateRequestDTO request = new ResgateRequestDTO();
        request.setAtivoId(ativoCripto.getId());
        request.setQuantidade(1);
        request.setCodigoAcesso("112233");

        // When
        ResgateResponseDTO response = resgateService.solicitarResgate(request);

        // Then
        BigDecimal lucro = new BigDecimal("20000.00"); // 50000 - 30000
        BigDecimal impostoEsperado = lucro.multiply(new BigDecimal("0.225")).setScale(4, RoundingMode.HALF_UP); // 22.5% (lucro > 5000)
        assertEquals(impostoEsperado, response.getImposto());
    }

    @Test
    void quandoSolicitarResgate_comPrejuizo_entaoNaoCalculaImposto() {
        // Given - Atualizar valor do ativo para menor que o de aquisição
        ativoAcao.setValorAtual(new BigDecimal("80.00"));
        ativoRepository.save(ativoAcao);

        ResgateRequestDTO request = new ResgateRequestDTO();
        request.setAtivoId(ativoAcao.getId());
        request.setQuantidade(5);
        request.setCodigoAcesso("112233");

        // When
        ResgateResponseDTO response = resgateService.solicitarResgate(request);

        // Then
        BigDecimal prejuizo = new BigDecimal("-50.00"); // (80*5) - (90*5)
        assertEquals(BigDecimal.ZERO, response.getImposto());
    }

    // US16 - Testes para visualizar resgates

    @Test
    void quandoVisualizarResgates_comResgatesExistentes_entaoRetornaLista() {
        // Given - Criar resgate
        ResgateRequestDTO request = new ResgateRequestDTO();
        request.setAtivoId(ativoAcao.getId());
        request.setQuantidade(3);
        request.setCodigoAcesso("112233");
        resgateService.solicitarResgate(request);

        // When
        List<ResgateResponseDTO> resgates = resgateService.visualizarResgates("112233");

        // Then
        assertEquals(1, resgates.size());
        ResgateResponseDTO resgate = resgates.get(0);
        assertEquals(3, resgate.getQuantidade());
        assertEquals(EstadoResgate.SOLICITADO, resgate.getEstado());
    }

    @Test
    void quandoVisualizarResgates_semResgates_entaoRetornaListaVazia() {
        // When
        List<ResgateResponseDTO> resgates = resgateService.visualizarResgates("112233");

        // Then
        assertTrue(resgates.isEmpty());
    }

    @Test
    void quandoVisualizarResgates_comClienteInexistente_entaoLancaExcecao() {
        // When & Then
        assertThrows(ClienteNaoEncontradoException.class, () -> {
            resgateService.visualizarResgates("999999");
        });
    }

    // US17 - Testes para confirmar e finalizar resgates

    @Test
    void quandoConfirmarResgate_comResgateSolicitado_entaoAtualizaParaConfirmado() {
        // Given - Criar resgate
        ResgateRequestDTO request = new ResgateRequestDTO();
        request.setAtivoId(ativoAcao.getId());
        request.setQuantidade(3);
        request.setCodigoAcesso("112233");
        ResgateResponseDTO resgateCriado = resgateService.solicitarResgate(request);

        // When
        ResgateResponseDTO response = resgateService.confirmarResgate(resgateCriado.getId());

        // Then
        assertEquals(EstadoResgate.CONFIRMADO, response.getEstado());
        assertNotNull(response.getDataConfirmacao());
    }

    @Test
    void quandoConfirmarResgate_comResgateNaoSolicitado_entaoLancaExcecao() {
        // Given - Criar resgate e confirmar
        ResgateRequestDTO request = new ResgateRequestDTO();
        request.setAtivoId(ativoAcao.getId());
        request.setQuantidade(3);
        request.setCodigoAcesso("112233");
        ResgateResponseDTO resgateCriado = resgateService.solicitarResgate(request);
        resgateService.confirmarResgate(resgateCriado.getId());

        // When & Then - Tentar confirmar novamente
        assertThrows(RegraDeNegocioException.class, () -> {
            resgateService.confirmarResgate(resgateCriado.getId());
        });
    }

    @Test
    void quandoFinalizarResgate_comResgateConfirmado_entaoAtualizaParaEmConta() {
        // Given - Criar e confirmar resgate
        ResgateRequestDTO request = new ResgateRequestDTO();
        request.setAtivoId(ativoAcao.getId());
        request.setQuantidade(3);
        request.setCodigoAcesso("112233");
        ResgateResponseDTO resgateCriado = resgateService.solicitarResgate(request);
        resgateService.confirmarResgate(resgateCriado.getId());

        // When
        ResgateResponseDTO response = resgateService.finalizarResgate(resgateCriado.getId());

        // Then
        assertEquals(EstadoResgate.EM_CONTA, response.getEstado());
        assertNotNull(response.getDataFinalizacao());

        // Verificar se a carteira foi atualizada
        Carteira carteiraAtualizada = carteiraRepository.findByClienteAndAtivo(cliente, ativoAcao).orElse(null);
        assertNotNull(carteiraAtualizada);
        assertEquals(7, carteiraAtualizada.getQuantidade()); // 10 - 3

        // Verificar se o saldo do cliente foi atualizado
        Cliente clienteAtualizado = clienteRepository.findById(cliente.getId()).orElse(null);
        assertNotNull(clienteAtualizado);
        BigDecimal valorLiquido = new BigDecimal("300.00").subtract(response.getImposto());
        assertEquals(new BigDecimal("1000.00").add(valorLiquido), clienteAtualizado.getSaldo());
    }

    @Test
    void quandoFinalizarResgate_comResgateNaoConfirmado_entaoLancaExcecao() {
        // Given - Criar resgate sem confirmar
        ResgateRequestDTO request = new ResgateRequestDTO();
        request.setAtivoId(ativoAcao.getId());
        request.setQuantidade(3);
        request.setCodigoAcesso("112233");
        ResgateResponseDTO resgateCriado = resgateService.solicitarResgate(request);

        // When & Then
        assertThrows(RegraDeNegocioException.class, () -> {
            resgateService.finalizarResgate(resgateCriado.getId());
        });
    }

    @Test
    void quandoFinalizarResgate_comQuantidadeInsuficiente_entaoLancaExcecao() {
        // Given - Criar e confirmar resgate
        ResgateRequestDTO request = new ResgateRequestDTO();
        request.setAtivoId(ativoAcao.getId());
        request.setQuantidade(3);
        request.setCodigoAcesso("112233");
        ResgateResponseDTO resgateCriado = resgateService.solicitarResgate(request);
        resgateService.confirmarResgate(resgateCriado.getId());

        // Simular remoção de ativos da carteira
        carteira.setQuantidade(0);
        carteiraRepository.save(carteira);

        // When & Then
        assertThrows(RegraDeNegocioException.class, () -> {
            resgateService.finalizarResgate(resgateCriado.getId());
        });
    }

    @Test
    void quandoFinalizarResgate_comQuantidadeExata_entaoRemoveCarteira() {
        // Given - Criar e confirmar resgate de toda a quantidade
        ResgateRequestDTO request = new ResgateRequestDTO();
        request.setAtivoId(ativoAcao.getId());
        request.setQuantidade(10); // Toda a quantidade
        request.setCodigoAcesso("112233");
        ResgateResponseDTO resgateCriado = resgateService.solicitarResgate(request);
        resgateService.confirmarResgate(resgateCriado.getId());

        // When
        resgateService.finalizarResgate(resgateCriado.getId());

        // Then - Verificar se a carteira foi removida
        assertFalse(carteiraRepository.findByClienteAndAtivo(cliente, ativoAcao).isPresent());
    }

    @Test
    void quandoListarResgatesSolicitados_comResgatesExistentes_entaoRetornaLista() {
        // Given - Criar resgates com diferentes estados
        ResgateRequestDTO request1 = new ResgateRequestDTO();
        request1.setAtivoId(ativoAcao.getId());
        request1.setQuantidade(3);
        request1.setCodigoAcesso("112233");
        ResgateResponseDTO resgate1 = resgateService.solicitarResgate(request1);

        ResgateRequestDTO request2 = new ResgateRequestDTO();
        request2.setAtivoId(ativoAcao.getId());
        request2.setQuantidade(2);
        request2.setCodigoAcesso("112233");
        ResgateResponseDTO resgate2 = resgateService.solicitarResgate(request2);

        // Confirmar um dos resgates
        resgateService.confirmarResgate(resgate1.getId());

        // When
        List<ResgateResponseDTO> resgatesSolicitados = resgateService.listarResgatesSolicitados();

        // Then
        assertEquals(1, resgatesSolicitados.size());
        assertEquals(resgate2.getId(), resgatesSolicitados.get(0).getId());
        assertEquals(EstadoResgate.SOLICITADO, resgatesSolicitados.get(0).getEstado());
    }

    @Test
    void quandoListarResgatesSolicitados_semResgates_entaoRetornaListaVazia() {
        // When
        List<ResgateResponseDTO> resgatesSolicitados = resgateService.listarResgatesSolicitados();

        // Then
        assertTrue(resgatesSolicitados.isEmpty());
    }

    @Test
    void quandoSolicitarResgate_comCodigoAcessoIncorreto_entaoLancaExcecao() {
        // Given
        ResgateRequestDTO request = new ResgateRequestDTO();
        request.setAtivoId(ativoAcao.getId());
        request.setQuantidade(5);
        request.setCodigoAcesso("000000"); // Código incorreto

        // When & Then
        assertThrows(ClienteNaoEncontradoException.class, () -> {
            resgateService.solicitarResgate(request);
        });
    }

    @Test
    void quandoConfirmarResgateInexistente_entaoLancaExcecao() {
        // When & Then
        assertThrows(RegraDeNegocioException.class, () -> {
            resgateService.confirmarResgate(999L);
        });
    }

    @Test
    void quandoFinalizarResgateInexistente_entaoLancaExcecao() {
        // When & Then
        assertThrows(RegraDeNegocioException.class, () -> {
            resgateService.finalizarResgate(999L);
        });
    }
}

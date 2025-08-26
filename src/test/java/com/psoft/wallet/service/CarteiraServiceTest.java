package com.psoft.wallet.service;

import com.psoft.wallet.dto.CarteiraDTO;
import com.psoft.wallet.enums.TipoAtivo;
import com.psoft.wallet.enums.TipoPlano;
import com.psoft.wallet.exception.ClienteNaoEncontradoException;
import com.psoft.wallet.model.Ativo;
import com.psoft.wallet.model.Carteira;
import com.psoft.wallet.model.Cliente;
import com.psoft.wallet.repository.AtivoRepository;
import com.psoft.wallet.repository.CarteiraRepository;
import com.psoft.wallet.repository.ClienteRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CarteiraServiceTest {

    @Autowired
    CarteiraService carteiraService;

    @Autowired
    CarteiraRepository carteiraRepository;

    @Autowired
    ClienteRepository clienteRepository;

    @Autowired
    AtivoRepository ativoRepository;

    Cliente cliente;
    Ativo ativo;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setNomeCompleto("Cliente Teste");
        cliente.setPlano(TipoPlano.PREMIUM);
        cliente.setCodigoAcesso("112233");
        cliente.setSaldo(new BigDecimal("1000.00"));
        clienteRepository.save(cliente);

        ativo = new Ativo();
        ativo.setNome("Ação Teste");
        ativo.setTipo(TipoAtivo.ACAO);
        ativo.setDisponivel(true);
        ativo.setValorAtual(new BigDecimal("100.00"));
        ativoRepository.save(ativo);
    }

    @AfterEach
    void tearDown() {
        carteiraRepository.deleteAll();
        ativoRepository.deleteAll();
        clienteRepository.deleteAll();
    }

    @Test
    void quandoAdicionarAtivoACarteira_comAtivoNovo_entaoCriaNovaEntrada() {
        // Given
        Integer quantidade = 5;
        BigDecimal valorAquisicao = new BigDecimal("95.00");

        // When
        carteiraService.adicionarAtivoACarteira(cliente, ativo, quantidade, valorAquisicao);

        // Then
        List<Carteira> carteira = carteiraRepository.findAllByCliente(cliente);
        assertEquals(1, carteira.size());

        Carteira entrada = carteira.get(0);
        assertEquals(cliente.getId(), entrada.getCliente().getId());
        assertEquals(ativo.getId(), entrada.getAtivo().getId());
        assertEquals(quantidade, entrada.getQuantidade());
        assertEquals(valorAquisicao, entrada.getValorAquisicao());
        assertEquals(ativo.getValorAtual(), entrada.getValorAtual());
        assertEquals(ativo.getValorAtual().subtract(valorAquisicao), entrada.getDesempenho());
        assertNotNull(entrada.getDataEntradaCarteira());
        assertNotNull(entrada.getDataUltimaAtualizacao());
    }

    @Test
    void quandoAdicionarAtivoACarteira_comAtivoExistente_entaoAtualizaQuantidadeEValorMedio() {
        // Given: Adicionar primeira entrada
        carteiraService.adicionarAtivoACarteira(cliente, ativo, 5, new BigDecimal("90.00"));

        // When: Adicionar mais quantidade com preço diferente
        carteiraService.adicionarAtivoACarteira(cliente, ativo, 3, new BigDecimal("110.00"));

        // Then
        List<Carteira> carteira = carteiraRepository.findAllByCliente(cliente);
        assertEquals(1, carteira.size());

        Carteira entrada = carteira.get(0);
        assertEquals(8, entrada.getQuantidade()); // 5 + 3

        // Valor médio ponderado: (5*90 + 3*110) / 8 = (450 + 330) / 8 = 780 / 8 = 97.50
        BigDecimal valorMedioEsperado = new BigDecimal("97.50");
        assertEquals(0, valorMedioEsperado.compareTo(entrada.getValorAquisicao()), 
                "Valor médio deve ser calculado corretamente");
    }

    @Test
    void quandoVisualizarCarteira_comAtivosExistentes_entaoRetornaListaDeDTOs() {
        // Given: Adicionar ativos à carteira
        carteiraService.adicionarAtivoACarteira(cliente, ativo, 5, new BigDecimal("95.00"));

        // When
        List<CarteiraDTO> carteiraDTOs = carteiraService.visualizarCarteira("112233");

        // Then
        assertEquals(1, carteiraDTOs.size());
        CarteiraDTO dto = carteiraDTOs.get(0);
        assertEquals(ativo.getId(), dto.getAtivoId());
        assertEquals(ativo.getNome(), dto.getNomeAtivo());
        assertEquals(ativo.getTipo(), dto.getTipoAtivo());
        assertEquals(5, dto.getQuantidade());
        assertEquals(new BigDecimal("95.00"), dto.getValorAquisicao());
        assertEquals(ativo.getValorAtual(), dto.getValorAtual());
        assertEquals(ativo.getValorAtual().subtract(new BigDecimal("95.00")), dto.getDesempenho());
        assertNotNull(dto.getDataEntradaCarteira());
    }

    @Test
    void quandoVisualizarCarteira_comCodigoInexistente_entaoLancaExcecao() {
        // When & Then
        assertThrows(ClienteNaoEncontradoException.class, () -> {
            carteiraService.visualizarCarteira("999999");
        });
    }

    @Test
    void quandoVisualizarCarteira_semAtivos_entaoRetornaListaVazia() {
        // When
        List<CarteiraDTO> carteiraDTOs = carteiraService.visualizarCarteira("112233");

        // Then
        assertTrue(carteiraDTOs.isEmpty());
    }

    @Test
    void quandoAtualizarValoresCarteira_entaoAtualizaValoresEAtualizacoes() {
        // Given: Adicionar ativo à carteira
        carteiraService.adicionarAtivoACarteira(cliente, ativo, 5, new BigDecimal("95.00"));

        // Alterar valor do ativo
        ativo.setValorAtual(new BigDecimal("105.00"));
        ativoRepository.save(ativo);

        // When
        carteiraService.atualizarValoresCarteira();

        // Then
        List<Carteira> carteira = carteiraRepository.findAllByCliente(cliente);
        assertEquals(1, carteira.size());

        Carteira entrada = carteira.get(0);
        assertEquals(new BigDecimal("105.00"), entrada.getValorAtual());
        assertEquals(new BigDecimal("50.00"), entrada.getDesempenho()); // (105-95)*5
        assertNotNull(entrada.getDataUltimaAtualizacao());
    }

    @Test
    void quandoAdicionarAtivoACarteira_comMultiplosAtivos_entaoMantemSeparados() {
        // Given: Criar segundo ativo
        Ativo ativo2 = new Ativo();
        ativo2.setNome("Criptomoeda Teste");
        ativo2.setTipo(TipoAtivo.CRIPTOMOEDA);
        ativo2.setDisponivel(true);
        ativo2.setValorAtual(new BigDecimal("50.00"));
        ativoRepository.save(ativo2);

        // When: Adicionar ambos os ativos
        carteiraService.adicionarAtivoACarteira(cliente, ativo, 3, new BigDecimal("95.00"));
        carteiraService.adicionarAtivoACarteira(cliente, ativo2, 10, new BigDecimal("45.00"));

        // Then
        List<Carteira> carteira = carteiraRepository.findAllByCliente(cliente);
        assertEquals(2, carteira.size());

        // Verificar que são ativos diferentes
        assertNotEquals(carteira.get(0).getAtivo().getId(), carteira.get(1).getAtivo().getId());
    }
}

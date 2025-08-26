package com.psoft.wallet.service;

import com.psoft.wallet.dto.CompraRequestDTO;
import com.psoft.wallet.dto.ConfirmacaoCompraDTO;
import com.psoft.wallet.dto.ExecucaoCompraDTO;
import com.psoft.wallet.enums.EstadoCompra;
import com.psoft.wallet.exception.AtivoNaoEncontradoException;
import com.psoft.wallet.exception.ClienteNaoEncontradoException;
import com.psoft.wallet.exception.CompraNaoEncontradaException;
import com.psoft.wallet.exception.RegraDeNegocioException;
import com.psoft.wallet.model.Ativo;
import com.psoft.wallet.model.Cliente;
import com.psoft.wallet.model.Compra;
import com.psoft.wallet.repository.AtivoRepository;
import com.psoft.wallet.repository.ClienteRepository;
import com.psoft.wallet.repository.CompraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CompraService {

    private final ClienteRepository clienteRepository;
    private final AtivoRepository ativoRepository;
    private final CompraRepository compraRepository;
    private final CarteiraService carteiraService;
    private final NotificacaoService notificacaoService;

    public CompraService(ClienteRepository clienteRepository, AtivoRepository ativoRepository, CompraRepository compraRepository, CarteiraService carteiraService, NotificacaoService notificacaoService) {
        this.clienteRepository = clienteRepository;
        this.ativoRepository = ativoRepository;
        this.compraRepository = compraRepository;
        this.carteiraService = carteiraService;
        this.notificacaoService = notificacaoService;
    }

    @Transactional
    public Compra solicitarCompra(CompraRequestDTO compraDTO) {
        Cliente cliente = clienteRepository.findByCodigoAcesso(compraDTO.getCodigoAcessoCliente())
                .orElseThrow(() -> new ClienteNaoEncontradoException("Cliente não encontrado."));

        Ativo ativo = ativoRepository.findById(compraDTO.getAtivoId())
                .orElseThrow(() -> new AtivoNaoEncontradoException("Ativo não encontrado."));

        if (!ativo.isDisponivel()) {
            throw new RegraDeNegocioException("O ativo '" + ativo.getNome() + "' não está disponível para compra.");
        }

        if (compraDTO.getQuantidade() <= 0) {
            throw new RegraDeNegocioException("A quantidade da compra deve ser maior que zero.");
        }

        BigDecimal valorTotalCompra = ativo.getValorAtual().multiply(new BigDecimal(compraDTO.getQuantidade()));
        if (cliente.getSaldo().compareTo(valorTotalCompra) < 0) {
            throw new RegraDeNegocioException("Saldo insuficiente para realizar a compra.");
        }

        Compra novaCompra = new Compra();
        novaCompra.setCliente(cliente);
        novaCompra.setAtivo(ativo);
        novaCompra.setQuantidade(compraDTO.getQuantidade());
        novaCompra.setValorUnitarioNaCompra(ativo.getValorAtual());
        novaCompra.setValorTotal(valorTotalCompra);
        novaCompra.setDataSolicitacao(LocalDateTime.now());
        novaCompra.setEstado(EstadoCompra.SOLICITADO);

        return compraRepository.save(novaCompra);
    }

    public List<Compra> listarComprasPorCliente(String codigoAcesso) {
        Cliente cliente = clienteRepository.findByCodigoAcesso(codigoAcesso)
                .orElseThrow(() -> new ClienteNaoEncontradoException("Cliente não encontrado."));

        return compraRepository.findAllByCliente(cliente);
    }

    @Transactional
    public Compra confirmarDisponibilidadeCompra(ConfirmacaoCompraDTO confirmacaoDTO) {
        Compra compra = compraRepository.findById(confirmacaoDTO.getCompraId())
                .orElseThrow(() -> new CompraNaoEncontradaException("Compra não encontrada."));

        if (compra.getEstado() != EstadoCompra.SOLICITADO) {
            throw new RegraDeNegocioException("A compra deve estar no estado 'Solicitado' para ser confirmada.");
        }

        Ativo ativo = compra.getAtivo();
        if (!ativo.isDisponivel()) {
            throw new RegraDeNegocioException("O ativo não está disponível para compra.");
        }

        // Verificar liquidez (saldo do cliente)
        Cliente cliente = compra.getCliente();
        if (cliente.getSaldo().compareTo(compra.getValorTotal()) < 0) {
            throw new RegraDeNegocioException("Cliente não possui saldo suficiente para a compra.");
        }

        // Atualizar estado da compra
        compra.setEstado(EstadoCompra.DISPONIVEL);
        compra = compraRepository.save(compra);

        // Notificar cliente sobre disponibilidade
        notificacaoService.notificarDisponibilidade(cliente, ativo);

        return compra;
    }

    @Transactional
    public Compra confirmarExecucaoCompra(ExecucaoCompraDTO execucaoDTO) {
        Cliente cliente = clienteRepository.findByCodigoAcesso(execucaoDTO.getCodigoAcessoCliente())
                .orElseThrow(() -> new ClienteNaoEncontradoException("Cliente não encontrado."));

        Compra compra = compraRepository.findById(execucaoDTO.getCompraId())
                .orElseThrow(() -> new CompraNaoEncontradaException("Compra não encontrada."));

        if (!compra.getCliente().getId().equals(cliente.getId())) {
            throw new RegraDeNegocioException("Cliente não pode confirmar compra de outro cliente.");
        }

        if (compra.getEstado() != EstadoCompra.DISPONIVEL) {
            throw new RegraDeNegocioException("A compra deve estar no estado 'Disponível' para ser executada.");
        }

        // Verificar se ainda há saldo suficiente
        if (cliente.getSaldo().compareTo(compra.getValorTotal()) < 0) {
            throw new RegraDeNegocioException("Saldo insuficiente para executar a compra.");
        }

        // Debitar saldo do cliente
        cliente.setSaldo(cliente.getSaldo().subtract(compra.getValorTotal()));
        clienteRepository.save(cliente);

        // Atualizar estado da compra para "Comprado"
        compra.setEstado(EstadoCompra.COMPRADO);
        compra = compraRepository.save(compra);

        // Adicionar ativo à carteira do cliente
        carteiraService.adicionarAtivoACarteira(cliente, compra.getAtivo(), compra.getQuantidade(), compra.getValorUnitarioNaCompra());

        // Atualizar estado para "Em carteira"
        compra.setEstado(EstadoCompra.EM_CARTEIRA);
        compra = compraRepository.save(compra);

        return compra;
    }
}

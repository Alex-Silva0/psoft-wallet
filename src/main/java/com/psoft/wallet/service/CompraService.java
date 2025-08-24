package com.psoft.wallet.service;

import com.psoft.wallet.dto.CompraRequestDTO;
import com.psoft.wallet.enums.EstadoCompra;
import com.psoft.wallet.exception.AtivoNaoEncontradoException;
import com.psoft.wallet.exception.ClienteNaoEncontradoException;
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

    public CompraService(ClienteRepository clienteRepository, AtivoRepository ativoRepository, CompraRepository compraRepository) {
        this.clienteRepository = clienteRepository;
        this.ativoRepository = ativoRepository;
        this.compraRepository = compraRepository;
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
}
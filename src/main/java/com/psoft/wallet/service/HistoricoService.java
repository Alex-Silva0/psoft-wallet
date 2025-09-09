package com.psoft.wallet.service;

import com.psoft.wallet.dto.OperacaoDTO;
import com.psoft.wallet.enums.EstadoCompra;
import com.psoft.wallet.enums.EstadoResgate;
import com.psoft.wallet.enums.TipoAtivo;
import com.psoft.wallet.exception.ClienteNaoEncontradoException;
import com.psoft.wallet.model.Cliente;
import com.psoft.wallet.model.Compra;
import com.psoft.wallet.model.Resgate;
import com.psoft.wallet.repository.ClienteRepository;
import com.psoft.wallet.repository.CompraRepository;
import com.psoft.wallet.repository.ResgateRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class HistoricoService {

    private final CompraRepository compraRepository;
    private final ResgateRepository resgateRepository;
    private final ClienteRepository clienteRepository;

    public HistoricoService(CompraRepository compraRepository, ResgateRepository resgateRepository, ClienteRepository clienteRepository) {
        this.compraRepository = compraRepository;
        this.resgateRepository = resgateRepository;
        this.clienteRepository = clienteRepository;
    }

    public List<OperacaoDTO> getHistoricoCliente(String codigoAcesso, TipoAtivo tipoAtivo, LocalDate dataInicio, LocalDate dataFim, String status) {
        Cliente cliente = clienteRepository.findByCodigoAcesso(codigoAcesso)
                .orElseThrow(() -> new ClienteNaoEncontradoException("Cliente não encontrado."));

        return getHistorico(cliente.getId(), tipoAtivo, dataInicio, dataFim, status, null);
    }

    public List<OperacaoDTO> getHistoricoAdmin(Long clienteId, TipoAtivo tipoAtivo, LocalDate data, String tipoOperacao) {
        LocalDate dataInicio = (data != null) ? data : null;
        LocalDate dataFim = (data != null) ? data : null;

        return getHistorico(clienteId, tipoAtivo, dataInicio, dataFim, null, tipoOperacao);
    }

    private List<OperacaoDTO> getHistorico(Long clienteId, TipoAtivo tipoAtivo, LocalDate dataInicio,
                                           LocalDate dataFim, String status, String tipoOperacao) {
        List<Compra> compras = new ArrayList<>();
        if (isCompra(tipoOperacao)) {
            EstadoCompra estadoCompra = null;
            if (status != null && isEstadoCompra(status)) {
                estadoCompra = EstadoCompra.valueOf(status.toUpperCase());
            }
            if (status == null || estadoCompra != null) {
                compras = compraRepository.findWithFilters(clienteId, tipoAtivo, dataInicio, dataFim, estadoCompra);
            }
        }

        List<Resgate> resgates = new ArrayList<>();
        if (isResgate(tipoOperacao)) {
            EstadoResgate estadoResgate = null;
            if (status != null && isEstadoResgate(status)) {
                estadoResgate = EstadoResgate.valueOf(status.toUpperCase());
            }
            if (status == null || estadoResgate != null) {
                resgates = resgateRepository.findWithFilters(clienteId, tipoAtivo, dataInicio, dataFim, estadoResgate);
            }
        }

        Stream<OperacaoDTO> comprasStream = compras.stream().map(this::compraToOperacaoDTO);
        Stream<OperacaoDTO> resgatesStream = resgates.stream().map(this::resgateToOperacaoDTO);

        return Stream.concat(comprasStream, resgatesStream)
                .sorted(Comparator.comparing(OperacaoDTO::getData).reversed())
                .collect(Collectors.toList());
    }

    private OperacaoDTO compraToOperacaoDTO(Compra compra) {
        return new OperacaoDTO(
                "COMPRA",
                compra.getId(),
                compra.getAtivo().getNome(),
                compra.getAtivo().getTipo(),
                compra.getQuantidade(),
                compra.getValorTotal(),
                compra.getEstado().name(),
                compra.getDataSolicitacao()
        );
    }

    private OperacaoDTO resgateToOperacaoDTO(Resgate resgate) {
        return new OperacaoDTO(
                "RESGATE",
                resgate.getId(),
                resgate.getAtivo().getNome(),
                resgate.getAtivo().getTipo(),
                resgate.getQuantidade(),
                resgate.getValorTotal(),
                resgate.getEstado().name(),
                resgate.getDataSolicitacao()
        );
    }

    private boolean isCompra(String tipoOperacao) {
        return tipoOperacao == null || tipoOperacao.equalsIgnoreCase("COMPRA");
    }

    private boolean isResgate(String tipoOperacao) {
        return tipoOperacao == null || tipoOperacao.equalsIgnoreCase("RESGATE");
    }

    private boolean isEstadoCompra(String status) {
        for (EstadoCompra e : EstadoCompra.values()) {
            if (e.name().equalsIgnoreCase(status)) return true;
        }
        return false;
    }

    private boolean isEstadoResgate(String status) {
        for (EstadoResgate e : EstadoResgate.values()) {
            if (e.name().equalsIgnoreCase(status)) return true;
        }
        return false;
    }
}
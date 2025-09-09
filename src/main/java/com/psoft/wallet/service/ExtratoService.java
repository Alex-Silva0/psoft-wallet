package com.psoft.wallet.service;

import com.psoft.wallet.exception.ClienteNaoEncontradoException;
import com.psoft.wallet.model.Cliente;
import com.psoft.wallet.model.Compra;
import com.psoft.wallet.model.Resgate;
import com.psoft.wallet.repository.ClienteRepository;
import com.psoft.wallet.repository.CompraRepository;
import com.psoft.wallet.repository.ResgateRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

@Service
public class ExtratoService {

    private final ClienteRepository clienteRepository;
    private final CompraRepository compraRepository;
    private final ResgateRepository resgateRepository;

    public ExtratoService(ClienteRepository clienteRepository,
                          CompraRepository compraRepository, ResgateRepository resgateRepository) {
        this.clienteRepository = clienteRepository;
        this.compraRepository = compraRepository;
        this.resgateRepository = resgateRepository;
    }

    public String gerarExtratoCSV(String codigoAcesso) throws IOException {
        Cliente cliente = clienteRepository.findByCodigoAcesso(codigoAcesso)
                .orElseThrow(() -> new ClienteNaoEncontradoException("Cliente não encontrado."));

        List<Compra> compras = compraRepository.findAllByCliente(cliente);
        List<Resgate> resgates = resgateRepository.findAllByCliente(cliente);

        StringWriter stringWriter = new StringWriter();
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader("Tipo Operação", "Data", "Ativo", "Tipo Ativo", "Quantidade", "Valor Unitário", "Valor Total", "Imposto Pago", "Status")
                .build();

        try (CSVPrinter csvPrinter = new CSVPrinter(stringWriter, csvFormat)) {
            for (Compra compra : compras) {
                csvPrinter.printRecord(
                        "COMPRA",
                        compra.getDataSolicitacao(),
                        compra.getAtivo().getNome(),
                        compra.getAtivo().getTipo(),
                        compra.getQuantidade(),
                        compra.getValorUnitarioNaCompra(),
                        compra.getValorTotal(),
                        "N/A", // Compra não tem imposto direto
                        compra.getEstado()
                );
            }

            for (Resgate resgate : resgates) {
                csvPrinter.printRecord(
                        "RESGATE",
                        resgate.getDataSolicitacao(),
                        resgate.getAtivo().getNome(),
                        resgate.getAtivo().getTipo(),
                        resgate.getQuantidade(),
                        resgate.getValorUnitario(),
                        resgate.getValorTotal(),
                        resgate.getImposto(),
                        resgate.getEstado()
                );
            }
        }

        return stringWriter.toString();
    }
}
package com.psoft.wallet.controller;

import com.psoft.wallet.dto.CompraRequestDTO;
import com.psoft.wallet.dto.ConfirmacaoCompraDTO;
import com.psoft.wallet.dto.ExecucaoCompraDTO;
import com.psoft.wallet.dto.OperacaoDTO;
import com.psoft.wallet.enums.TipoAtivo;
import com.psoft.wallet.model.Compra;
import com.psoft.wallet.service.CompraService;
import com.psoft.wallet.service.HistoricoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/compras")
public class CompraController {

    private final CompraService compraService;
    private final HistoricoService historicoService;

    public CompraController(CompraService compraService, HistoricoService historicoService) {
        this.compraService = compraService;
        this.historicoService = historicoService;
    }

    @PostMapping
    public ResponseEntity<Compra> solicitarCompra(@RequestBody CompraRequestDTO compraDTO) {
        Compra novaCompra = compraService.solicitarCompra(compraDTO);
        return new ResponseEntity<>(novaCompra, HttpStatus.CREATED);
    }

    @GetMapping("/cliente/{codigoAcesso}")
    public ResponseEntity<List<Compra>> listarComprasDoCliente(@PathVariable String codigoAcesso) {
        List<Compra> compras = compraService.listarComprasPorCliente(codigoAcesso);
        return ResponseEntity.ok(compras);
    }

    @GetMapping("/cliente/{codigoAcesso}/historico")
    public ResponseEntity<List<OperacaoDTO>> getHistoricoComprasCliente(
            @PathVariable String codigoAcesso,
            @RequestParam(required = false) TipoAtivo tipoAtivo,
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(required = false) String status) {
        List<OperacaoDTO> historico = historicoService.getHistoricoCliente(codigoAcesso, tipoAtivo, dataInicio, dataFim, status);
        List<OperacaoDTO> compras = historico.stream().filter(o -> o.getTipoOperacao().equals("COMPRA")).collect(Collectors.toList());
        return ResponseEntity.ok(compras);
    }

    @PutMapping("/confirmar-disponibilidade")
    public ResponseEntity<Compra> confirmarDisponibilidadeCompra(@RequestBody ConfirmacaoCompraDTO confirmacaoDTO) {
        Compra compra = compraService.confirmarDisponibilidadeCompra(confirmacaoDTO);
        return ResponseEntity.ok(compra);
    }

    @PutMapping("/confirmar-execucao")
    public ResponseEntity<Compra> confirmarExecucaoCompra(@RequestBody ExecucaoCompraDTO execucaoDTO) {
        Compra compra = compraService.confirmarExecucaoCompra(execucaoDTO);
        return ResponseEntity.ok(compra);
    }
}

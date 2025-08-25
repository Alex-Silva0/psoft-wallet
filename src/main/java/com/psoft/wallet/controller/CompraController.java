package com.psoft.wallet.controller;

import com.psoft.wallet.dto.CompraRequestDTO;
import com.psoft.wallet.dto.ConfirmacaoCompraDTO;
import com.psoft.wallet.dto.ExecucaoCompraDTO;
import com.psoft.wallet.model.Compra;
import com.psoft.wallet.service.CompraService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/compras")
public class CompraController {

    private final CompraService compraService;

    public CompraController(CompraService compraService) {
        this.compraService = compraService;
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

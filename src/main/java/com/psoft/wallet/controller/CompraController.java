package com.psoft.wallet.controller;

import com.psoft.wallet.dto.CompraRequestDTO;
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
}
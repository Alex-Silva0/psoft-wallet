package com.psoft.wallet.controller;

import com.psoft.wallet.dto.OperacaoDTO;
import com.psoft.wallet.enums.TipoAtivo;
import com.psoft.wallet.service.HistoricoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/operacoes")
public class AdminController {

    private final HistoricoService historicoService;

    public AdminController(HistoricoService historicoService) {
        this.historicoService = historicoService;
    }

    @GetMapping("/historico")
    public ResponseEntity<List<OperacaoDTO>> getHistoricoAdmin(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) TipoAtivo tipoAtivo,
            @RequestParam(required = false) LocalDate data,
            @RequestParam(required = false) String tipoOperacao) { // "COMPRA" ou "RESGATE"
        List<OperacaoDTO> historico = historicoService.getHistoricoAdmin(clienteId, tipoAtivo, data, tipoOperacao);
        return ResponseEntity.ok(historico);
    }
}
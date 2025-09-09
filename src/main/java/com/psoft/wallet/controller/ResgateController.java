package com.psoft.wallet.controller;

import com.psoft.wallet.dto.OperacaoDTO;
import com.psoft.wallet.dto.ResgateRequestDTO;
import com.psoft.wallet.dto.ResgateResponseDTO;
import com.psoft.wallet.enums.TipoAtivo;
import com.psoft.wallet.service.ResgateService;
import com.psoft.wallet.service.HistoricoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/resgates")
public class ResgateController {

    private final ResgateService resgateService;
    private final HistoricoService historicoService;

    public ResgateController(ResgateService resgateService, HistoricoService historicoService) {
        this.resgateService = resgateService;
        this.historicoService = historicoService;
    }

    @PostMapping
    public ResponseEntity<ResgateResponseDTO> solicitarResgate(@Valid @RequestBody ResgateRequestDTO request) {
        ResgateResponseDTO response = resgateService.solicitarResgate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/cliente/{codigoAcesso}")
    public ResponseEntity<List<ResgateResponseDTO>> visualizarResgates(@PathVariable String codigoAcesso) {
        List<ResgateResponseDTO> resgates = resgateService.visualizarResgates(codigoAcesso);
        return ResponseEntity.ok(resgates);
    }

    @GetMapping("/cliente/{codigoAcesso}/historico")
    public ResponseEntity<List<OperacaoDTO>> getHistoricoResgatesCliente(
            @PathVariable String codigoAcesso,
            @RequestParam(required = false) TipoAtivo tipoAtivo,
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(required = false) String status) {
        List<OperacaoDTO> historico = historicoService.getHistoricoCliente(codigoAcesso, tipoAtivo, dataInicio, dataFim, status);
        List<OperacaoDTO> resgates = historico.stream().filter(o -> o.getTipoOperacao().equals("RESGATE")).collect(Collectors.toList());
        return ResponseEntity.ok(resgates);
    }

    @PutMapping("/{id}/confirmar")
    public ResponseEntity<ResgateResponseDTO> confirmarResgate(@PathVariable Long id) {
        ResgateResponseDTO response = resgateService.confirmarResgate(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/finalizar")
    public ResponseEntity<ResgateResponseDTO> finalizarResgate(@PathVariable Long id) {
        ResgateResponseDTO response = resgateService.finalizarResgate(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/solicitados")
    public ResponseEntity<List<ResgateResponseDTO>> listarResgatesSolicitados() {
        List<ResgateResponseDTO> resgates = resgateService.listarResgatesSolicitados();
        return ResponseEntity.ok(resgates);
    }
}

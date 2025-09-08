package com.psoft.wallet.controller;

import com.psoft.wallet.dto.ResgateRequestDTO;
import com.psoft.wallet.dto.ResgateResponseDTO;
import com.psoft.wallet.service.ResgateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/resgates")
public class ResgateController {

    private final ResgateService resgateService;

    public ResgateController(ResgateService resgateService) {
        this.resgateService = resgateService;
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

package com.psoft.wallet.controller;

import com.psoft.wallet.dto.AtivoRequestDTO;
import com.psoft.wallet.dto.AtivoResponseDTO;
import com.psoft.wallet.model.Ativo;
import com.psoft.wallet.service.AtivoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ativos")
public class AtivoController {
    private final AtivoService ativoService;

    public AtivoController(AtivoService ativoService) {
        this.ativoService = ativoService;
    }

    @PostMapping
    public ResponseEntity<AtivoResponseDTO> criarAtivo(@Valid @RequestBody AtivoRequestDTO ativoRequest) {
        Ativo novoAtivo = ativoService.criarAtivo(ativoRequest);
        AtivoResponseDTO response = convertToResponseDTO(novoAtivo);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/valor")
    public ResponseEntity<AtivoResponseDTO> atualizarValor(@PathVariable Long id, @RequestParam BigDecimal novoValor) {
        Ativo ativoAtualizado = ativoService.atualizarValor(id, novoValor);
        AtivoResponseDTO response = convertToResponseDTO(ativoAtualizado);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removerAtivo(@PathVariable Long id) {
        ativoService.removerAtivo(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AtivoResponseDTO> ativarDesativarAtivo(@PathVariable Long id, @RequestParam boolean disponivel) {
        Ativo ativoAtualizado = ativoService.ativarDesativarAtivo(id, disponivel);
        AtivoResponseDTO response = convertToResponseDTO(ativoAtualizado);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<AtivoResponseDTO>> listarTodosAtivos() {
        List<AtivoResponseDTO> response = ativoService.listarTodosAtivos().stream()
            .map(this::convertToResponseDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/disponiveis")
    public ResponseEntity<List<AtivoResponseDTO>> listarAtivosDisponiveis() {
        List<AtivoResponseDTO> response = ativoService.listarAtivosDisponiveis().stream()
            .map(this::convertToResponseDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/indisponiveis")
    public ResponseEntity<List<AtivoResponseDTO>> listarAtivosIndisponiveis() {
        List<AtivoResponseDTO> response = ativoService.listarAtivosIndisponiveis().stream()
            .map(this::convertToResponseDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AtivoResponseDTO> buscarAtivoPorId(@PathVariable Long id) {
        Ativo ativo = ativoService.buscarAtivoPorId(id);
        AtivoResponseDTO response = convertToResponseDTO(ativo);
        return ResponseEntity.ok(response);
    }

    private AtivoResponseDTO convertToResponseDTO(Ativo ativo) {
        return new AtivoResponseDTO(
            ativo.getId(),
            ativo.getNome(),
            ativo.getTipo(),
            ativo.getDescricao(),
            ativo.isDisponivel(),
            ativo.getValorAtual()
        );
    }
}
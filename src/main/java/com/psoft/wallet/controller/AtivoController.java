package com.psoft.wallet.controller;

import com.psoft.wallet.model.Ativo;
import com.psoft.wallet.service.AtivoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/ativos")
public class AtivoController {
    private final AtivoService ativoService;

    public AtivoController(AtivoService ativoService) {
        this.ativoService = ativoService;
    }

    @PostMapping
    public ResponseEntity<Ativo> criarAtivo(@Valid @RequestBody Ativo ativo) {
        Ativo novoAtivo = ativoService.criarAtivo(ativo);
        return new ResponseEntity<>(novoAtivo, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/valor")
    public ResponseEntity<Ativo> atualizarValor(@PathVariable Long id, @RequestParam BigDecimal novoValor) {
        Ativo ativoAtualizado = ativoService.atualizarValor(id, novoValor);
        return ResponseEntity.ok(ativoAtualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removerAtivo(@PathVariable Long id) {
        ativoService.removerAtivo(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Ativo> ativarDesativarAtivo(@PathVariable Long id, @RequestParam boolean disponivel) {
        Ativo ativoAtualizado = ativoService.ativarDesativarAtivo(id, disponivel);
        return ResponseEntity.ok(ativoAtualizado);
    }

    @GetMapping
    public ResponseEntity<List<Ativo>> listarTodosAtivos() {
        return ResponseEntity.ok(ativoService.listarTodosAtivos());
    }

    @GetMapping("/disponiveis")
    public ResponseEntity<List<Ativo>> listarAtivosDisponiveis() {
        return ResponseEntity.ok(ativoService.listarAtivosDisponiveis());
    }

    @GetMapping("/indisponiveis")
    public ResponseEntity<List<Ativo>> listarAtivosIndisponiveis() {
        return ResponseEntity.ok(ativoService.listarAtivosIndisponiveis());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ativo> buscarAtivoPorId(@PathVariable Long id) {
        Ativo ativo = ativoService.buscarAtivoPorId(id);
        return ResponseEntity.ok(ativo);
    }
}
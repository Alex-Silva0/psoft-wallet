package com.psoft.wallet.controller;

import org.springframework.web.bind.annotation.*;
import com.psoft.wallet.model.Ativo;
import com.psoft.wallet.service.AtivoService;

@RestController
@RequestMapping("/ativos")
public class AtivoController {
    private final AtivoService service;

    public AtivoController(AtivoService service) {
        this.service = service;
    }

    @PostMapping
    public Ativo criarAtivo(@RequestBody Ativo ativo) {
        return service.criarAtivo(ativo);
    }

    @PatchMapping("/{id}/valor")
    public Ativo atualizarValor(@PathVariable Long id, @RequestParam float novoValor) {
        return service.atualizarValor(id, novoValor);
    }

    @DeleteMapping("/{id}")
    public void removerAtivo(@PathVariable Long id) {
        service.removerAtivo(id);
    }
}
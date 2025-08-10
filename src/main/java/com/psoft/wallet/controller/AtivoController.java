package com.psoft.wallet.controller;

import com.psoft.wallet.model.Ativo;
import com.psoft.wallet.service.AtivoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ativos")
public class AtivoController {

    private final AtivoService ativoService;

    public AtivoController(AtivoService ativoService) {
        this.ativoService = ativoService;
    }

    @PostMapping
    public Ativo criarAtivo(@RequestBody Ativo ativo) {
        return ativoService.criarAtivo(ativo);
    }

    @GetMapping
    public List<Ativo> listarTodosAtivos() {
        return ativoService.listarTodosAtivos();
    }

    @GetMapping("/disponiveis")
    public List<Ativo> listarAtivosDisponiveis() {
        return ativoService.listarAtivosDisponiveis();
    }

    @GetMapping("/indisponiveis")
    public List<Ativo> listarAtivosIndisponiveis() {
        return ativoService.listarAtivosIndisponiveis();
    }

    @DeleteMapping("/{id}")
    public void removerAtivo(@PathVariable Long id) {
        ativoService.removerAtivo(id);
    }

    @PatchMapping("/{id}/valor")
    public Ativo atualizarValorAtivo(@PathVariable Long id, @RequestParam float novoValor) {
        return ativoService.atualizarPrecoAtivo(id, novoValor);
    }

    @PatchMapping("/{id}/status")
    public Ativo atualizarStatusAtivo(@PathVariable Long id, @RequestParam boolean ativo) {
        return ativoService.atualizarStatusAtivo(id, ativo);
    }
}
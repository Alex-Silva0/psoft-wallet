package com.psoft.wallet.controller;

import org.springframework.web.bind.annotation.*;
import com.psoft.wallet.model.Ativo;
import com.psoft.wallet.model.TipoAtivo;
import com.psoft.wallet.service.AtivoService;
import com.psoft.wallet.service.ClienteService;
import com.psoft.wallet.model.Cliente;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/cliente/ativos")
public class AtivoClienteController {
    private final AtivoService ativoService;
    private final ClienteService clienteService;

    public AtivoClienteController(AtivoService ativoService, ClienteService clienteService) {
        this.ativoService = ativoService;
        this.clienteService = clienteService;
    }

    @GetMapping("/disponiveis")
    public List<Ativo> listarAtivosDisponiveisParaPlano(@RequestParam String codigoAcesso) {
        // Validar código de acesso e obter cliente
        List<Cliente> clientes = clienteService.listarAtivosPorPlano(codigoAcesso);
        if (clientes.isEmpty()) {
            throw new RuntimeException("Cliente não encontrado");
        }
        
        Cliente cliente = clientes.get(0);
        
        // Obter todos os ativos disponíveis
        List<Ativo> todosAtivos = ativoService.listarAtivosDisponiveis();
        
        // Filtrar por plano
        if (cliente.getPlano() == com.psoft.wallet.model.TipoPlano.NORMAL) {
            // Clientes Normal veem apenas Tesouro Direto
            return todosAtivos.stream()
                .filter(ativo -> ativo.getTipo() == TipoAtivo.TESOURO_DIRETO)
                .collect(Collectors.toList());
        } else {
            // Clientes Premium veem todos os tipos
            return todosAtivos;
        }
    }
} 
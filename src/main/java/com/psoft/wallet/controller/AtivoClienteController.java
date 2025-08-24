package com.psoft.wallet.controller;

import com.psoft.wallet.enums.TipoPlano;
import org.springframework.web.bind.annotation.*;
import com.psoft.wallet.model.Ativo;
import com.psoft.wallet.enums.TipoAtivo;
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
        List<Cliente> clientes = clienteService.listarAtivosPorPlano(codigoAcesso);
        if (clientes.isEmpty()) {
            throw new RuntimeException("Cliente não encontrado");
        }
        
        Cliente cliente = clientes.get(0);

        List<Ativo> todosAtivos = ativoService.listarAtivosDisponiveis();

        if (cliente.getPlano() == TipoPlano.NORMAL) {
            return todosAtivos.stream()
                .filter(ativo -> ativo.getTipo() == TipoAtivo.TESOURO_DIRETO)
                .collect(Collectors.toList());
        } else {
            return todosAtivos;
        }
    }
} 
package com.psoft.wallet.controller;

import org.springframework.web.bind.annotation.*;
import com.psoft.wallet.model.Cliente;
import com.psoft.wallet.service.ClienteService;

import java.util.List;

@RestController
@RequestMapping("/clientes")
public class ClienteController {
    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @PostMapping
    public Cliente criarCliente(@RequestBody Cliente cliente) {
        Cliente clienteSalvo = service.criarCliente(cliente);
        // Não retornar código de acesso
        clienteSalvo.setCodigoAcesso(null);
        return clienteSalvo;
    }

    @GetMapping
    public List<Cliente> listarTodosClientes() {
        return service.listarTodosClientes();
    }

    @GetMapping("/{id}")
    public Cliente buscarClientePorId(@PathVariable Long id) {
        return service.buscarClientePorId(id);
    }

    @PutMapping("/{id}")
    public Cliente editarCliente(@PathVariable Long id, 
                                @RequestBody Cliente cliente,
                                @RequestParam String codigoAcesso) {
        return service.editarCliente(id, cliente, codigoAcesso);
    }

    @DeleteMapping("/{id}")
    public void removerCliente(@PathVariable Long id, 
                              @RequestParam String codigoAcesso) {
        service.removerCliente(id, codigoAcesso);
    }

    @GetMapping("/validar-acesso")
    public List<Cliente> validarAcesso(@RequestParam String codigoAcesso) {
        return service.listarAtivosPorPlano(codigoAcesso);
    }
} 
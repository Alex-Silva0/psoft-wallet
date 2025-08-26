package com.psoft.wallet.controller;

import com.psoft.wallet.dto.ClienteRequestDTO;
import com.psoft.wallet.dto.ClienteResponseDTO;
import org.springframework.web.bind.annotation.*;
import com.psoft.wallet.model.Cliente;
import com.psoft.wallet.service.ClienteService;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/clientes")
public class ClienteController {
    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @PostMapping
    public ClienteResponseDTO criarCliente(@Valid @RequestBody ClienteRequestDTO clienteRequest) {
        Cliente cliente = new Cliente();
        cliente.setNomeCompleto(clienteRequest.getNomeCompleto());
        cliente.setEnderecoPrincipal(clienteRequest.getEnderecoPrincipal());
        cliente.setPlano(clienteRequest.getPlano());
        cliente.setCodigoAcesso(clienteRequest.getCodigoAcesso());
        
        Cliente clienteSalvo = service.criarCliente(cliente);
        return convertToResponseDTO(clienteSalvo);
    }

    @GetMapping
    public List<ClienteResponseDTO> listarTodosClientes() {
        return service.listarTodosClientes().stream()
            .map(this::convertToResponseDTO)
            .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ClienteResponseDTO buscarClientePorId(@PathVariable Long id) {
        Cliente cliente = service.buscarClientePorId(id);
        return convertToResponseDTO(cliente);
    }

    @PutMapping("/{id}")
    public ClienteResponseDTO editarCliente(@PathVariable Long id, 
                                @Valid @RequestBody ClienteRequestDTO clienteRequest,
                                @RequestParam String codigoAcesso) {
        Cliente cliente = new Cliente();
        cliente.setNomeCompleto(clienteRequest.getNomeCompleto());
        cliente.setEnderecoPrincipal(clienteRequest.getEnderecoPrincipal());
        cliente.setPlano(clienteRequest.getPlano());
        if (clienteRequest.getCodigoAcesso() != null) {
            cliente.setCodigoAcesso(clienteRequest.getCodigoAcesso());
        }
        
        Cliente clienteSalvo = service.editarCliente(id, cliente, codigoAcesso);
        return convertToResponseDTO(clienteSalvo);
    }

    @DeleteMapping("/{id}")
    public void removerCliente(@PathVariable Long id, 
                              @RequestParam String codigoAcesso) {
        service.removerCliente(id, codigoAcesso);
    }

    private ClienteResponseDTO convertToResponseDTO(Cliente cliente) {
        return new ClienteResponseDTO(
            cliente.getId(),
            cliente.getNomeCompleto(),
            cliente.getEnderecoPrincipal(),
            cliente.getPlano()
        );
    }
}

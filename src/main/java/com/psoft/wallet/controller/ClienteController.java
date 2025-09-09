package com.psoft.wallet.controller;

import com.psoft.wallet.dto.ClienteRequestDTO;
import com.psoft.wallet.dto.ClienteResponseDTO;
import com.psoft.wallet.dto.OperacaoDTO;
import com.psoft.wallet.enums.TipoAtivo;
import org.springframework.web.bind.annotation.*;
import com.psoft.wallet.model.Cliente;
import com.psoft.wallet.service.ClienteService;
import com.psoft.wallet.service.ExtratoService;
import com.psoft.wallet.service.HistoricoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/clientes")
public class ClienteController {
    private final ClienteService service;
    private final HistoricoService historicoService;
    private final ExtratoService extratoService;

    public ClienteController(ClienteService service, HistoricoService historicoService, ExtratoService extratoService) {
        this.service = service;
        this.historicoService = historicoService;
        this.extratoService = extratoService;
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

    @GetMapping("/{codigoAcesso}/historico-completo")
    public ResponseEntity<List<OperacaoDTO>> getHistoricoCompletoCliente(
            @PathVariable String codigoAcesso,
            @RequestParam(required = false) TipoAtivo tipoAtivo,
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(required = false) String status) {
        List<OperacaoDTO> historico = historicoService.getHistoricoCliente(codigoAcesso, tipoAtivo, dataInicio, dataFim, status);
        return ResponseEntity.ok(historico);
    }

    @GetMapping("/{codigoAcesso}/extrato-csv")
    public ResponseEntity<String> exportarExtratoCSV(@PathVariable String codigoAcesso) throws IOException {
        String csvData = extratoService.gerarExtratoCSV(codigoAcesso);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=extrato.csv");
        headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8");

        return ResponseEntity.ok().headers(headers).body(csvData);
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

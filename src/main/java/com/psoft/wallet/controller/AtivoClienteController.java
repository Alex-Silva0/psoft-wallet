package com.psoft.wallet.controller;

import com.psoft.wallet.dto.AtivoResponseDTO;
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
    public List<AtivoResponseDTO> listarAtivosDisponiveisParaPlano(@RequestParam String codigoAcesso) {
        Cliente cliente = clienteService.buscarClientePorCodigoAcesso(codigoAcesso);

        List<Ativo> todosAtivos = ativoService.listarAtivosDisponiveis();

        List<Ativo> ativosFiltrados;
        if (cliente.getPlano() == TipoPlano.NORMAL) {
            ativosFiltrados = todosAtivos.stream()
                .filter(ativo -> ativo.getTipo() == TipoAtivo.TESOURO_DIRETO)
                .collect(Collectors.toList());
        } else {
            ativosFiltrados = todosAtivos;
        }

        return ativosFiltrados.stream()
            .map(this::convertToResponseDTO)
            .collect(Collectors.toList());
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

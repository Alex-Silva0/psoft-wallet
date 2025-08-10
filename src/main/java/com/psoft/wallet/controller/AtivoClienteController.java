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
        // 1. Valida o código de acesso e obtém o cliente.
        //    O método no service deve lançar uma exceção apropriada (que resulta em 401/404)
        //    se o código for inválido, o que está alinhado com os testes.
        //    O nome do método foi alterado para maior clareza.
        Cliente cliente = clienteService.validarAcesso(codigoAcesso);

        // 2. Obter todos os ativos disponíveis do sistema.
        List<Ativo> todosAtivos = ativoService.listarAtivosDisponiveis();

        // 3. Filtra a lista de ativos com base no plano do cliente (US05).
        if (cliente.getPlano() == com.psoft.wallet.model.TipoPlano.NORMAL) {
            // Clientes do plano Normal visualizam apenas Tesouro Direto.
            return todosAtivos.stream()
                .filter(ativo -> ativo.getTipo() == TipoAtivo.TESOURO_DIRETO)
                .collect(Collectors.toList());
        } else {
            // Clientes do plano Premium visualizam todos os tipos de ativos.
            return todosAtivos;
        }
    }
} 
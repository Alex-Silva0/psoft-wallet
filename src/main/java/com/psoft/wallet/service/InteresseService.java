package com.psoft.wallet.service;

import com.psoft.wallet.model.*;
import com.psoft.wallet.repository.AtivoRepository;
import com.psoft.wallet.repository.ClienteRepository;
import com.psoft.wallet.repository.InteresseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InteresseService {

    private final InteresseRepository interesseRepository;
    private final ClienteRepository clienteRepository;
    private final AtivoRepository ativoRepository;
    private final ClienteService clienteService;

    public InteresseService(InteresseRepository interesseRepository,
                            ClienteRepository clienteRepository,
                            AtivoRepository ativoRepository,
                            ClienteService clienteService) {
        this.interesseRepository = interesseRepository;
        this.clienteRepository = clienteRepository;
        this.ativoRepository = ativoRepository;
        this.clienteService = clienteService;
    }

    /**
     * US06 & US07: Permite que um cliente marque interesse em um ativo.
     * - Se o ativo estiver disponível, cria um interesse em VARIAÇÃO DE PREÇO (US06).
     * - Se o ativo estiver indisponível, cria um interesse em DISPONIBILIDADE (US07).
     */
    @Transactional
    public Interesse marcarInteresse(Long ativoId, String codigoAcesso) {
        // Valida o cliente
        Cliente cliente = clienteService.validarAcesso(codigoAcesso);
        
        // Valida o ativo
        Ativo ativo = ativoRepository.findById(ativoId)
                .orElseThrow(() -> new AtivoNaoEncontradoException("Ativo com ID " + ativoId + " não encontrado"));

        if (ativo.isDisponivel()) {
            // Lógica para US06: Interesse em variação de preço
            if (cliente.getPlano() != TipoPlano.PREMIUM) {
                throw new OperacaoNaoAutorizadaException("Funcionalidade disponível apenas para clientes Premium.");
            }
            if (ativo.getTipo() == TipoAtivo.TESOURO_DIRETO) {
                throw new RegraDeNegocioException("Interesse por variação de preço só pode ser marcado para Ações ou Criptomoedas.");
            }
            interesseRepository.findByClienteAndAtivo(cliente, ativo).ifPresent(i -> {
                throw new RecursoDuplicadoException("Cliente já possui interesse neste ativo.");
            });
            return interesseRepository.save(Interesse.builder()
                    .cliente(cliente)
                    .ativo(ativo)
                    .precoNoRegistro(ativo.getValorAtual())
                    .build());
        } else {
            // Lógica para US07: Interesse em disponibilidade
            interesseRepository.findByClienteAndAtivo(cliente, ativo).ifPresent(i -> {
                throw new RecursoDuplicadoException("Cliente já possui interesse neste ativo.");
            });
            return interesseRepository.save(Interesse.builder()
                    .cliente(cliente)
                    .ativo(ativo)
                    .precoNoRegistro(null) // Nulo indica interesse em disponibilidade
                    .build());
        }
    }
}
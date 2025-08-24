package com.psoft.wallet.service;

import com.psoft.wallet.dto.InteresseDTO;
import com.psoft.wallet.enums.TipoInteresse;
import com.psoft.wallet.enums.TipoPlano;
import com.psoft.wallet.exception.AtivoNaoEncontradoException;
import com.psoft.wallet.exception.ClienteNaoEncontradoException;
import com.psoft.wallet.exception.OperacaoNaoAutorizadaException;
import com.psoft.wallet.exception.RegraDeNegocioException;
import com.psoft.wallet.model.Ativo;
import com.psoft.wallet.model.Cliente;
import com.psoft.wallet.model.Interesse;
import com.psoft.wallet.repository.AtivoRepository;
import com.psoft.wallet.repository.ClienteRepository;
import com.psoft.wallet.repository.InteresseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InteresseService {

    private final ClienteRepository clienteRepository;
    private final AtivoRepository ativoRepository;
    private final InteresseRepository interesseRepository;

    public InteresseService(ClienteRepository clienteRepository, AtivoRepository ativoRepository, InteresseRepository interesseRepository) {
        this.clienteRepository = clienteRepository;
        this.ativoRepository = ativoRepository;
        this.interesseRepository = interesseRepository;
    }

    @Transactional
    public Interesse registrarInteresse(InteresseDTO interesseDTO) {
        Cliente cliente = clienteRepository.findByCodigoAcesso(interesseDTO.getCodigoAcessoCliente())
                .orElseThrow(() -> new ClienteNaoEncontradoException("Cliente não encontrado."));

        Ativo ativo = ativoRepository.findById(interesseDTO.getAtivoId())
                .orElseThrow(() -> new AtivoNaoEncontradoException("Ativo não encontrado."));

        // US06: Interesse em variação de preço é apenas para clientes Premium e ativos disponíveis
        if (interesseDTO.getTipoInteresse() == TipoInteresse.VARIACAO_PRECO) {
            if (cliente.getPlano() != TipoPlano.PREMIUM) {
                throw new OperacaoNaoAutorizadaException("Apenas clientes Premium podem registrar interesse na variação de preço.");
            }
            if (!ativo.isDisponivel()) {
                throw new RegraDeNegocioException("Não é possível registrar interesse na variação de preço de um ativo indisponível.");
            }
        }

        // US07: Interesse em disponibilidade é apenas para ativos indisponíveis
        if (interesseDTO.getTipoInteresse() == TipoInteresse.DISPONIBILIDADE && ativo.isDisponivel()) {
            throw new RegraDeNegocioException("Só é possível registrar interesse em disponibilidade para ativos que estão indisponíveis.");
        }

        // Evitar duplicados
        interesseRepository.findByClienteAndAtivoAndTipo(cliente, ativo, interesseDTO.getTipoInteresse())
                .ifPresent(i -> {
                    throw new RegraDeNegocioException("Interesse já registrado para este cliente e ativo.");
                });

        Interesse novoInteresse = new Interesse(null, cliente, ativo, interesseDTO.getTipoInteresse());
        return interesseRepository.save(novoInteresse);
    }

    @Transactional
    public void removerInteresse(InteresseDTO interesseDTO) {
        Cliente cliente = clienteRepository.findByCodigoAcesso(interesseDTO.getCodigoAcessoCliente())
                .orElseThrow(() -> new ClienteNaoEncontradoException("Cliente não encontrado."));

        Ativo ativo = ativoRepository.findById(interesseDTO.getAtivoId())
                .orElseThrow(() -> new AtivoNaoEncontradoException("Ativo não encontrado."));

        Interesse interesse = interesseRepository.findByClienteAndAtivoAndTipo(cliente, ativo, interesseDTO.getTipoInteresse())
                .orElseThrow(() -> new RegraDeNegocioException("Nenhum interesse encontrado para remover."));

        interesseRepository.delete(interesse);
    }
}
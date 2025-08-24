package com.psoft.wallet.service;

import com.psoft.wallet.enums.TipoInteresse;
import com.psoft.wallet.exception.AtivoNaoEncontradoException;
import com.psoft.wallet.exception.VariacaoInvalidaException;
import com.psoft.wallet.exception.RegraDeNegocioException;
import com.psoft.wallet.model.Ativo;
import com.psoft.wallet.model.Interesse;
import com.psoft.wallet.repository.AtivoRepository;
import com.psoft.wallet.repository.InteresseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.math.BigDecimal;
import java.util.List;

@Service
public class AtivoService {

    private final AtivoRepository ativoRepository;
    private final InteresseRepository interesseRepository;
    private final NotificacaoService notificacaoService;

    public AtivoService(AtivoRepository ativoRepository, InteresseRepository interesseRepository, NotificacaoService notificacaoService) {
        this.ativoRepository = ativoRepository;
        this.interesseRepository = interesseRepository;
        this.notificacaoService = notificacaoService;
    }

    @Transactional
    public Ativo criarAtivo(Ativo ativo) {
        ativoRepository.findByNome(ativo.getNome()).ifPresent(a -> {
            throw new RegraDeNegocioException("Já existe um ativo com o nome: " + ativo.getNome());
        });
        return ativoRepository.save(ativo);
    }

    @Transactional
    public Ativo atualizarValor(Long id, BigDecimal novoValor) {
        Ativo ativo = ativoRepository.findById(id)
                .orElseThrow(() -> new AtivoNaoEncontradoException("Ativo com ID " + id + " não encontrado."));

        BigDecimal valorAntigo = ativo.getValorAtual();
        BigDecimal valorAtual = ativo.getValorAtual();

        if (novoValor.compareTo(valorAtual) == 0) {
            throw new VariacaoInvalidaException("Variação mínima de 1% não atingida");
        }

        BigDecimal percentualVariacao = BigDecimal.ZERO;
        if (valorAtual.compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal variacaoAbsoluta = novoValor.subtract(valorAtual).abs();
            percentualVariacao = variacaoAbsoluta.divide(valorAtual, 4, RoundingMode.HALF_UP);

            if (percentualVariacao.compareTo(new BigDecimal("0.01")) < 0) {
                throw new VariacaoInvalidaException("Variação mínima de 1% não atingida");
            }
        }

        ativo.setValorAtual(novoValor);
        Ativo ativoSalvo = ativoRepository.save(ativo);

        verificarEnotificarVariacaoPreco(ativoSalvo, valorAntigo);

        return ativoSalvo;
    }

    @Transactional
    public void removerAtivo(Long id) {
        if (!ativoRepository.existsById(id)) {
            throw new AtivoNaoEncontradoException("Ativo com ID " + id + " não encontrado para remoção.");
        }
        ativoRepository.deleteById(id);
    }

    @Transactional
    public Ativo ativarDesativarAtivo(Long id, boolean disponivel) {
        Ativo ativo = ativoRepository.findById(id)
                .orElseThrow(() -> new AtivoNaoEncontradoException("Ativo com ID " + id + " não encontrado."));

        boolean eraIndisponivel = !ativo.isDisponivel();
        ativo.setDisponivel(disponivel);
        Ativo ativoSalvo = ativoRepository.save(ativo);

        if (eraIndisponivel && ativoSalvo.isDisponivel()) {
            notificarEremoverInteresseDisponibilidade(ativoSalvo);
        }
        return ativoSalvo;
    }

    public List<Ativo> listarTodosAtivos() {
        return ativoRepository.findAll();
    }

    public List<Ativo> listarAtivosDisponiveis() {
        return ativoRepository.findByDisponivel(true);
    }

    public List<Ativo> listarAtivosIndisponiveis() {
        return ativoRepository.findByDisponivel(false);
    }

    public Ativo buscarAtivoPorId(Long id) {
        return ativoRepository.findById(id)
                .orElseThrow(() -> new AtivoNaoEncontradoException("Ativo com ID " + id + " não encontrado"));
    }

    private void verificarEnotificarVariacaoPreco(Ativo ativo, BigDecimal valorAntigo) {
        if (valorAntigo.compareTo(BigDecimal.ZERO) == 0) return;

        BigDecimal variacao = ativo.getValorAtual().subtract(valorAntigo);
        BigDecimal percentualVariacao = variacao.divide(valorAntigo, 4, RoundingMode.HALF_UP);

        if (percentualVariacao.abs().compareTo(new BigDecimal("0.10")) >= 0) {
            List<Interesse> interesses = interesseRepository.findByAtivoAndTipo(ativo, TipoInteresse.VARIACAO_PRECO);
            BigDecimal finalPercentualVariacao = percentualVariacao;
            interesses.forEach(interesse -> {
                notificacaoService.notificarVariacaoPreco(interesse.getCliente(), ativo, finalPercentualVariacao);
            });
        }
    }

    @Transactional
    public void notificarEremoverInteresseDisponibilidade(Ativo ativo) {
        List<Interesse> interesses = interesseRepository.findByAtivoAndTipo(ativo, TipoInteresse.DISPONIBILIDADE);

        interesses.forEach(interesse -> {
            notificacaoService.notificarDisponibilidade(interesse.getCliente(), ativo);
        });

        interesseRepository.deleteAll(interesses);
    }
}
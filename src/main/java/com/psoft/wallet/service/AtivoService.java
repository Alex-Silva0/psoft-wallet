package com.psoft.wallet.service;

import org.springframework.stereotype.Service;
import com.psoft.wallet.model.Ativo;
import com.psoft.wallet.repository.AtivoRepository;

import java.util.List;

@Service
public class AtivoService {
    private final AtivoRepository repository;

    public AtivoService(AtivoRepository repository) {
        this.repository = repository;
    }

    public Ativo criarAtivo(Ativo ativo) {
        // Verificar se já existe um ativo com o mesmo nome
        if (repository.findByNome(ativo.getNome()).isPresent()) {
            throw new AtivoNomeDuplicadoException("Já existe um ativo com o nome '" + ativo.getNome() + "'");
        }
        return repository.save(ativo);
    }

    public Ativo atualizarValor(Long id, float novoValor) {
        Ativo ativo = repository.findById(id)
            .orElseThrow(() -> new AtivoNaoEncontradoException("Ativo com ID " + id + " não encontrado"));

        float variacao = Math.abs((novoValor - ativo.getValorAtual()) / ativo.getValorAtual());
        if (variacao < 0.01f) {
            throw new VariacaoInvalidaException("Variação mínima de 1% não atingida");
        }

        ativo.setValorAtual(novoValor);
        return repository.save(ativo);
    }

    public void removerAtivo(Long id) {
        if (!repository.existsById(id)) {
            throw new AtivoNaoEncontradoException("Ativo com ID " + id + " não encontrado");
        }
        repository.deleteById(id);
    }

    public Ativo ativarDesativarAtivo(Long id, boolean ativo) {
        Ativo ativoEncontrado = repository.findById(id)
            .orElseThrow(() -> new AtivoNaoEncontradoException("Ativo com ID " + id + " não encontrado"));
        
        ativoEncontrado.setDisponivel(ativo);
        return repository.save(ativoEncontrado);
    }

    public List<Ativo> listarAtivosDisponiveis() {
        return repository.findByDisponivelTrue();
    }

    public List<Ativo> listarAtivosIndisponiveis() {
        return repository.findByDisponivelFalse();
    }

    public List<Ativo> listarTodosAtivos() {
        return repository.findAll();
    }
}
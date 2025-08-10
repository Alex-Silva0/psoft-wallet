package com.psoft.wallet.service;

import com.psoft.wallet.model.Ativo;
import com.psoft.wallet.model.Interesse;
import com.psoft.wallet.model.TipoAtivo;
import com.psoft.wallet.repository.AtivoRepository;
import com.psoft.wallet.repository.InteresseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AtivoService {

    private final AtivoRepository ativoRepository;
    private final InteresseRepository interesseRepository;
    private final NotificationService notificationService;

    public AtivoService(AtivoRepository ativoRepository, InteresseRepository interesseRepository, NotificationService notificationService) {
        this.ativoRepository = ativoRepository;
        this.interesseRepository = interesseRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public Ativo criarAtivo(Ativo ativo) {
        ativoRepository.findByNome(ativo.getNome()).ifPresent(a -> {
            throw new RecursoDuplicadoException("Já existe um ativo com o nome '" + ativo.getNome() + "'");
        });
        return ativoRepository.save(ativo);
    }

    public List<Ativo> listarTodosAtivos() {
        return ativoRepository.findAll();
    }

    public List<Ativo> listarAtivosIndisponiveis() {
        return ativoRepository.findAll().stream().filter(a -> !a.isDisponivel()).collect(Collectors.toList());
    }

    @Transactional
    public void removerAtivo(Long id) {
        if (!ativoRepository.existsById(id)) {
            throw new AtivoNaoEncontradoException("Ativo com ID " + id + " não encontrado");
        }
        ativoRepository.deleteById(id);
    }

    /**
     * Lista todos os ativos que estão marcados como disponíveis.
     * @return Uma lista de ativos disponíveis.
     */
    public List<Ativo> listarAtivosDisponiveis() {
        // A forma mais performática seria criar um método no Repository: `findByDisponivel(true)`
        return ativoRepository.findAll().stream().filter(Ativo::isDisponivel).collect(Collectors.toList());
    }

    /**
     * US06: Lógica de notificação ao atualizar o preço de um ativo.
     * Este método deve ser chamado sempre que o preço de um ativo for alterado no sistema.
     */
    @Transactional
    public Ativo atualizarPrecoAtivo(Long ativoId, float novoPreco) {
        Ativo ativo = ativoRepository.findById(ativoId)
                .orElseThrow(() -> new AtivoNaoEncontradoException("Ativo com ID " + ativoId + " não encontrado"));

        if (ativo.getTipo() == TipoAtivo.TESOURO_DIRETO) {
            throw new OperacaoNaoAutorizadaException("Não é permitido atualizar o valor de um ativo do tipo Tesouro Direto.");
        }
        if (ativo.getValorAtual() == 0.0f) {
            throw new RegraDeNegocioException("Não é possível calcular a variação de um ativo com valor atual zero.");
        }
        float variacao = Math.abs((novoPreco - ativo.getValorAtual()) / ativo.getValorAtual());
        if (variacao < 0.01f) {
            throw new RegraDeNegocioException("Variação mínima de 1% não atingida");
        }

        ativo.setValorAtual(novoPreco);
        Ativo ativoAtualizado = ativoRepository.save(ativo);

        notificarInteressadosVariacaoPreco(ativoAtualizado);

        return ativoAtualizado;
    }

    @Transactional
    public Ativo atualizarStatusAtivo(Long id, boolean disponivel) {
        Ativo ativo = ativoRepository.findById(id)
                .orElseThrow(() -> new AtivoNaoEncontradoException("Ativo com ID " + id + " não encontrado"));

        boolean eraDisponivel = ativo.isDisponivel();
        if (eraDisponivel == disponivel) {
            return ativo; // Sem mudança, sem ação
        }

        ativo.setDisponivel(disponivel);
        Ativo ativoAtualizado = ativoRepository.save(ativo);

        // Notificar apenas quando se torna disponível
        if (!eraDisponivel && disponivel) {
            notificarInteressadosDisponibilidade(ativoAtualizado);
        }

        return ativoAtualizado;
    }

    private void notificarInteressadosVariacaoPreco(Ativo ativo) {
        List<Interesse> interesses = interesseRepository.findAllByAtivoAndPrecoNoRegistroIsNotNull(ativo);

        for (Interesse interesse : interesses) {
            float precoNoRegistro = interesse.getPrecoNoRegistro();
            float precoAtual = ativo.getValorAtual();

            float variacaoPercentual = Math.abs((precoAtual - precoNoRegistro) / precoNoRegistro);

            if (variacaoPercentual >= 0.10) {
                notificationService.notificarVariacaoPreco(interesse.getCliente(), ativo, precoNoRegistro, precoAtual);
                // Atualiza o preço no registro para que a próxima notificação seja baseada no novo valor.
                interesse.setPrecoNoRegistro(precoAtual);
                interesseRepository.save(interesse);
            }
        }
    }

    private void notificarInteressadosDisponibilidade(Ativo ativo) {
        List<Interesse> interesses = interesseRepository.findAllByAtivoAndPrecoNoRegistroIsNull(ativo);

        for (Interesse interesse : interesses) {
            notificationService.notificarDisponibilidade(interesse.getCliente(), ativo);
            // Remove o interesse, pois a notificação deve ser enviada apenas uma vez.
            interesseRepository.delete(interesse);
        }
    }
}
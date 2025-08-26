package com.psoft.wallet.service;

import com.psoft.wallet.dto.CarteiraDTO;
import com.psoft.wallet.exception.ClienteNaoEncontradoException;
import com.psoft.wallet.model.Ativo;
import com.psoft.wallet.model.Carteira;
import com.psoft.wallet.model.Cliente;
import com.psoft.wallet.repository.CarteiraRepository;
import com.psoft.wallet.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CarteiraService {

    private final CarteiraRepository carteiraRepository;
    private final ClienteRepository clienteRepository;

    public CarteiraService(CarteiraRepository carteiraRepository, ClienteRepository clienteRepository) {
        this.carteiraRepository = carteiraRepository;
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public void adicionarAtivoACarteira(Cliente cliente, Ativo ativo, Integer quantidade, BigDecimal valorAquisicao) {
        Optional<Carteira> carteiraExistente = carteiraRepository.findByClienteAndAtivo(cliente, ativo);
        
        if (carteiraExistente.isPresent()) {
            // Atualiza quantidade existente
            Carteira carteira = carteiraExistente.get();
            BigDecimal valorTotalAtual = carteira.getValorAquisicao().multiply(new BigDecimal(carteira.getQuantidade()));
            BigDecimal valorTotalNovo = valorAquisicao.multiply(new BigDecimal(quantidade));
            BigDecimal quantidadeTotal = new BigDecimal(carteira.getQuantidade() + quantidade);
            
            // Calcula valor médio ponderado
            BigDecimal valorMedioAquisicao = valorTotalAtual.add(valorTotalNovo).divide(quantidadeTotal, 4, RoundingMode.HALF_UP);
            
            carteira.setQuantidade(carteira.getQuantidade() + quantidade);
            carteira.setValorAquisicao(valorMedioAquisicao);
            carteira.setValorAtual(ativo.getValorAtual());
            carteira.setDesempenho(ativo.getValorAtual().subtract(valorMedioAquisicao));
            carteira.setDataUltimaAtualizacao(LocalDateTime.now());
            
            carteiraRepository.save(carteira);
        } else {
            // Cria nova entrada na carteira
            Carteira novaCarteira = new Carteira();
            novaCarteira.setCliente(cliente);
            novaCarteira.setAtivo(ativo);
            novaCarteira.setQuantidade(quantidade);
            novaCarteira.setValorAquisicao(valorAquisicao);
            novaCarteira.setValorAtual(ativo.getValorAtual());
            novaCarteira.setDesempenho(ativo.getValorAtual().subtract(valorAquisicao));
            novaCarteira.setDataEntradaCarteira(LocalDateTime.now());
            novaCarteira.setDataUltimaAtualizacao(LocalDateTime.now());
            
            carteiraRepository.save(novaCarteira);
        }
    }

    public List<CarteiraDTO> visualizarCarteira(String codigoAcesso) {
        Cliente cliente = clienteRepository.findByCodigoAcesso(codigoAcesso)
                .orElseThrow(() -> new ClienteNaoEncontradoException("Cliente não encontrado."));

        List<Carteira> carteira = carteiraRepository.findAllByCliente(cliente);
        
        return carteira.stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    private CarteiraDTO converterParaDTO(Carteira carteira) {
        CarteiraDTO dto = new CarteiraDTO();
        Ativo ativo = carteira.getAtivo();
        dto.setAtivoId(ativo.getId());
        dto.setNomeAtivo(ativo.getNome());
        dto.setTipoAtivo(ativo.getTipo());
        dto.setQuantidade(carteira.getQuantidade());
        dto.setValorAquisicao(carteira.getValorAquisicao());
        dto.setValorAtual(carteira.getValorAtual());
        dto.setDesempenho(carteira.getDesempenho());
        dto.setDataEntradaCarteira(carteira.getDataEntradaCarteira());
        return dto;
    }

    @Transactional
    public void atualizarValoresCarteira() {
        List<Carteira> todasCarteiras = carteiraRepository.findAll();
        
        for (Carteira carteira : todasCarteiras) {
            Ativo ativo = carteira.getAtivo();
            carteira.setValorAtual(ativo.getValorAtual());
            carteira.setDesempenho(ativo.getValorAtual().subtract(carteira.getValorAquisicao()).multiply(new BigDecimal(carteira.getQuantidade())));
            carteira.setDataUltimaAtualizacao(LocalDateTime.now());
            carteiraRepository.save(carteira);
        }
    }
}

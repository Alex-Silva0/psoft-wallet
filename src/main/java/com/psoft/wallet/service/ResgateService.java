package com.psoft.wallet.service;

import com.psoft.wallet.dto.ResgateRequestDTO;
import com.psoft.wallet.dto.ResgateResponseDTO;
import com.psoft.wallet.enums.EstadoResgate;
import com.psoft.wallet.enums.TipoAtivo;
import com.psoft.wallet.exception.ClienteNaoEncontradoException;
import com.psoft.wallet.exception.CodigoAcessoIncorretoException;
import com.psoft.wallet.exception.RegraDeNegocioException;
import com.psoft.wallet.model.Ativo;
import com.psoft.wallet.model.Carteira;
import com.psoft.wallet.model.Cliente;
import com.psoft.wallet.model.Resgate;
import com.psoft.wallet.repository.AtivoRepository;
import com.psoft.wallet.repository.CarteiraRepository;
import com.psoft.wallet.repository.ClienteRepository;
import com.psoft.wallet.repository.ResgateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ResgateService {

    private final ResgateRepository resgateRepository;
    private final ClienteRepository clienteRepository;
    private final AtivoRepository ativoRepository;
    private final CarteiraRepository carteiraRepository;
    private final NotificationService notificationService;

    public ResgateService(ResgateRepository resgateRepository, 
                         ClienteRepository clienteRepository,
                         AtivoRepository ativoRepository,
                         CarteiraRepository carteiraRepository,
                         NotificationService notificationService) {
        this.resgateRepository = resgateRepository;
        this.clienteRepository = clienteRepository;
        this.ativoRepository = ativoRepository;
        this.carteiraRepository = carteiraRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public ResgateResponseDTO solicitarResgate(ResgateRequestDTO request) {
        // Validar cliente e código de acesso
        Cliente cliente = clienteRepository.findByCodigoAcesso(request.getCodigoAcesso())
                .orElseThrow(() -> new ClienteNaoEncontradoException("Cliente não encontrado."));
        
        if (!cliente.getCodigoAcesso().equals(request.getCodigoAcesso())) {
            throw new CodigoAcessoIncorretoException("Código de acesso incorreto.");
        }

        // Buscar ativo
        Ativo ativo = ativoRepository.findById(request.getAtivoId())
                .orElseThrow(() -> new RegraDeNegocioException("Ativo não encontrado."));

        // Verificar se cliente possui o ativo na carteira
        Optional<Carteira> carteiraOpt = carteiraRepository.findByClienteAndAtivo(cliente, ativo);
        if (carteiraOpt.isEmpty()) {
            throw new RegraDeNegocioException("Cliente não possui este ativo na carteira.");
        }

        Carteira carteira = carteiraOpt.get();
        
        // Verificar se há quantidade suficiente
        if (carteira.getQuantidade() < request.getQuantidade()) {
            throw new RegraDeNegocioException("Quantidade insuficiente na carteira.");
        }

        // Calcular valores
        BigDecimal valorUnitario = ativo.getValorAtual();
        BigDecimal valorTotal = valorUnitario.multiply(new BigDecimal(request.getQuantidade()));
        BigDecimal valorAquisicao = carteira.getValorAquisicao().multiply(new BigDecimal(request.getQuantidade()));
        BigDecimal lucro = valorTotal.subtract(valorAquisicao);
        BigDecimal imposto = calcularImposto(ativo.getTipo(), lucro);

        // Criar resgate
        Resgate resgate = new Resgate();
        resgate.setCliente(cliente);
        resgate.setAtivo(ativo);
        resgate.setQuantidade(request.getQuantidade());
        resgate.setValorUnitario(valorUnitario);
        resgate.setValorTotal(valorTotal);
        resgate.setValorAquisicao(valorAquisicao);
        resgate.setLucro(lucro);
        resgate.setImposto(imposto);
        resgate.setEstado(EstadoResgate.SOLICITADO);
        resgate.setDataSolicitacao(LocalDateTime.now());

        resgate = resgateRepository.save(resgate);

        return converterParaDTO(resgate);
    }

    public List<ResgateResponseDTO> visualizarResgates(String codigoAcesso) {
        Cliente cliente = clienteRepository.findByCodigoAcesso(codigoAcesso)
                .orElseThrow(() -> new ClienteNaoEncontradoException("Cliente não encontrado."));

        List<Resgate> resgates = resgateRepository.findAllByCliente(cliente);
        
        return resgates.stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ResgateResponseDTO confirmarResgate(Long resgateId) {
        Resgate resgate = resgateRepository.findById(resgateId)
                .orElseThrow(() -> new RegraDeNegocioException("Resgate não encontrado."));

        if (resgate.getEstado() != EstadoResgate.SOLICITADO) {
            throw new RegraDeNegocioException("Apenas resgates solicitados podem ser confirmados.");
        }

        // Atualizar estado para confirmado
        resgate.setEstado(EstadoResgate.CONFIRMADO);
        resgate.setDataConfirmacao(LocalDateTime.now());
        resgate = resgateRepository.save(resgate);

        // Notificar cliente
        notificationService.notificarConfirmacaoResgate(resgate);

        return converterParaDTO(resgate);
    }

    @Transactional
    public ResgateResponseDTO finalizarResgate(Long resgateId) {
        Resgate resgate = resgateRepository.findById(resgateId)
                .orElseThrow(() -> new RegraDeNegocioException("Resgate não encontrado."));

        if (resgate.getEstado() != EstadoResgate.CONFIRMADO) {
            throw new RegraDeNegocioException("Apenas resgates confirmados podem ser finalizados.");
        }

        // Remover ativos da carteira
        Carteira carteira = carteiraRepository.findByClienteAndAtivo(resgate.getCliente(), resgate.getAtivo())
                .orElseThrow(() -> new RegraDeNegocioException("Carteira não encontrada."));

        if (carteira.getQuantidade() < resgate.getQuantidade()) {
            throw new RegraDeNegocioException("Quantidade insuficiente na carteira.");
        }

        // Atualizar carteira
        carteira.setQuantidade(carteira.getQuantidade() - resgate.getQuantidade());
        if (carteira.getQuantidade() == 0) {
            carteiraRepository.delete(carteira);
        } else {
            carteiraRepository.save(carteira);
        }

        // Adicionar valor líquido ao saldo do cliente
        BigDecimal valorLiquido = resgate.getValorTotal().subtract(resgate.getImposto());
        Cliente cliente = resgate.getCliente();
        cliente.setSaldo(cliente.getSaldo().add(valorLiquido));
        clienteRepository.save(cliente);

        // Finalizar resgate
        resgate.setEstado(EstadoResgate.EM_CONTA);
        resgate.setDataFinalizacao(LocalDateTime.now());
        resgate = resgateRepository.save(resgate);

        return converterParaDTO(resgate);
    }

    public List<ResgateResponseDTO> listarResgatesSolicitados() {
        List<Resgate> resgates = resgateRepository.findAllByEstado(EstadoResgate.SOLICITADO);
        
        return resgates.stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    private BigDecimal calcularImposto(TipoAtivo tipoAtivo, BigDecimal lucro) {
        if (lucro.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal aliquota;
        switch (tipoAtivo) {
            case TESOURO_DIRETO:
                aliquota = new BigDecimal("0.10"); // 10%
                break;
            case ACAO:
                aliquota = new BigDecimal("0.15"); // 15%
                break;
            case CRIPTOMOEDA:
                if (lucro.compareTo(new BigDecimal("5000")) <= 0) {
                    aliquota = new BigDecimal("0.15"); // 15%
                } else {
                    aliquota = new BigDecimal("0.225"); // 22.5%
                }
                break;
            default:
                aliquota = BigDecimal.ZERO;
        }

        return lucro.multiply(aliquota).setScale(4, RoundingMode.HALF_UP);
    }

    private ResgateResponseDTO converterParaDTO(Resgate resgate) {
        ResgateResponseDTO dto = new ResgateResponseDTO();
        dto.setId(resgate.getId());
        dto.setClienteId(resgate.getCliente().getId());
        dto.setNomeCliente(resgate.getCliente().getNomeCompleto());
        dto.setAtivoId(resgate.getAtivo().getId());
        dto.setNomeAtivo(resgate.getAtivo().getNome());
        dto.setQuantidade(resgate.getQuantidade());
        dto.setValorUnitario(resgate.getValorUnitario());
        dto.setValorTotal(resgate.getValorTotal());
        dto.setValorAquisicao(resgate.getValorAquisicao());
        dto.setLucro(resgate.getLucro());
        dto.setImposto(resgate.getImposto());
        dto.setEstado(resgate.getEstado());
        dto.setDataSolicitacao(resgate.getDataSolicitacao());
        dto.setDataConfirmacao(resgate.getDataConfirmacao());
        dto.setDataFinalizacao(resgate.getDataFinalizacao());
        return dto;
    }
}

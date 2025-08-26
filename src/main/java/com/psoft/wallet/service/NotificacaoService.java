package com.psoft.wallet.service;

import com.psoft.wallet.model.Ativo;
import com.psoft.wallet.model.Cliente;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Locale;

@Service
public class NotificacaoService {

    public void notificarVariacaoPreco(Cliente cliente, Ativo ativo, BigDecimal variacaoPercentual) {
        String direcao = variacaoPercentual.compareTo(BigDecimal.ZERO) > 0 ? "subiu" : "caiu";
        String variacaoFormatada = variacaoPercentual.abs().multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString();

        String mensagem = String.format(Locale.US,
                "[NOTIFICAÇÃO DE PREÇO] Olá, %s! O ativo '%s' que você tem interesse %s %s%% e agora está cotado em R$ %.2f.%n",
                cliente.getNomeCompleto(),
                ativo.getNome(),
                direcao,
                variacaoFormatada,
                ativo.getValorAtual()
        );
        System.out.print(mensagem);
    }

    public void notificarDisponibilidade(Cliente cliente, Ativo ativo) {
        System.out.printf(
                "[NOTIFICAÇÃO DE DISPONIBILIDADE] Olá, %s! O ativo '%s' que você tinha interesse agora está disponível para compra.%n",
                cliente.getNomeCompleto(),
                ativo.getNome()
        );
    }
}
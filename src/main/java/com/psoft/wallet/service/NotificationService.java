package com.psoft.wallet.service;

import com.psoft.wallet.model.Ativo;
import com.psoft.wallet.model.Cliente;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    public void notificarVariacaoPreco(Cliente cliente, Ativo ativo, float precoAntigo, float precoNovo) {
        System.out.printf(
                "NOTIFICAÇÃO para %s: O ativo '%s' teve uma variação de preço significativa. Valor anterior: %.1f, Valor atual: %.1f.%n",
                cliente.getNomeCompleto(), ativo.getNome(), precoAntigo, precoNovo
        );
    }

    public void notificarDisponibilidade(Cliente cliente, Ativo ativo) {
        System.out.printf(
                "NOTIFICAÇÃO para %s: O ativo '%s' agora está disponível para compra.%n",
                cliente.getNomeCompleto(), ativo.getNome()
        );
    }
}
package com.psoft.wallet.service;

public class OperacaoNaoAutorizadaException extends RuntimeException {
    public OperacaoNaoAutorizadaException(String message) {
        super(message);
    }
} 
package com.psoft.wallet.exception;

public class OperacaoNaoAutorizadaException extends RuntimeException {
    public OperacaoNaoAutorizadaException(String message) {
        super(message);
    }
} 
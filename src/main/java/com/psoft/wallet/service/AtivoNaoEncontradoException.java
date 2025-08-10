package com.psoft.wallet.service;

public class AtivoNaoEncontradoException extends RuntimeException {
    public AtivoNaoEncontradoException(String message) {
        super(message);
    }
}
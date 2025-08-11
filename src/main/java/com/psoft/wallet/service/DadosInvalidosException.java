package com.psoft.wallet.service;

public class DadosInvalidosException extends RuntimeException {
    public DadosInvalidosException(String message) {
        super(message);
    }
}
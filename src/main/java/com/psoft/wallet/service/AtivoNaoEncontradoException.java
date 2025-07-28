package com.psoft.wallet.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AtivoNaoEncontradoException extends RuntimeException {
    public AtivoNaoEncontradoException(String message) {
        super(message);
    }
} 
package com.psoft.wallet.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class VariacaoInvalidaException extends IllegalArgumentException {
    public VariacaoInvalidaException(String message) {
        super(message);
    }
}

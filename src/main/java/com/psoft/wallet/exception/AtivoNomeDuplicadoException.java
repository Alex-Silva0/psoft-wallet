package com.psoft.wallet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class AtivoNomeDuplicadoException extends RuntimeException {
    public AtivoNomeDuplicadoException(String message) {
        super(message);
    }
} 
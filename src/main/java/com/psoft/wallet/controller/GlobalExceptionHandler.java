package com.psoft.wallet.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import com.psoft.wallet.service.VariacaoInvalidaException;
import com.psoft.wallet.service.AtivoNaoEncontradoException;
import com.psoft.wallet.service.AtivoNomeDuplicadoException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(VariacaoInvalidaException.class)
    public ResponseEntity<Map<String, Object>> handleVariacaoInvalidaException(VariacaoInvalidaException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", 400);
        errorResponse.put("error", "Bad Request");
        errorResponse.put("message", ex.getMessage());

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(AtivoNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleAtivoNaoEncontradoException(AtivoNaoEncontradoException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", 404);
        errorResponse.put("error", "Not Found");
        errorResponse.put("message", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(AtivoNomeDuplicadoException.class)
    public ResponseEntity<Map<String, Object>> handleAtivoNomeDuplicadoException(AtivoNomeDuplicadoException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", 409);
        errorResponse.put("error", "Conflict");
        errorResponse.put("message", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
} 
package com.psoft.wallet.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import com.psoft.wallet.service.VariacaoInvalidaException;
import com.psoft.wallet.service.AtivoNaoEncontradoException;
import com.psoft.wallet.service.AtivoNomeDuplicadoException;
import com.psoft.wallet.service.ClienteNaoEncontradoException;
import com.psoft.wallet.service.CodigoAcessoIncorretoException;
import com.psoft.wallet.service.OperacaoNaoAutorizadaException;

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

    @ExceptionHandler(ClienteNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleClienteNaoEncontradoException(ClienteNaoEncontradoException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", 404);
        errorResponse.put("error", "Not Found");
        errorResponse.put("message", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(CodigoAcessoIncorretoException.class)
    public ResponseEntity<Map<String, Object>> handleCodigoAcessoIncorretoException(CodigoAcessoIncorretoException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", 401);
        errorResponse.put("error", "Unauthorized");
        errorResponse.put("message", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(OperacaoNaoAutorizadaException.class)
    public ResponseEntity<Map<String, Object>> handleOperacaoNaoAutorizadaException(OperacaoNaoAutorizadaException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", 403);
        errorResponse.put("error", "Forbidden");
        errorResponse.put("message", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", 400);
        errorResponse.put("error", "Bad Request");
        errorResponse.put("message", ex.getMessage());
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
} 
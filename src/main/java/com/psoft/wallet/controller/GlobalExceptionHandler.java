package com.psoft.wallet.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import com.psoft.wallet.service.*;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Object> buildErrorResponse(Exception exception, HttpStatus status) {
        Map<String, String> body = Map.of("message", exception.getMessage());
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<Object> handleRecursoDuplicadoException(RecursoDuplicadoException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.CONFLICT);
    }

    @ExceptionHandler({AtivoNaoEncontradoException.class, ClienteNaoEncontradoException.class})
    public ResponseEntity<Object> handleNotFoundException(RuntimeException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CodigoAcessoIncorretoException.class)
    public ResponseEntity<Object> handleCodigoAcessoIncorretoException(CodigoAcessoIncorretoException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(OperacaoNaoAutorizadaException.class)
    public ResponseEntity<Object> handleOperacaoNaoAutorizadaException(OperacaoNaoAutorizadaException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({DadosInvalidosException.class, RegraDeNegocioException.class, IllegalArgumentException.class})
    public ResponseEntity<Object> handleBadRequestException(RuntimeException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST);
    }
} 
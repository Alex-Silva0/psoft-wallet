package com.psoft.wallet.controller;

import com.psoft.wallet.dto.InteresseDTO;
import com.psoft.wallet.model.Interesse;
import com.psoft.wallet.service.InteresseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interesses")
public class InteresseController {

    private final InteresseService interesseService;

    public InteresseController(InteresseService interesseService) {
        this.interesseService = interesseService;
    }

    @PostMapping
    public ResponseEntity<Interesse> registrarInteresse(@RequestBody InteresseDTO interesseDTO) {
        Interesse novoInteresse = interesseService.registrarInteresse(interesseDTO);
        return new ResponseEntity<>(novoInteresse, HttpStatus.CREATED);
    }

    @DeleteMapping
    public ResponseEntity<Void> removerInteresse(@RequestBody InteresseDTO interesseDTO) {
        interesseService.removerInteresse(interesseDTO);
        return ResponseEntity.noContent().build();
    }
}
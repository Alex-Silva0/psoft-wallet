package com.psoft.wallet.controller;

import com.psoft.wallet.model.Interesse;
import com.psoft.wallet.service.ClienteService;
import com.psoft.wallet.service.InteresseService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/interesses")
public class InteresseController {

    private final InteresseService interesseService;

    public InteresseController(InteresseService interesseService) {
        this.interesseService = interesseService;
    }

    @PostMapping("/{ativoId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Interesse marcarInteresse(
            @PathVariable Long ativoId,
            @RequestParam String codigoAcesso
    ) {
        return interesseService.marcarInteresse(ativoId, codigoAcesso);
    }
}
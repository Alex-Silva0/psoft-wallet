package com.psoft.wallet.controller;

import com.psoft.wallet.dto.CarteiraDTO;
import com.psoft.wallet.service.CarteiraService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carteira")
public class CarteiraController {

    private final CarteiraService carteiraService;

    public CarteiraController(CarteiraService carteiraService) {
        this.carteiraService = carteiraService;
    }

    @GetMapping("/cliente/{codigoAcesso}")
    public ResponseEntity<List<CarteiraDTO>> visualizarCarteira(@PathVariable String codigoAcesso) {
        List<CarteiraDTO> carteira = carteiraService.visualizarCarteira(codigoAcesso);
        return ResponseEntity.ok(carteira);
    }
}

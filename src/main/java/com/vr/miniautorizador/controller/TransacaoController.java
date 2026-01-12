package com.vr.miniautorizador.controller;

import com.vr.miniautorizador.dto.TransacaoRequest;
import com.vr.miniautorizador.service.CartaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transacoes")
@RequiredArgsConstructor
public class TransacaoController {

    private final CartaoService cartaoService;

    @PostMapping
    public ResponseEntity<String> realizarTransacao(@Valid @RequestBody TransacaoRequest request) {
        cartaoService.realizarTransacao(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("OK");
    }
}

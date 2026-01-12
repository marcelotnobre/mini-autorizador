package com.vr.miniautorizador.exception;

import com.vr.miniautorizador.dto.CartaoResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CartaoExistenteException.class)
    public ResponseEntity<CartaoResponse> handleCartaoExistente(CartaoExistenteException ex) {
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ex.getCartaoResponse());
    }

    @ExceptionHandler(CartaoNaoEncontradoException.class)
    public ResponseEntity<Void> handleCartaoNaoEncontrado(CartaoNaoEncontradoException ex) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(TransacaoNaoAutorizadaException.class)
    public ResponseEntity<String> handleTransacaoNaoAutorizada(TransacaoNaoAutorizadaException ex) {
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ex.getStatus().name());
    }
}

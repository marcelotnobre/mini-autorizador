package com.vr.miniautorizador.exception;

public class CartaoNaoEncontradoException extends RuntimeException {

    public CartaoNaoEncontradoException() {
        super("Cartão não encontrado");
    }
}

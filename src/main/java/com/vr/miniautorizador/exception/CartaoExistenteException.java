package com.vr.miniautorizador.exception;

import com.vr.miniautorizador.dto.CartaoResponse;
import lombok.Getter;

@Getter
public class CartaoExistenteException extends RuntimeException {

    private final CartaoResponse cartaoResponse;

    public CartaoExistenteException(CartaoResponse cartaoResponse) {
        super("Cartão já existe");
        this.cartaoResponse = cartaoResponse;
    }
}

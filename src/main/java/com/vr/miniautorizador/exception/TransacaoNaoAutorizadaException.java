package com.vr.miniautorizador.exception;

import lombok.Getter;

@Getter
public class TransacaoNaoAutorizadaException extends RuntimeException {

    private final TransacaoStatus status;

    public TransacaoNaoAutorizadaException(TransacaoStatus status) {
        super(status.name());
        this.status = status;
    }
}

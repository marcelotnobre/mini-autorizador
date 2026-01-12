package com.vr.miniautorizador.service;

import com.vr.miniautorizador.dto.CartaoRequest;
import com.vr.miniautorizador.dto.CartaoResponse;
import com.vr.miniautorizador.dto.TransacaoRequest;
import com.vr.miniautorizador.entity.Cartao;
import com.vr.miniautorizador.exception.CartaoExistenteException;
import com.vr.miniautorizador.exception.CartaoNaoEncontradoException;
import com.vr.miniautorizador.exception.TransacaoNaoAutorizadaException;
import com.vr.miniautorizador.exception.TransacaoStatus;
import com.vr.miniautorizador.repository.CartaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CartaoService {

    private static final BigDecimal SALDO_INICIAL = new BigDecimal("500.00");

    private final CartaoRepository cartaoRepository;

    @Transactional
    public CartaoResponse criarCartao(CartaoRequest request) {
        cartaoRepository.findById(request.getNumeroCartao())
                .ifPresent(cartao -> {
                    throw new CartaoExistenteException(
                            CartaoResponse.builder()
                                    .numeroCartao(cartao.getNumeroCartao())
                                    .senha(cartao.getSenha())
                                    .build()
                    );
                });

        Cartao cartao = Cartao.builder()
                .numeroCartao(request.getNumeroCartao())
                .senha(request.getSenha())
                .saldo(SALDO_INICIAL)
                .build();

        cartaoRepository.save(cartao);

        return CartaoResponse.builder()
                .numeroCartao(cartao.getNumeroCartao())
                .senha(cartao.getSenha())
                .build();
    }

    @Transactional(readOnly = true)
    public BigDecimal obterSaldo(String numeroCartao) {
        return cartaoRepository.findById(numeroCartao)
                .map(Cartao::getSaldo)
                .orElseThrow(CartaoNaoEncontradoException::new);
    }

    @Transactional
    public void realizarTransacao(TransacaoRequest request) {
        Cartao cartao = cartaoRepository.findByNumeroCartaoComLock(request.getNumeroCartao())
                .orElseThrow(() -> new TransacaoNaoAutorizadaException(TransacaoStatus.CARTAO_INEXISTENTE));

        validarSenha(cartao, request.getSenhaCartao());
        validarSaldo(cartao, request.getValor());

        cartao.setSaldo(cartao.getSaldo().subtract(request.getValor()));
        cartaoRepository.save(cartao);
    }

    private void validarSenha(Cartao cartao, String senha) {
        boolean senhaValida = cartao.getSenha().equals(senha);
        
        if (!senhaValida) {
            throw new TransacaoNaoAutorizadaException(TransacaoStatus.SENHA_INVALIDA);
        }
    }

    private void validarSaldo(Cartao cartao, BigDecimal valor) {
        boolean saldoSuficiente = cartao.getSaldo().compareTo(valor) >= 0;
        
        if (!saldoSuficiente) {
            throw new TransacaoNaoAutorizadaException(TransacaoStatus.SALDO_INSUFICIENTE);
        }
    }
}

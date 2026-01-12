package com.vr.miniautorizador;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vr.miniautorizador.dto.CartaoRequest;
import com.vr.miniautorizador.dto.TransacaoRequest;
import com.vr.miniautorizador.repository.CartaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MiniAutorizadorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CartaoRepository cartaoRepository;

    private static final String NUMERO_CARTAO = "6549873025634501";
    private static final String SENHA = "1234";

    @BeforeEach
    void setUp() {
        cartaoRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve criar um cartão com sucesso")
    void deveCriarCartaoComSucesso() throws Exception {
        CartaoRequest request = CartaoRequest.builder()
                .numeroCartao(NUMERO_CARTAO)
                .senha(SENHA)
                .build();

        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroCartao").value(NUMERO_CARTAO))
                .andExpect(jsonPath("$.senha").value(SENHA));
    }

    @Test
    @DisplayName("Deve retornar 422 ao tentar criar cartão já existente")
    void deveRetornar422QuandoCartaoJaExiste() throws Exception {
        CartaoRequest request = CartaoRequest.builder()
                .numeroCartao(NUMERO_CARTAO)
                .senha(SENHA)
                .build();

        // Primeira criação
        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Segunda criação - deve falhar
        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.numeroCartao").value(NUMERO_CARTAO))
                .andExpect(jsonPath("$.senha").value(SENHA));
    }

    @Test
    @DisplayName("Deve obter saldo do cartão com sucesso")
    void deveObterSaldoComSucesso() throws Exception {
        // Criar cartão primeiro
        CartaoRequest request = CartaoRequest.builder()
                .numeroCartao(NUMERO_CARTAO)
                .senha(SENHA)
                .build();

        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Consultar saldo
        mockMvc.perform(get("/cartoes/{numeroCartao}", NUMERO_CARTAO))
                .andExpect(status().isOk())
                .andExpect(content().string("500.00"));
    }

    @Test
    @DisplayName("Deve retornar 404 ao consultar saldo de cartão inexistente")
    void deveRetornar404QuandoCartaoNaoExiste() throws Exception {
        mockMvc.perform(get("/cartoes/{numeroCartao}", "0000000000000000"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve realizar transação com sucesso")
    void deveRealizarTransacaoComSucesso() throws Exception {
        // Criar cartão
        CartaoRequest cartaoRequest = CartaoRequest.builder()
                .numeroCartao(NUMERO_CARTAO)
                .senha(SENHA)
                .build();

        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartaoRequest)))
                .andExpect(status().isCreated());

        // Realizar transação
        TransacaoRequest transacaoRequest = TransacaoRequest.builder()
                .numeroCartao(NUMERO_CARTAO)
                .senhaCartao(SENHA)
                .valor(new BigDecimal("10.00"))
                .build();

        mockMvc.perform(post("/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transacaoRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().string("OK"));

        // Verificar saldo atualizado
        mockMvc.perform(get("/cartoes/{numeroCartao}", NUMERO_CARTAO))
                .andExpect(status().isOk())
                .andExpect(content().string("490.00"));
    }

    @Test
    @DisplayName("Deve retornar SALDO_INSUFICIENTE quando saldo for insuficiente")
    void deveRetornarSaldoInsuficiente() throws Exception {
        // Criar cartão
        CartaoRequest cartaoRequest = CartaoRequest.builder()
                .numeroCartao(NUMERO_CARTAO)
                .senha(SENHA)
                .build();

        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartaoRequest)))
                .andExpect(status().isCreated());

        // Realizar transação com valor maior que o saldo
        TransacaoRequest transacaoRequest = TransacaoRequest.builder()
                .numeroCartao(NUMERO_CARTAO)
                .senhaCartao(SENHA)
                .valor(new BigDecimal("600.00"))
                .build();

        mockMvc.perform(post("/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transacaoRequest)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("SALDO_INSUFICIENTE"));
    }

    @Test
    @DisplayName("Deve retornar SENHA_INVALIDA quando senha for incorreta")
    void deveRetornarSenhaInvalida() throws Exception {
        // Criar cartão
        CartaoRequest cartaoRequest = CartaoRequest.builder()
                .numeroCartao(NUMERO_CARTAO)
                .senha(SENHA)
                .build();

        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartaoRequest)))
                .andExpect(status().isCreated());

        // Realizar transação com senha incorreta
        TransacaoRequest transacaoRequest = TransacaoRequest.builder()
                .numeroCartao(NUMERO_CARTAO)
                .senhaCartao("0000")
                .valor(new BigDecimal("10.00"))
                .build();

        mockMvc.perform(post("/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transacaoRequest)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("SENHA_INVALIDA"));
    }

    @Test
    @DisplayName("Deve retornar CARTAO_INEXISTENTE quando cartão não existir")
    void deveRetornarCartaoInexistente() throws Exception {
        TransacaoRequest transacaoRequest = TransacaoRequest.builder()
                .numeroCartao("0000000000000000")
                .senhaCartao(SENHA)
                .valor(new BigDecimal("10.00"))
                .build();

        mockMvc.perform(post("/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transacaoRequest)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("CARTAO_INEXISTENTE"));
    }

    @Test
    @DisplayName("Deve realizar múltiplas transações até saldo insuficiente")
    void deveRealizarMultiplasTransacoesAteSaldoInsuficiente() throws Exception {
        // Criar cartão
        CartaoRequest cartaoRequest = CartaoRequest.builder()
                .numeroCartao(NUMERO_CARTAO)
                .senha(SENHA)
                .build();

        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartaoRequest)))
                .andExpect(status().isCreated());

        // Realizar 5 transações de R$100,00 cada (total R$500,00)
        for (int i = 0; i < 5; i++) {
            TransacaoRequest transacaoRequest = TransacaoRequest.builder()
                    .numeroCartao(NUMERO_CARTAO)
                    .senhaCartao(SENHA)
                    .valor(new BigDecimal("100.00"))
                    .build();

            mockMvc.perform(post("/transacoes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(transacaoRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(content().string("OK"));
        }

        // Verificar saldo zerado
        mockMvc.perform(get("/cartoes/{numeroCartao}", NUMERO_CARTAO))
                .andExpect(status().isOk())
                .andExpect(content().string("0.00"));

        // Próxima transação deve falhar por saldo insuficiente
        TransacaoRequest transacaoRequest = TransacaoRequest.builder()
                .numeroCartao(NUMERO_CARTAO)
                .senhaCartao(SENHA)
                .valor(new BigDecimal("1.00"))
                .build();

        mockMvc.perform(post("/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transacaoRequest)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("SALDO_INSUFICIENTE"));
    }
}

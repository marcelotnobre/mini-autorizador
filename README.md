# Mini Autorizador - VR Benefícios

Sistema de autorização de transações de cartões de benefícios.

## Tecnologias Utilizadas

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **MySQL 5.7**
- **Lombok**
- **H2 Database** (para testes)
- **JUnit 5** (testes)

## Arquitetura

O projeto segue uma arquitetura em camadas:

```
src/main/java/com/vr/miniautorizador/
├── controller/          # Endpoints REST
├── service/             # Regras de negócio
├── repository/          # Acesso a dados
├── entity/              # Entidades JPA
├── dto/                 # Objetos de transferência
└── exception/           # Exceções e handlers
```

## Design Patterns e Boas Práticas

- **DTO Pattern**: Separação entre entidades de domínio e objetos de API
- **Repository Pattern**: Abstração do acesso a dados com Spring Data JPA
- **Exception Handler**: Tratamento centralizado de exceções com `@RestControllerAdvice`
- **Injeção de Dependência**: Uso de `@RequiredArgsConstructor` do Lombok
- **Builder Pattern**: Construção fluente de objetos com `@Builder`
- **Pessimistic Locking**: Controle de concorrência no banco de dados

## Controle de Concorrência

Para garantir que duas transações simultâneas não causem problemas de concorrência, foram implementadas duas estratégias:

1. **Pessimistic Locking**: O método `findByNumeroCartaoComLock` utiliza `@Lock(LockModeType.PESSIMISTIC_WRITE)` para bloquear o registro durante a transação.

2. **Optimistic Locking**: A entidade `Cartao` possui um campo `@Version` que é incrementado automaticamente a cada atualização, prevenindo atualizações perdidas.

## Como Executar

### Pré-requisitos

- Java 17+
- Maven 3.8+
- Docker e Docker Compose

### 1. Iniciar o banco de dados

```bash
docker-compose up -d mysql
```

### 2. Executar a aplicação

```bash
mvn spring-boot:run
```

A aplicação estará disponível em `http://localhost:8080`

### 3. Executar testes

```bash
mvn test
```

## Endpoints da API

### Criar Cartão

```http
POST /cartoes
Content-Type: application/json

{
    "numeroCartao": "6549873025634501",
    "senha": "1234"
}
```

**Respostas:**
- `201 Created`: Cartão criado com sucesso
- `422 Unprocessable Entity`: Cartão já existe

### Consultar Saldo

```http
GET /cartoes/{numeroCartao}
```

**Respostas:**
- `200 OK`: Retorna o saldo (ex: `495.15`)
- `404 Not Found`: Cartão não encontrado

### Realizar Transação

```http
POST /transacoes
Content-Type: application/json

{
    "numeroCartao": "6549873025634501",
    "senhaCartao": "1234",
    "valor": 10.00
}
```

**Respostas:**
- `201 Created`: Transação realizada com sucesso (`OK`)
- `422 Unprocessable Entity`: Transação não autorizada
  - `CARTAO_INEXISTENTE`
  - `SENHA_INVALIDA`
  - `SALDO_INSUFICIENTE`

## Regras de Negócio

1. Todo cartão é criado com saldo inicial de **R$ 500,00**
2. Uma transação só é autorizada se:
   - O cartão existir
   - A senha estiver correta
   - Houver saldo suficiente
3. Não é permitido criar cartões duplicados

## Suposições

1. O número do cartão é tratado como String para preservar zeros à esquerda
2. A senha é armazenada em texto plano (em produção deveria ser hasheada)
3. O saldo é representado com precisão de 2 casas decimais usando `BigDecimal`

## Estrutura do Banco de Dados

```sql
CREATE TABLE cartao (
    numero_cartao VARCHAR(16) PRIMARY KEY,
    senha VARCHAR(255) NOT NULL,
    saldo DECIMAL(10,2) NOT NULL,
    version BIGINT
);
```

## Testes

O projeto inclui testes de integração que cobrem todos os cenários especificados:

- ✅ Criação de cartão
- ✅ Tentativa de criação de cartão duplicado
- ✅ Consulta de saldo
- ✅ Consulta de saldo de cartão inexistente
- ✅ Transação com sucesso
- ✅ Transação com saldo insuficiente
- ✅ Transação com senha inválida
- ✅ Transação com cartão inexistente
- ✅ Múltiplas transações até esgotar saldo

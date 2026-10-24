# 🐾 petVerso API

API REST do **petVerso**, um aplicativo mobile para **compartilhar o cuidado de pets entre várias pessoas**. O foco não é compartilhar dados do usuário, e sim os dados do pet: tutores vinculados ao mesmo pet enxergam o perfil dele, uma agenda de tarefas e a carteira de vacinas em comum, e podem acompanhar o que já foi feito.

Projeto desenvolvido como TCC. O aplicativo Android é desenvolvido separadamente e consome esta API.

> 🚧 **Em desenvolvimento.** A API é construída de forma incremental: Usuário → Pet → Vínculo → Agenda → Vacinas.

---

## ✨ Funcionalidades

**Usuários**
- Cadastro com e-mail, telefone e senha (e-mail e telefone únicos)
- Login por e-mail e senha
- Perfil com apelido e foto de perfil, ambos opcionais

**Pets**
- Cadastro de pet com dados básicos (nome, raça, espécie, porte, peso, sexo, data de nascimento) e foto opcional
- Geração automática de um **código de vínculo** (formato `XXX-XXX`) para compartilhar o pet
- Edição do perfil de sensibilidade (alergias, restrições, sensibilidade emocional) e das personalidades
- Atualização da foto do pet (limite de 5 MB)
- Listagem dos pets do usuário

**Vínculo entre tutores**
- O dono do pet compartilha o código de vínculo
- Outro usuário solicita ser **tutor de suporte** usando o código
- O dono lista, aceita ou recusa as solicitações
- Solicitações recusadas podem ser feitas novamente

**Agenda (tarefas)**
- Criação de tarefas para um pet (dono e suporte)
- Listagem das tarefas de um pet
- Listagem das tarefas de um dia, somando todos os pets aos quais o usuário tem vínculo aceito
- Alteração de uma tarefa (somente o dono, e apenas enquanto ela não estiver concluída)
- Conclusão de tarefa por qualquer usuário com vínculo aceito, registrando quem executou e quando

**Vacinas**
- Registro de vacinas do pet com nome, dose única (sim/não), intervalo de revacinação (quantidade + unidade) e observação (somente o dono)
- A vacina nasce como `PENDENTE`, podendo ser agendada antes de ser aplicada
- Marcar a vacina como aplicada (qualquer usuário com vínculo aceito): a data de aplicação passa a ser o dia de hoje e a data da próxima dose é calculada a partir do intervalo
- Status da vacina: `PENDENTE`, `APLICADA` ou `ATRASADA` (esta última é derivada na resposta e nunca gravada no banco)
- Listagem das vacinas do pet, da aplicação mais recente para a mais antiga
- Alteração de uma vacina (somente o dono), recalculando a próxima dose quando a vacina já foi aplicada e não é dose única

---

## 🧱 Stack

| Camada | Tecnologia |
| --- | --- |
| Linguagem | Java 21 |
| Framework | Spring Boot 4.1.1 |
| Módulos | Spring Web MVC, Spring Data JPA, Spring Security, Bean Validation |
| Banco de dados | MySQL (driver `mysql-connector-j`) |
| Build | Maven (Maven Wrapper incluso) |
| Container | Docker (build multi-stage) |
| Hospedagem | API no Render, MySQL na Aiven |

Pacote base: `com.namassacompany.petVersoRestFull`

---

## 🏗️ Arquitetura

Organização em camadas:

```
src/main/java/com/namassacompany/petVersoRestFull/
├── controller/   # endpoints REST
├── service/      # regras de negócio
├── repository/   # acesso a dados (Spring Data JPA)
├── model/        # entidades JPA e enums
├── dto/          # records de entrada e saída
├── exception/    # exceções de domínio e GlobalExceptionHandler
└── security/     # SecurityConfig e TokenAuthFilter
```

Principais decisões:

- **DTOs em vez de entidades** na entrada e na saída, evitando mass assignment e vazamento de dados.
- **Fotos como Base64 no JSON** (sem multipart) e armazenadas como `MEDIUMBLOB`. O DTO leve (listagens) não leva foto; o DTO de perfil leva.
- **Enums** (`Papel`, `StatusDeVinculo`, `Porte`, `Sexo`, `StatusVacina`, `IntervaloUnidade`) gravados como `String` no banco.
- **Controle de acesso mascarado**: quem não tem vínculo aceito com um recurso recebe o mesmo `404` de um recurso inexistente, sem revelar que ele existe.
- **Verificação de vínculo centralizada**: o `VinculoAcessoService` concentra a regra "vínculo aceito" e "papel de dono", usada pelos services de tarefas e vacinas. Cada recurso informa a exceção a lançar, para que o `404` mascarado fale do seu próprio recurso.
- **Alterações completas (`PUT`)**: tarefas e vacinas são atualizadas reenviando todos os campos; a hora da tarefa vazia significa "sem hora".
- **Próxima dose calculada pelo servidor**: a vacina guarda o intervalo (quantidade + unidade) e o service calcula a `dataProximaDose` ao marcar como aplicada ou ao alterar uma vacina já aplicada; o cliente nunca envia essa data.
- **Status da vacina derivado**: o banco guarda só `PENDENTE` e `APLICADA`; `ATRASADA` é calculada ao montar o DTO de resposta (campo `statusVacina`), sem alterar a entidade.
- **Fuso horário explícito**: datas "de hoje" usam `America/Sao_Paulo` em cada `now()`, independentemente do fuso do servidor.

### Modelo de dados (resumo)

```
Usuario ──< VinculoPet >── Pet ──< Tarefa
                │            │        │
        papel: DONO | SUPORTE │        └── executadoPor → Usuario
        status: PENDENTE | ACEITO | RECUSADO
                             └──< Vacina
```

- **Usuario**: nome, e-mail, telefone, senha, apelido, foto
- **Pet**: nome, raça, espécie, porte, peso, sexo, data de nascimento, perfil de sensibilidade, foto, código de vínculo, personalidades
- **VinculoPet**: relação muitos-para-muitos entre usuário e pet, com papel, status e data de criação
- **Sessao**: token de sessão de cada login
- **Tarefa**: título, descrição, data, hora, concluída, quem executou, data de conclusão
- **Vacina**: nome, data de aplicação (opcional, preenchida ao aplicar), dose única, intervalo de revacinação (quantidade + unidade), data da próxima dose (calculada), status (`PENDENTE` ou `APLICADA`), observação (opcional, até 500 caracteres)

---

## 🔐 Autenticação e autorização

Autenticação por **token opaco de sessão salvo no banco** (não é JWT):

1. O cadastro e o login retornam um token.
2. Nas demais requisições, envie `Authorization: Bearer <token>`.
3. O `TokenAuthFilter` busca a sessão pelo token e coloca o usuário no `SecurityContext`; os controllers o recebem com `@AuthenticationPrincipal`.
4. O token **não expira sozinho**: o app é de uso exclusivo no celular e a sessão só termina com logout manual.
5. Um mesmo usuário pode ter várias sessões válidas ao mesmo tempo.

Rotas públicas: `POST /api/usuarios/cadastrar` e `POST /api/login`. Todo o resto exige token; sem ele a resposta é `403`.

**Regras por papel**

| Ação | DONO | SUPORTE (aceito) | Sem vínculo / pendente |
| --- | :---: | :---: | :---: |
| Ver perfil do pet | ✅ | ✅ | ❌ (404) |
| Ver agenda do pet | ✅ | ✅ | ❌ (404) |
| Criar tarefa | ✅ | ✅ | ❌ (404) |
| Concluir tarefa | ✅ | ✅ | ❌ (404) |
| Alterar tarefa | ✅ | ❌ (403) | ❌ (404) |
| Ver vacinas do pet | ✅ | ✅ | ❌ (404) |
| Registrar e alterar vacina | ✅ | ❌ (403) | ❌ (404) |
| Marcar vacina como aplicada | ✅ | ✅ | ❌ (404) |
| Editar sensibilidade e personalidades | ✅ | ❌ (403) | ❌ (404) |
| Atualizar foto do pet | ✅ | ❌ (403) | ❌ (404) |
| Ver e processar solicitações de vínculo | ✅ | ❌ (403) | ❌ (404) |

> A alteração de tarefas e vacinas pelo suporte, mediante **solicitação ao dono**, está planejada (ver [Roadmap](#️-roadmap)). Até lá, o suporte recebe `403` nessas ações.

---

## 📡 Endpoints

Todos os endpoints (exceto os públicos) exigem `Authorization: Bearer <token>`.

### Usuários e autenticação

| Método | Rota | Descrição |
| --- | --- | --- |
| `POST` | `/api/usuarios/cadastrar` | Cadastra o usuário e já retorna o token (público) |
| `POST` | `/api/login` | Autentica por e-mail e senha e retorna o token (público) |
| `GET` | `/api/usuarios/perfil` | Perfil do usuário autenticado (com foto) |
| `PUT` | `/api/usuarios/atualizarPerfil` | Atualiza apelido e foto (Base64) |

### Pets e vínculos

| Método | Rota | Descrição |
| --- | --- | --- |
| `POST` | `/api/pets/cadastrar` | Cadastra um pet; o criador vira DONO e a resposta traz o código de vínculo |
| `GET` | `/api/pets/meusPets` | Lista os pets vinculados ao usuário (id, nome, papel e foto) |
| `GET` | `/api/pets/{id}/perfil` | Perfil completo do pet |
| `PUT` | `/api/pets/{id}/atualizarPetPerfil` | Atualiza perfil de sensibilidade e personalidades (somente DONO) |
| `PUT` | `/api/pets/{id}/foto` | Atualiza a foto do pet (somente DONO, máx. 5 MB) |
| `POST` | `/api/pets/solicitarVinculo` | Solicita vínculo como suporte usando o código do pet |
| `GET` | `/api/pets/listarSolicitacoes` | Lista as solicitações de vínculo pendentes (somente DONO) |
| `POST` | `/api/pets/processarSolicitacao` | Aceita ou recusa uma solicitação (somente DONO) |

### Agenda

| Método | Rota | Descrição |
| --- | --- | --- |
| `POST` | `/api/pets/{id}/tarefas` | Cria uma tarefa para o pet (DONO e SUPORTE) |
| `GET` | `/api/pets/{id}/tarefas` | Lista as tarefas de um pet |
| `GET` | `/api/tarefas?data=yyyy-MM-dd` | Lista as tarefas do dia de todos os pets vinculados |
| `PUT` | `/api/tarefas/{idTarefa}` | Altera a tarefa (somente DONO, e apenas se ainda não concluída) |
| `PUT` | `/api/tarefas/{idTarefa}/concluir` | Marca a tarefa como concluída (qualquer vínculo aceito) |

### Vacinas

| Método | Rota | Descrição |
| --- | --- | --- |
| `POST` | `/api/pets/{idPet}/vacinas` | Registra uma vacina para o pet, sempre como `PENDENTE` (somente DONO) |
| `GET` | `/api/pets/{idPet}/vacinas` | Lista as vacinas do pet, da aplicação mais recente para a mais antiga, com `statusVacina` |
| `PUT` | `/api/vacinas/{idVacina}` | Altera uma vacina (somente DONO) |
| `PATCH` | `/api/vacinas/{idVacina}/aplicar` | Marca a vacina como aplicada hoje e calcula a próxima dose (qualquer vínculo aceito; `409` se já aplicada) |


### Exemplos

```bash
# Cadastro (retorna o token)
curl -X POST http://localhost:8080/api/usuarios/cadastrar \
  -H "Content-Type: application/json" \
  -d '{"nome":"Maria","email":"maria@email.com","telefone":"11999999999","senha":"********"}'

# Criar uma tarefa
curl -X POST http://localhost:8080/api/pets/1/tarefas \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"titulo":"Passeio","descricao":"30 minutos","dataTarefa":"2026-10-10","hora":"08:00"}'

# Registrar uma vacina
curl -X POST http://localhost:8080/api/pets/1/vacinas \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"nome":"V10","dosagemUnica":false,"intervaloQuantidade":1,"intervaloUnidade":"ANOS","observacao":"Reforço anual"}'

# Marcar a vacina como aplicada (data de aplicação = hoje, próxima dose calculada)
curl -X PATCH http://localhost:8080/api/vacinas/1/aplicar \
  -H "Authorization: Bearer <token>"
```

### Regras de validação

- **Vacina**: `nome` e `dosagemUnica` são obrigatórios; vacina de dose única não aceita intervalo; vacina que não é dose única exige intervalo (quantidade e unidade) com quantidade maior que zero; a observação aceita até 500 caracteres. Uma vacina já aplicada não pode ser aplicada novamente.
- **Tarefa**: `titulo` e `dataTarefa` são obrigatórios; uma tarefa já concluída não pode ser alterada nem concluída novamente.

### Erros

Tratados de forma central no `GlobalExceptionHandler`:

| Status | Quando |
| --- | --- |
| `400` | Validação de campos, datas inválidas, intervalo de vacina inválido, foto inválida ou grande demais |
| `403` | Sem token ou sem permissão para a ação |
| `404` | Pet, tarefa, vacina ou solicitação não encontrada (ou sem acesso a ela) |
| `409` | Conflito, como e-mail ou telefone já cadastrado, solicitação de vínculo duplicada, tarefa já concluída ou vacina já aplicada |

---

## 🚀 Como rodar localmente

### Pré-requisitos

- Java 21
- MySQL em execução (local ou remoto)
- Docker (opcional)

### Variáveis de ambiente

| Variável | Descrição |
| --- | --- |
| `DB_HOST` | Host do MySQL |
| `DB_PORT` | Porta do MySQL |
| `DB_NAME` | Nome do schema |
| `DB_USER` | Usuário do banco |
| `DB_PASSWORD` | Senha do banco |

### Com Maven

```bash
git clone https://github.com/justletthefatherlead/petVerso-API.git
cd petVerso-API

export DB_HOST=localhost DB_PORT=3306 DB_NAME=petverso DB_USER=root DB_PASSWORD=sua_senha
./mvnw spring-boot:run
```

### Com Docker

```bash
docker build -t petverso-api .

docker run -p 8080:8080 \
  -e DB_HOST=... -e DB_PORT=... -e DB_NAME=... \
  -e DB_USER=... -e DB_PASSWORD=... \
  petverso-api
```

O `Dockerfile` é multi-stage: compila com `eclipse-temurin:21-jdk` + Maven Wrapper e executa o `.jar` em `eclipse-temurin:21-jre`.

---

## ☁️ Deploy

- **API**: Render, a partir do `Dockerfile`.
- **Banco**: MySQL na Aiven.
- A configuração do banco vem 100% de variáveis de ambiente; nenhum segredo fica no repositório.

---

## 📝 Notas de desenvolvimento

- `spring.jpa.hibernate.ddl-auto=update` é usado enquanto as entidades ainda estão sendo criadas. A migração para Flyway ou Liquibase está prevista para quando o projeto se aproximar de produção.
- `spring.jpa.open-in-view=false`: relacionamentos `LAZY` são acessados dentro de `@Transactional` e copiados para os DTOs (`List.copyOf`).
- `@GeneratedValue(strategy = GenerationType.IDENTITY)` em todas as entidades (necessário com MySQL no Hibernate 7).
- Coleções `@ElementCollection` (como `pet_personalidades`) podem não ter a tabela criada pelo Hibernate no `ddl-auto=update`; confira com `SHOW TABLES` após deploys que criem tabelas novas. A Aiven exige chave primária em todas as tabelas.
- A tabela `vacina` mudou de estrutura (status, dose única e intervalo no lugar de data da próxima dose informada). Como o `ddl-auto=update` não remove nem converte colunas antigas, a tabela precisa ser recriada (DROP e nova criação pelo Hibernate) no banco local e na Aiven, o que é seguro enquanto ainda não há registros de vacina.
- Os controllers recebem o usuário autenticado com `@AuthenticationPrincipal Usuario`, sem acessar o `SecurityContextHolder` diretamente.

---

## 🗺️ Roadmap

- [x] Cadastro, login e sessão por token
- [x] Perfil do usuário (apelido e foto)
- [x] Cadastro e perfil do pet
- [x] Vínculo por código (dono e tutor de suporte)
- [x] Agenda: criar, listar, alterar e concluir tarefas
- [x] Módulo de vacinas (registrar, listar e alterar)
- [ ] Rodada de testes consolidada e deploy para integração com o app Android
- [ ] Solicitação de alteração: o suporte propõe a mudança e o dono aprova ou recusa (tarefas e vacinas)
- [ ] Alertas de vacina baseados na data da próxima dose
- [ ] Verificação de e-mail no cadastro
- [ ] Revisão de atomicidade (`@Transactional`) no cadastro de usuário
- [ ] Padronizar o nome dos DTOs em `Request`/`Response`


---

## 👤 Autor

Desenvolvido por [@justletthefatherlead](https://github.com/justletthefatherlead).
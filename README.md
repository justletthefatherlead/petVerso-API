# 🐾 petVerso API

API REST do **petVerso**, um aplicativo mobile para **compartilhar o cuidado de pets entre várias pessoas**. O foco não é compartilhar dados do usuário, e sim os dados do pet: tutores vinculados ao mesmo pet enxergam o perfil dele, uma agenda de tarefas e a carteira de vacinas em comum, e podem acompanhar o que já foi feito.

Projeto desenvolvido como TCC. O aplicativo Android é desenvolvido separadamente e consome esta API.

> A API foi construída de forma incremental: Usuário → Pet → Vínculo → Agenda → Vacinas → Solicitações de alteração.
 
---

## ✨ Funcionalidades

**Usuários**
- Cadastro com e-mail, telefone e senha (e-mail e telefone únicos)
- Login por e-mail e senha e logout (encerra apenas a sessão atual)
- Perfil com apelido e foto de perfil, ambos opcionais
- Alteração dos dados da conta (nome, e-mail e telefone, mantendo a unicidade)
- Alteração de senha: exige a senha atual, a nova precisa ser diferente e as **outras sessões do usuário são encerradas**, mantendo a atual
  **Pets**
- Cadastro de pet com dados básicos (nome, raça, espécie, porte, peso, sexo, data de nascimento) e foto opcional
- Geração automática de um **código de vínculo** (formato `XXX-XXX`) para compartilhar o pet
- Edição dos dados básicos do pet
- Edição do perfil de sensibilidade (alergias, restrições, sensibilidade emocional) e das personalidades
- Atualização da foto do pet (limite de 5 MB)
- Listagem dos pets do usuário
- Exclusão do pet (somente o dono), removendo junto tarefas, vacinas, solicitações e vínculos
  **Vínculo entre tutores**
- O dono do pet compartilha o código de vínculo
- Outro usuário solicita ser **tutor de suporte** usando o código
- O dono lista, aceita ou recusa as solicitações de vínculo
- Solicitações recusadas podem ser feitas novamente
- Qualquer tutor com vínculo aceito lista os tutores do pet
- O dono remove um tutor de suporte; o suporte pode sair do pet por conta própria (o dono não pode sair, apenas excluir o pet)
- Quem foi removido ou saiu pode solicitar o vínculo de novo com o código, e o dono decide
  **Agenda (tarefas)**
- Criação de tarefas para um pet (dono e suporte)
- Listagem das tarefas de um pet
- Listagem das tarefas de um dia, somando todos os pets aos quais o usuário tem vínculo aceito
- Alteração de uma tarefa (somente o dono, e apenas enquanto ela não estiver concluída)
- Conclusão de tarefa por qualquer usuário com vínculo aceito, registrando quem executou e quando
- Exclusão de tarefa (somente o dono)
  **Vacinas**
- Registro de vacinas do pet com nome, data, se já foi aplicada (`jaAplicada`), dose única (sim/não), intervalo de revacinação (quantidade + unidade) e observação (somente o dono)
- Vacina já tomada entra como `APLICADA`, com a data real e a próxima dose já calculada; vacina ainda não tomada entra como `PENDENTE`, com a data prevista
- Marcar uma vacina pendente como aplicada (qualquer usuário com vínculo aceito): a data de aplicação passa a ser o dia de hoje e a data da próxima dose é calculada a partir do intervalo
- Status da vacina: `PENDENTE`, `APLICADA` ou `ATRASADA` (pendente com data já passada; derivada na resposta e nunca gravada no banco)
- Listagem das vacinas do pet, da aplicação mais recente para a mais antiga
- Alteração de uma vacina (somente o dono), recalculando a próxima dose quando a vacina está aplicada e não é dose única; uma vacina `PENDENTE` pode virar `APLICADA`, mas uma `APLICADA` não volta a `PENDENTE`
- Exclusão de vacina (somente o dono)
  **Solicitações de alteração (permissividade do suporte)**
- O tutor de **suporte** não altera tarefas e vacinas diretamente: ele **propõe** a mudança e o dono decide
- Três tipos de solicitação: alterar uma tarefa, adicionar uma vacina e alterar uma vacina
- A proposta usa o mesmo formato (e as mesmas validações) da operação direta do dono, então um pedido inválido já é rejeitado na criação
- O dono vê, em uma única chamada, os pedidos de vínculo e as solicitações de alteração pendentes de todos os pets dele
- **Aprovar** aplica a mudança pelo mesmo caminho que o dono usaria diretamente, revalidando tudo no momento da aprovação; **recusar** aceita um motivo opcional (até 255 caracteres)
- Cada solicitação só pode ser respondida uma vez (`409` nas demais tentativas)
- Um mesmo suporte não mantém duas solicitações pendentes para a mesma tarefa ou vacina
- Se o suporte sai do pet ou é removido, as solicitações pendentes dele viram `CANCELADA`; ao excluir uma tarefa, uma vacina ou o pet, as solicitações relacionadas são apagadas junto
> **Notificações** (por exemplo, avisar o dono de um pedido novo ou lembrar de uma vacina próxima) ficam por responsabilidade do aplicativo Android, que consome a API e usa campos como `dataProximaDose`, `statusVacina` e a lista de pendentes.
 
---

## 🧱 Stack

| Camada | Tecnologia |
| --- | --- |
| Linguagem | Java 21 |
| Framework | Spring Boot 4.1.1 |
| Módulos | Spring Web MVC, Spring Data JPA, Spring Security, Bean Validation, Jackson 3 |
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
├── config/       # SecurityConfig
├── controller/   # endpoints REST
├── service/      # regras de negócio
├── repository/   # acesso a dados (Spring Data JPA)
├── model/        # entidades JPA e enums
├── dto/          # records de entrada e saída
├── exception/    # exceções de domínio e GlobalExceptionHandler
└── security/     # TokenAuthFilter
```

Principais decisões:

- **DTOs em vez de entidades** na entrada e na saída, evitando mass assignment e vazamento de dados.
- **Fotos como Base64 no JSON** (sem multipart) e armazenadas como `MEDIUMBLOB`. O DTO leve (listagens) não leva foto; o DTO de perfil leva.
- **Enums** (`Papel`, `StatusDeVinculo`, `Porte`, `Sexo`, `StatusVacina`, `IntervaloUnidade`, `TipoAlteracao`, `StatusSolicitacao`) gravados como `String` no banco.
- **Controle de acesso mascarado**: quem não tem vínculo aceito com um recurso recebe o mesmo `404` de um recurso inexistente, sem revelar que ele existe. Isso vale também para as solicitações de alteração e para a remoção de tutores.
- **Verificação de vínculo centralizada**: o `VinculoAcessoService` concentra a regra "vínculo aceito" e "papel de dono", usada pelos services de pets, tarefas, vacinas e solicitações. Cada recurso informa a exceção a lançar, para que o `404` mascarado fale do seu próprio recurso.
- **Alterações completas (`PUT`)**: tarefas e vacinas são atualizadas reenviando todos os campos; a hora da tarefa vazia significa "sem hora".
- **Próxima dose calculada pelo servidor**: a vacina guarda o intervalo (quantidade + unidade) e o service calcula a `dataProximaDose` sempre que a vacina fica ou está `APLICADA` (no cadastro com `jaAplicada`, ao aplicar ou ao alterar); em vacina `PENDENTE` ou de dose única ela fica vazia, e o cliente nunca envia essa data.
- **Status da vacina derivado**: o banco guarda só `PENDENTE` e `APLICADA`; `ATRASADA` é calculada ao montar o DTO de resposta (campo `statusVacina`), sem alterar a entidade.
- **Validações da vacina em um só lugar**: as regras que dependem apenas do corpo ficam no `VacinaRequestDTO.validarRegras()`, e `VacinaService.validarAtualizacao` soma a regra de estado (vacina aplicada não volta a pendente). O cadastro direto, a criação da solicitação e a aprovação usam o mesmo código.
- **Solicitações de alteração como dado genérico**: uma única entidade (`SolicitacaoAlteracao`) guarda o tipo, o alvo e os dados propostos como **JSON**. Na aprovação, os dados são desserializados e entregues aos métodos públicos de `TarefaService` e `VacinaService` com o dono como usuário, sem duplicar regra de negócio. Se qualquer regra falhar, a transação é desfeita e a solicitação continua pendente.
- **Resposta concorrente protegida**: aprovar e recusar buscam a solicitação com bloqueio pessimista, para dois cliques seguidos não aplicarem a mesma mudança duas vezes.
- **Dependências em um só sentido**: `SolicitacaoAlteracaoService` depende de `TarefaService` e `VacinaService`, e nunca o contrário; quando estes precisam apagar solicitações, usam o repository direto.
- **Fuso horário explícito**: datas "de hoje" usam `America/Sao_Paulo` em cada `now()`, independentemente do fuso do servidor.
### Modelo de dados (resumo)

```
Usuario ──< VinculoPet >── Pet ──< Tarefa
   │            │            │        │
   │    papel: DONO | SUPORTE │        └── executadoPor → Usuario
   │    status: PENDENTE | ACEITO | RECUSADO
   │                         ├──< Vacina
   │                         └──< SolicitacaoAlteracao ── solicitante → Usuario
   └──< Sessao                      (respondidoPor → Usuario)
```

- **Usuario**: nome, e-mail, telefone, senha, apelido, foto
- **Pet**: nome, raça, espécie, porte, peso, sexo, data de nascimento, perfil de sensibilidade, foto, código de vínculo, personalidades
- **VinculoPet**: relação muitos-para-muitos entre usuário e pet, com papel, status e data de criação
- **Sessao**: token de sessão de cada login
- **Tarefa**: título, descrição, data, hora, concluída, quem executou, data de conclusão
- **Vacina**: nome, data de aplicação (prevista enquanto `PENDENTE`, real quando `APLICADA`), dose única, intervalo de revacinação (quantidade + unidade), data da próxima dose (calculada), status (`PENDENTE` ou `APLICADA`), observação (opcional, até 500 caracteres)
- **SolicitacaoAlteracao**: pet, solicitante, tipo (`ALTERAR_TAREFA`, `ALTERAR_VACINA` ou `ADICIONAR_VACINA`), alvo (nulo ao adicionar), dados propostos (JSON), status (`PENDENTE`, `APROVADA`, `RECUSADA` ou `CANCELADA`), datas de criação e resposta, quem respondeu e motivo da recusa
---

## 🔐 Autenticação e autorização

Autenticação por **token opaco de sessão salvo no banco** (não é JWT):

1. O cadastro e o login retornam um token.
2. Nas demais requisições, envie `Authorization: Bearer <token>`.
3. O `TokenAuthFilter` busca a sessão pelo token e coloca o usuário no `SecurityContext`; os controllers o recebem com `@AuthenticationPrincipal`.
4. O token **não expira sozinho**: o app é de uso exclusivo no celular e a sessão só termina com logout manual (ou, nas outras sessões, quando a senha é alterada).
5. Um mesmo usuário pode ter várias sessões válidas ao mesmo tempo.
   Rotas públicas: `POST /api/usuarios/cadastrar` e `POST /api/login`. Todo o resto exige token; sem ele a resposta é `403`.

**Regras por papel**

| Ação | DONO | SUPORTE (aceito) | Sem vínculo / pendente |
| --- | :---: | :---: | :---: |
| Ver perfil do pet | ✅ | ✅ | ❌ (404) |
| Editar dados básicos do pet | ✅ | ✅ | ❌ (404) |
| Editar sensibilidade e personalidades | ✅ | ❌ (403) | ❌ (404) |
| Atualizar foto do pet | ✅ | ❌ (403) | ❌ (404) |
| Excluir pet | ✅ | ❌ (403) | ❌ (404) |
| Ver agenda do pet | ✅ | ✅ | ❌ (404) |
| Criar tarefa | ✅ | ✅ | ❌ (404) |
| Concluir tarefa | ✅ | ✅ | ❌ (404) |
| Alterar tarefa | ✅ | ❌ (403)¹ | ❌ (404) |
| Excluir tarefa | ✅ | ❌ (403) | ❌ (404) |
| Ver vacinas do pet | ✅ | ✅ | ❌ (404) |
| Registrar e alterar vacina | ✅ | ❌ (403)¹ | ❌ (404) |
| Excluir vacina | ✅ | ❌ (403) | ❌ (404) |
| Marcar vacina como aplicada | ✅ | ✅ | ❌ (404) |
| Listar tutores do pet | ✅ | ✅ | ❌ (404) |
| Remover tutor | ✅ | ❌ (403) | ❌ (404) |
| Sair do pet | ❌ (403) | ✅ | ❌ (404) |
| Ver e processar solicitações de vínculo | ✅ | ❌ (403) | ❌ (404) |
| Solicitar alteração de tarefa ou vacina | ❌ (403)² | ✅ | ❌ (404) |
| Ver pedidos pendentes (`/api/solicitacoes/pendentes`) | ✅ | lista vazia | lista vazia |
| Aprovar ou recusar solicitação de alteração | ✅ | ❌ (404) | ❌ (404) |

¹ O suporte não altera diretamente: ele envia uma **solicitação de alteração** ao dono.
² O dono altera diretamente, sem precisar solicitar.
 
---

## 📡 Endpoints

Todos os endpoints (exceto os públicos) exigem `Authorization: Bearer <token>`.

### Usuários e autenticação

| Método | Rota | Descrição |
| --- | --- | --- |
| `POST` | `/api/usuarios/cadastrar` | Cadastra o usuário e já retorna o token (público) |
| `POST` | `/api/login` | Autentica por e-mail e senha e retorna o token (público) |
| `POST` | `/api/logout` | Encerra a sessão atual (`204`) |
| `GET` | `/api/usuarios/perfil` | Perfil do usuário autenticado (com foto) |
| `PUT` | `/api/usuarios/atualizarPerfil` | Atualiza apelido e foto (Base64) |
| `PUT` | `/api/usuarios/dados` | Altera nome, e-mail e telefone (`409` se já em uso por outro usuário) |
| `PUT` | `/api/usuarios/senha` | Altera a senha (exige a senha atual) e encerra as outras sessões (`204`) |

### Pets e vínculos

| Método | Rota | Descrição |
| --- | --- | --- |
| `POST` | `/api/pets/cadastrar` | Cadastra um pet; o criador vira DONO e a resposta traz o código de vínculo |
| `GET` | `/api/pets/meusPets` | Lista os pets vinculados ao usuário (id, nome, papel e foto) |
| `GET` | `/api/pets/{id}/perfil` | Perfil completo do pet |
| `PUT` | `/api/pets/{idPet}/dados` | Atualiza os dados básicos do pet |
| `PUT` | `/api/pets/{id}/atualizarPetPerfil` | Atualiza perfil de sensibilidade e personalidades (somente DONO) |
| `PUT` | `/api/pets/{id}/foto` | Atualiza a foto do pet (somente DONO, máx. 5 MB) |
| `DELETE` | `/api/pets/{idPet}` | Exclui o pet e tudo que depende dele (somente DONO, `204`) |
| `POST` | `/api/pets/solicitarVinculo` | Solicita vínculo como suporte usando o código do pet |
| `GET` | `/api/pets/listarSolicitacoes` | Lista as solicitações de vínculo pendentes (somente DONO) |
| `POST` | `/api/pets/processarSolicitacao` | Aceita ou recusa uma solicitação de vínculo (somente DONO) |
| `GET` | `/api/pets/{idPet}/tutores` | Lista os tutores com vínculo aceito (qualquer vínculo aceito) |
| `DELETE` | `/api/pets/{idPet}/tutores/{idUsuario}` | Remove um tutor de suporte (somente DONO, `204`) |
| `DELETE` | `/api/pets/{idPet}/vinculo` | O suporte sai do pet (`204`; o dono recebe `403`) |

### Agenda

| Método | Rota | Descrição |
| --- | --- | --- |
| `POST` | `/api/pets/{id}/tarefas` | Cria uma tarefa para o pet (DONO e SUPORTE) |
| `GET` | `/api/pets/{id}/tarefas` | Lista as tarefas de um pet |
| `GET` | `/api/tarefas?data=yyyy-MM-dd` | Lista as tarefas do dia de todos os pets vinculados |
| `PUT` | `/api/tarefas/{idTarefa}` | Altera a tarefa (somente DONO, e apenas se ainda não concluída) |
| `PATCH` | `/api/tarefas/{idTarefa}/concluir` | Marca a tarefa como concluída (qualquer vínculo aceito) |
| `DELETE` | `/api/tarefas/{idTarefa}` | Exclui a tarefa (somente DONO, `204`) |

### Vacinas

| Método | Rota | Descrição |
| --- | --- | --- |
| `POST` | `/api/pets/{idPet}/vacinas` | Registra uma vacina para o pet, como `APLICADA` ou `PENDENTE` conforme `jaAplicada` (somente DONO) |
| `GET` | `/api/pets/{idPet}/vacinas` | Lista as vacinas do pet, da aplicação mais recente para a mais antiga, com `statusVacina` |
| `PUT` | `/api/vacinas/{idVacina}` | Altera uma vacina (somente DONO); `APLICADA` não volta a `PENDENTE` (`400`) |
| `PATCH` | `/api/vacinas/{idVacina}/aplicar` | Marca a vacina pendente como aplicada hoje e calcula a próxima dose (qualquer vínculo aceito; `409` se já aplicada) |
| `DELETE` | `/api/vacinas/{idVacina}` | Exclui a vacina (somente DONO, `204`) |

### Solicitações de alteração

| Método | Rota | Descrição |
| --- | --- | --- |
| `GET` | `/api/solicitacoes/pendentes` | Pedidos pendentes dos pets em que o usuário é dono, em duas listas: `vinculos` e `alteracoes` |
| `POST` | `/api/solicitacoes/tarefas/{idTarefa}` | O suporte propõe a alteração de uma tarefa (corpo igual ao do `PUT` da tarefa; `201`) |
| `POST` | `/api/solicitacoes/pets/{idPet}/vacinas` | O suporte propõe adicionar uma vacina (corpo igual ao do cadastro; `201`) |
| `POST` | `/api/solicitacoes/vacinas/{idVacina}` | O suporte propõe a alteração de uma vacina (corpo igual ao do `PUT` da vacina; `201`) |
| `PATCH` | `/api/solicitacoes/{idSolicitacao}/aprovar` | O dono aprova e a mudança é aplicada (sem corpo) |
| `PATCH` | `/api/solicitacoes/{idSolicitacao}/recusar` | O dono recusa; o corpo `{"motivo": "..."}` é opcional |

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
  -d '{"nome":"V10","dataAplicacao":"2026-10-01","jaAplicada":true,"dosagemUnica":false,"intervaloQuantidade":1,"intervaloUnidade":"ANOS","observacao":"Reforço anual"}'
 
# Marcar uma vacina pendente como aplicada (data de aplicação = hoje, próxima dose calculada)
curl -X PATCH http://localhost:8080/api/vacinas/1/aplicar \
  -H "Authorization: Bearer <token>"
 
# Suporte propõe alterar uma tarefa (o dono precisa aprovar)
curl -X POST http://localhost:8080/api/solicitacoes/tarefas/7 \
  -H "Authorization: Bearer <token-do-suporte>" \
  -H "Content-Type: application/json" \
  -d '{"titulo":"Passeio","descricao":"Levar a bolinha","dataTarefa":"2026-11-20","hora":"08:30"}'
 
# Dono vê o que está pendente (vínculos e alterações)
curl http://localhost:8080/api/solicitacoes/pendentes \
  -H "Authorization: Bearer <token-do-dono>"
 
# Dono aprova (ou recusa, com motivo opcional)
curl -X PATCH http://localhost:8080/api/solicitacoes/12/aprovar \
  -H "Authorization: Bearer <token-do-dono>"
 
curl -X PATCH http://localhost:8080/api/solicitacoes/12/recusar \
  -H "Authorization: Bearer <token-do-dono>" \
  -H "Content-Type: application/json" \
  -d '{"motivo":"Prefiro manter o horário"}'
```

### Regras de validação

- **Vacina**: `nome`, `dataAplicacao`, `jaAplicada` e `dosagemUnica` são obrigatórios; se `jaAplicada` for `true`, a data não pode ser futura; vacina de dose única não aceita intervalo; vacina que não é dose única exige intervalo (quantidade e unidade) com quantidade maior que zero; a observação aceita até 500 caracteres. Uma vacina já aplicada não pode ser aplicada novamente nem voltar a `PENDENTE`.
- **Tarefa**: `titulo` e `dataTarefa` são obrigatórios; uma tarefa já concluída não pode ser alterada nem concluída novamente.
- **Conta**: `nome`, `email` e `telefone` (mínimo de 6 caracteres) são obrigatórios ao alterar os dados; a nova senha precisa ser diferente da atual.
- **Solicitação de alteração**: o corpo passa pelas mesmas validações da operação direta; o motivo da recusa aceita até 255 caracteres; não pode haver duas solicitações pendentes do mesmo suporte para o mesmo alvo.
### Erros

Tratados de forma central no `GlobalExceptionHandler`:

| Status | Quando |
| --- | --- |
| `400` | Validação de campos, datas inválidas, intervalo de vacina inválido, senha atual incorreta, foto inválida ou grande demais |
| `401` | Credenciais inválidas no login |
| `403` | Sem token ou sem permissão para a ação |
| `404` | Pet, tarefa, vacina, tutor ou solicitação não encontrados (ou sem acesso a eles) |
| `409` | Conflito, como e-mail ou telefone já cadastrado, solicitação duplicada ou já respondida, tarefa já concluída ou vacina já aplicada |
 
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
- No MySQL, o Hibernate cria os enums como `enum(...)` na coluna. O `ddl-auto=update` **não** acrescenta valores a um enum de uma tabela que já existe: ao incluir um valor novo (por exemplo `CANCELADA` em `solicitacao_alteracao.status`), é preciso rodar um `ALTER TABLE ... MODIFY` no banco local e na Aiven antes do deploy.
- A tabela `vacina` mudou de estrutura (status, dose única e intervalo no lugar de data da próxima dose informada). Como o `ddl-auto=update` não remove nem converte colunas antigas, a tabela foi recriada (DROP e nova criação pelo Hibernate) no banco local e na Aiven, o que foi seguro por ainda não haver registros de vacina.
- A tabela `solicitacao_alteracao` segue o mesmo cuidado: ela só é criada corretamente se nunca existiu com uma versão antiga dos enums `tipo` e `status`.
- Os controllers recebem o usuário autenticado com `@AuthenticationPrincipal Usuario`, sem acessar o `SecurityContextHolder` diretamente.
- Deletes derivados do Spring Data (`deleteByPet`, `deleteByTipoAndIdAlvo`) exigem transação ativa; por isso os métodos que os chamam são `@Transactional`.
---

## 🗺️ Roadmap

- [x] Cadastro, login e sessão por token
- [x] Perfil do usuário (apelido e foto)
- [x] Cadastro e perfil do pet
- [x] Vínculo por código (dono e tutor de suporte)
- [x] Agenda: criar, listar, alterar e concluir tarefas
- [x] Módulo de vacinas (registrar, listar e alterar)
- [x] Rodada de testes consolidada e deploy para integração com o app Android
- [x] Revisão de atomicidade (`@Transactional`) no cadastro de usuário
- [x] Padronizar o nome dos DTOs em `Request`/`Response`
- [x] Logout, alteração de dados da conta e alteração de senha
- [x] Exclusão de pet, tarefa e vacina
- [x] Listar tutores, remover tutor e sair do pet
- [x] Solicitação de alteração: o suporte propõe a mudança e o dono aprova ou recusa (tarefas e vacinas)
---

## 👤 Autor

Desenvolvido por [@justletthefatherlead](https://github.com/justletthefatherlead).
 
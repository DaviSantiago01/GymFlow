# Treino Conecta — Reservas de armários

Implementação de F001 sobre o projeto D5 fornecido, preservando os seis módulos
Maven, Spring Boot, Angular, autenticação por sessão e convenções de domínio.
O projeto original foi extraído de `treinoconectacore.zip`; `GymFlow` não foi alterado.

## Executar

Requisitos: **JDK 25**, Maven 3.9+, Node 24 e npm. As versões efetivamente usadas
foram JDK 25.0.5, Maven 3.9.16 e Node 24.16.0. O Java 24 que estava no PATH não
atende ao projeto. Configure `JAVA_HOME` para o JDK 25 antes de chamar Maven/Java.

Na pasta `treinoconectacore`, em PowerShell:

```powershell
# Opção para este workspace: JDK portátil baixado e verificado durante a implementação.
$env:JAVA_HOME = (Resolve-Path '..\.tools\java25\jdk-25.0.5+8').Path
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

mvn clean package
java -jar ui-backend/target/treinoconectacore-ui-backend-1.0.0-SNAPSHOT.jar --spring.profiles.active=desenvolvimento
```

Em outro terminal:

```powershell
cd ui-frontend/src/main/angular
npm ci
npm start -- --host 127.0.0.1
```

Abra **http://127.0.0.1:4200**. A API escuta em **http://localhost:8080**.
O proxy Angular mantém as chamadas na mesma origem, incluindo sessão e CSRF.
Após o login, `/inicio` apresenta uma tela minimalista com atalhos para os recursos
existentes e menu lateral. **Marcar aula** e **Grupos de treino** são itens
ilustrativos desabilitados. Armários, reserva ativa e histórico continuam funcionais.
No celular, o botão **Abrir menu** exibe a navegação, que fecha ao selecionar uma página.
Para criar sua própria conta, abra **http://127.0.0.1:4200/cadastro** ou clique em
**Criar minha conta** na tela de login. Preencha nome, usuário, senha e confirmação;
depois entre com a conta criada. Os dados são persistidos no mesmo banco e o novo
aluno já pode reservar armários. Consulte [as regras do cadastro](docs/core/funcionalidades/F002-cadastro.md).
Contas exclusivas de desenvolvimento: **u / u** (Mariana Costa) e **v / v**
(Pedro Silva). Seis armários A-01 a A-06 são inseridos uma única vez no banco.
Esses são registros persistidos, consumidos pelos endpoints reais.

O banco H2 em arquivo fica em `data/treino-conecta.mv.db`, relativo ao diretório
de execução do backend. Execute o JAR sempre na raiz do projeto para reutilizar os
mesmos dados. Nenhum Docker ou serviço adicional é necessário.

As migrações **Flyway são aplicadas automaticamente na inicialização**. O arquivo
`infraestrutura/src/main/resources/db/migration/V1__armarios_e_reservas.sql` cria
as tabelas e restrições; Hibernate apenas valida o esquema (`ddl-auto=validate`).
Para aplicar as migrações, execute o backend com o comando acima. Não é necessário
um comando Flyway separado nem alteração manual de esquema.

Durante a validação foi utilizado um cache Maven isolado no workspace. Para
reutilizá-lo neste computador, acrescente `"-Dmaven.repo.local=../.m2/repository"`
aos comandos Maven executados na raiz. Em outra máquina, o repositório padrão funciona.

## Arquitetura e modelo

| Módulo | Implementação |
|---|---|
| `dominio-compartilhado` | Papéis existentes do D5, preservados |
| `dominio-core` | Agregados `Aluno`, `Armario`, `ReservaArmario`, identificadores, serviços e SPIs |
| `aplicacao` | Projeções, consultas paginadas, serviços CQRS e respostas com horário do servidor |
| `infraestrutura` | Entidades espelho JPA, repositórios, mapeamento explícito, Flyway e agendamento |
| `ui-backend` | Controladores D5, comandos específicos, sessão/CSRF e credenciais persistidas |
| `ui-frontend` | Angular 22, Bootstrap, resolvedores, filtros, cinco telas e testes de navegador |

`Aluno 1 → N ReservaArmario N ← 1 Armario`. No domínio as associações usam os
identificadores dos agregados; no JPA são chaves estrangeiras `ManyToOne`.
`USU_CREDENCIAL` guarda a credencial de autenticação associada ao usuário do aluno.
Disponibilidade é calculada pelas reservas e pelo prazo. O estado final já indica
o motivo de encerramento, sem um campo redundante.

O D5 é mantido como dependência `1.0.0-SNAPSHOT`, com Spring Boot 4.1.1. Serviços
e adaptadores de agregados são registrados pelo framework, sem anotações Spring.
As transações dos POSTs vêm do D5; o agendador usa `TransactionTemplate` porque
não existe uma requisição HTTP nessa execução. Os erros mantêm o contrato
`{"mensagem":"..."}` fornecido pelo D5.

## Regras atendidas

| Regra | Garantia implementada |
|---|---|
| RN01 | Bloqueio pessimista e índice único calculado por armário ativo |
| RN02 | Bloqueio do aluno e índice único calculado por aluno ativo |
| RN03 | Três horas em UTC; agendador a cada segundo; consultas e novas reservas respeitam imediatamente o vencimento |
| RN04 | Aluno obtido da sessão, conferência de propriedade, bloqueio da reserva e preservação do histórico |

Os índices H2 usam colunas **geradas pelo banco** quando a situação é ATIVA.
Não são estados de disponibilidade mantidos pela aplicação. Eles também rejeitam
inserções duplicadas feitas diretamente em SQL. Um comando usa a ordem de bloqueio
aluno → armário → reservas. Cancelamento e encerramento só alteram registros ativos.

A interface calcula o tempo restante a partir de `termino`, `horarioServidor` e
`performance.now()`, sincroniza a cada 15 segundos e ao retornar à aba, e bloqueia
ações quando a sincronização está antiga. O servidor decide todas as permissões.

## Endpoints

Os endpoints de reservas exigem autenticação; o cadastro é público. POSTs exigem `X-XSRF-TOKEN`, correspondente ao cookie
`XSRF-TOKEN`. O cliente nunca envia aluno, confirmação ou término como autoridade.

| Método | Caminho | Operação |
|---|---|---|
| GET | `/backend/autenticacao` | Login HTTP Basic e recuperação da sessão |
| POST | `/backend/sair` | Encerrar sessão |
| GET | `/backend/cadastro` | Preparar cadastro público e cookie CSRF |
| POST | `/backend/cadastro` | Criar aluno e credencial, exigindo CSRF |
| GET | `/backend/core/armario/pesquisa` | Catálogo e disponibilidade, consulta D5 paginada |
| GET | `/backend/core/armario/{codigo}/visualizacao` | Detalhes e disponibilidade |
| POST | `/backend/core/armario/{codigo}/reservar` | Confirmar reserva imediatamente |
| GET | `/backend/core/reserva-armario/ativa` | Reserva ativa do aluno ou `dados: null` |
| GET | `/backend/core/reserva-armario/pesquisa` | Histórico do aluno, paginado |
| GET | `/backend/core/reserva-armario/{id}/confirmacao` | Tela de confirmação |
| GET | `/backend/core/reserva-armario/{id}/visualizacao` | Detalhes da própria reserva |
| POST | `/backend/core/reserva-armario/{id}/cancelar` | Cancelar e liberar |
| POST | `/backend/core/reserva-armario/{id}/encerrar` | Encerrar uso e liberar |

Os controles de inclusão/alteração/exclusão genéricos herdados do D5 permanecem
sem autorização de escrita. Assim, não permitem alterar prazos, proprietários ou
apagar histórico por uma rota alternativa. Veja [exemplos HTTP](docs/core/api-armarios.md).

## Testes e compilação

```powershell
# Na raiz, com Java 25 configurado
mvn test
mvn package

# Na pasta ui-frontend/src/main/angular
npm test -- --watch=false
npm run build
# Backend em desenvolvimento e frontend em execução; requer Microsoft Edge instalado
npm run test:e2e
```

Os testes Java usam H2 isolado em memória e as migrações reais. Cobrem todos os
12 casos solicitados, incluindo concorrência em conexões/transações separadas,
restrições diretas do banco, rollback, CSRF, login real e o agendador sem HTTP.
Os testes de navegador criam reservas nas contas de desenvolvimento e as finalizam;
o histórico desses testes permanece no banco de desenvolvimento.

O [relatório de validação](docs/validacao/RESULTADOS.md) contém resultados, evidências
e avisos de compilação. A [lista de arquivos](docs/ARQUIVOS.md) compara a entrega
com o ZIP original. Há capturas das cinco telas e da versão para celular em `docs/validacao`.

## Produção e limites

O perfil `producao` exige `TREINO_BANCO_URL`, `TREINO_BANCO_USUARIO` e
`TREINO_BANCO_SENHA`, ativa cookies Secure e deve ser servido por HTTPS.
As contas de demonstração não são criadas nesse perfil, mesmo que desenvolvimento
seja ativado junto. Os alunos podem criar suas próprias contas pela tela de cadastro;
o inventário de armários deve ser provisionado pela academia. Não existe tela de
administração de usuários neste escopo.

Foi validado H2, o banco fornecido pelo D5. As colunas geradas da migração são
específicas desse banco; uma troca de SGBD requer migração equivalente e novos testes
de concorrência. No H2 em arquivo usa-se uma instância da aplicação por arquivo.
O frontend de produção precisa servir o bundle Angular e encaminhar `/backend`
para o Spring Boot; publicação e infraestrutura de hospedagem não foram solicitadas.

O vencimento lógico é exato; sua gravação automática pode ocorrer no ciclo seguinte
do agendador (normalmente até um segundo). Durante uma parada da aplicação não há
processo em execução; na retomada o agendador recupera vencimentos pendentes.

PIN, sensores e abertura física ilustrados no PDF dependem de equipamento e estão
fora das quatro regras solicitadas. A reserva é imediata, sem agenda futura.

Referências técnicas consultadas: [fontes D5 incluídas](.agents/d5/framework),
[agendamento Spring](https://docs.spring.io/spring-framework/reference/integration/scheduling.html),
[H2](https://h2database.com/html/advanced.html), [HTTP Angular](https://angular.dev/guide/http/making-requests)
e [sintaxe Context Mapper](https://contextmapper.org/docs/tactic-ddd/).

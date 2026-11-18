# Treino Conecta — Armários, avaliações e grupos de treino

Implementação de reservas de armários, cadastro, avaliações e grupos de treino sobre o projeto D5 fornecido, preservando os seis módulos
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
existentes e menu lateral. **Marcar aula** permanece ilustrativo. **Grupos de treino**
permite criar grupos, convidar colegas, propor encontros, responder presença e consultar histórico.
Veja o [relatório dos grupos](docs/validacao/GRUPOS.md) e o [contrato HTTP](docs/core/api-grupos.md).
Armários, reserva ativa e histórico continuam funcionais.
A aba **Avaliações** abre o histórico de treinos com data, horário, professor e
Avaliar treino. Há uma avaliação por treino concluído do aluno, com edição,
exclusão, médias e avaliações recentes calculadas no banco. Consulte a
[atualização do histórico](docs/validacao/TREINOS.md).
Minha reserva e Histórico de reservas ficam somente dentro da área **Armários**.
Veja o [relatório completo das avaliações](docs/validacao/AVALIACOES.md) e o
[contrato HTTP](docs/core/api-avaliacoes.md).
No celular, o botão **Abrir menu** exibe a navegação, que fecha ao selecionar uma página.
Para criar sua própria conta, abra **http://127.0.0.1:4200/cadastro** ou clique em
**Criar minha conta** na tela de login. Preencha nome, usuário, senha e confirmação;
depois entre com a conta criada. Os dados são persistidos no mesmo banco e o novo
aluno já pode reservar armários, participar de grupos e avaliar seus treinos concluídos. Consulte [as regras do cadastro](docs/core/funcionalidades/F002-cadastro.md).
Contas exclusivas de desenvolvimento: **u / u** (Mariana Costa) e **v / v**
(Pedro Silva). Seis armários A-01 a A-06 são inseridos uma única vez no banco.
Esses são registros persistidos, consumidos pelos endpoints reais.
Há três professores identificados como **Perfil de demonstração** apenas em
desenvolvimento. Não são criadas avaliações ou médias fictícias.

O banco H2 em arquivo fica em `data/treino-conecta.mv.db`, relativo ao diretório
de execução do backend. Execute o JAR sempre na raiz do projeto para reutilizar os
mesmos dados. Nenhum Docker ou serviço adicional é necessário.

As migrações **Flyway são aplicadas automaticamente na inicialização**. O arquivo
`infraestrutura/src/main/resources/db/migration/V1__armarios_e_reservas.sql` cria
as tabelas e restrições. `V2__reservas_por_horario.sql` adiciona o início agendado,
preserva reservas antigas e permite diferentes períodos no mesmo armário.
`V4__avaliacoes_professores.sql` acrescenta professores, avaliações e o registro
mínimo do par aluno-professor, sem modificar os dados das reservas.
`V5__avaliacoes_por_treino.sql` vincula avaliações ao treino;
`V6__grupos_treino.sql` acrescenta academia, grupos, participações, convites e encontros, preservando os dados existentes.
Hibernate apenas valida o esquema (`ddl-auto=validate`).
Para aplicar as migrações, execute o backend com o comando acima. Não é necessário
um comando Flyway separado nem alteração manual de esquema.

Durante a validação foi utilizado um cache Maven isolado no workspace. Para
reutilizá-lo neste computador, acrescente `"-Dmaven.repo.local=../.m2/repository"`
aos comandos Maven executados na raiz. Em outra máquina, o repositório padrão funciona.

## Arquitetura e modelo

| Módulo | Implementação |
|---|---|
| `dominio-compartilhado` | Papéis existentes do D5, preservados |
| `dominio-core` | Agregados `Aluno`, `Armario`, `ReservaArmario`, `Professor`, `AvaliacaoProfessor`, `Treino`, `Academia`, `GrupoTreino`, identificadores, serviços e SPIs |
| `aplicacao` | Projeções, consultas paginadas, serviços CQRS e respostas com horário do servidor |
| `infraestrutura` | Entidades espelho JPA, repositórios, mapeamento explícito, Flyway e agendamento |
| `ui-backend` | Controladores D5, comandos específicos, sessão/CSRF e credenciais persistidas |
| `ui-frontend` | Angular 22, Bootstrap, resolvedores, filtros, telas de armários/avaliações/grupos e testes de navegador |

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
| RN01 | Bloqueio pessimista do armário e rejeição de períodos sobrepostos |
| RN02 | Bloqueio do aluno e índice único calculado por aluno ativo |
| RN03 | Data, horas inteiras e duração de 1 a 3 horas; todos os dias das 06h às 22h em America/Sao_Paulo |
| RN04 | Confirmação e início distintos; vencimento no término escolhido, inclusive após parada |
| RN05 | Propriedade, cancelamento antes do início, encerramento durante uso e histórico preservado |

O índice único de aluno usa coluna gerada quando a situação é ATIVA, abrangendo
reservas em uso ou agendadas. Um comando usa a ordem de bloqueio aluno → armário →
reservas e reconsulta sobreposições dentro da transação. Intervalos são [início, término):
um pode começar exatamente quando outro termina. O índice único global por armário
foi removido; a garantia de não sobreposição depende dos comandos transacionais.
Escritas administrativas diretas devem respeitar essa mesma regra.

Na tela de detalhes, escolha a data, um bloco inicial e uma duração disponível.
O servidor rejeita passado, horas fracionadas, duração fora de 1–3h, períodos que
ultrapassem 22h e qualquer sobreposição. Continua permitido um único agendamento
ou reserva em uso por aluno. Antes do início é possível cancelar; encerrar uso só
fica disponível a partir do início. Uma mudança de disponibilidade invalida a
seleção afetada e pede que o aluno escolha novamente.

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
| GET | `/backend/core/armario/{codigo}/horarios?data=AAAA-MM-DD` | Blocos de 1h e durações consecutivas disponíveis |
| POST | `/backend/core/armario/{codigo}/reservar` | Agendar com corpo `{data, hora, duracaoHoras}` |
| GET | `/backend/core/reserva-armario/ativa` | Reserva em uso ou agendada do aluno, ou `dados: null` |
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

A validação atual está em [GRUPOS.md](docs/validacao/GRUPOS.md):
180 testes Java, 15 Angular e 11 fluxos de navegador aprovados.

Os testes Java usam H2 isolado em memória e as migrações reais. Cobrem todos os
casos solicitados nas três funcionalidades, incluindo concorrência em conexões/transações separadas,
restrições diretas do banco, rollback, CSRF, login real e o agendador sem HTTP.
Os testes de navegador criam contas próprias e reservas futuras e as cancelam;
o histórico desses testes permanece no banco de desenvolvimento.

O [relatório de validação](docs/validacao/RESULTADOS.md) contém resultados, evidências
e avisos de compilação. As capturas estão em `docs/validacao`.

## Produção e limites

O perfil `producao` exige `TREINO_BANCO_URL`, `TREINO_BANCO_USUARIO` e
`TREINO_BANCO_SENHA`, ativa cookies Secure e deve ser servido por HTTPS.
As contas de demonstração não são criadas nesse perfil, mesmo que desenvolvimento
seja ativado junto. Os alunos podem criar suas próprias contas pela tela de cadastro;
o inventário de armários e os perfis reais dos professores devem ser provisionados pela academia. Não existe tela de
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
fora das quatro regras solicitadas. As reservas permitem escolher data, horário e duração de 1 a 3 horas.

Referências técnicas consultadas: [fontes D5 incluídas](.agents/d5/framework),
[agendamento Spring](https://docs.spring.io/spring-framework/reference/integration/scheduling.html),
[H2](https://h2database.com/html/advanced.html), [HTTP Angular](https://angular.dev/guide/http/making-requests)
e [sintaxe Context Mapper](https://contextmapper.org/docs/tactic-ddd/).

## Regra de avaliação e exclusão

Cada aluno mantém uma avaliação por treino concluído, com cinco notas inteiras de 0 a 5
e comentário opcional de até 500 caracteres. Somente o autor edita ou exclui.
As médias são calculadas dos registros existentes; sem registros, aparece
“Sem avaliações”. As dez recentes são ordenadas pela criação.

A divergência da RN03 foi documentada em
[F003-avaliacoes.md](docs/core/funcionalidades/F003-avaliacoes.md): foi seguida a
regra esclarecida pelo usuário: uma avaliação por treino, inclusive quando o
professor é o mesmo em outros treinos. A V5 substitui a decisão inicial da V4.
Após exclusão, notas e comentário são apagados, mas o vínculo com o treino é preservado para
impedir recriação, conforme a interpretação conservadora explicada na interface.
O histórico é a entrada das avaliações; avaliar diretamente pelo professor não é permitido.
As contas u/u e v/v têm treinos de demonstração identificados. Contas novas podem
carregar os exemplos pelo botão no histórico, somente em desenvolvimento.
Treinos reais são suportados pela mesma persistência; veja
[como cadastrá-los por integração administrativa](docs/core/treinos-reais.md).

## Planos, mensalidades e profissionais (administração)

Duas funcionalidades operadas pelo novo papel de administrador, sem alterar as anteriores:
[F007 — planos e mensalidades](docs/core/funcionalidades/F007-planos-mensalidades.md) e
[F008 — professores e personal trainers](docs/core/funcionalidades/F008-profissionais.md).

- Conta de desenvolvimento **admin / admin** (somente no perfil `desenvolvimento`), com três planos e dois profissionais
  de exemplo. Em produção, os administradores são inseridos pela academia na tabela `ADM_ADMINISTRADOR`, com senha
  codificada pelo `PasswordEncoder` do Spring.
- O menu lateral do administrador mostra **Planos**, **Adesões**, **Mensalidades** e **Profissionais**. O aluno ganha
  **Meu plano**, com o plano ativo e as próprias mensalidades.
- A migração `V7__planos_mensalidades_profissionais.sql` só cria tabelas novas.
- Testes novos: cenários Cucumber `planos-mensalidades.feature` e `profissionais.feature`, testes de repositório com as
  migrações reais e testes de integração HTTP.

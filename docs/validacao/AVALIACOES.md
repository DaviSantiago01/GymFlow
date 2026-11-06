# Entrega — Avaliações de professores

> Atualização vigente: [Histórico e avaliações por treino](TREINOS.md). A regra e o acesso
> direto por professor descritos na entrega anterior foram substituídos pelo esclarecimento do usuário.


Validação local em 05/11/2026. Implementação na pasta `treinoconectacore`, mantendo
os seis módulos D5, autenticação, cadastro e reservas. A funcionalidade 2 do PDF
recebe o código F003 porque F002 já identifica o cadastro de alunos.

## 1. Implementação

Foi adicionada a opção **Avaliações** ao menu lateral autenticado e aos atalhos
da página inicial. O fluxo inclui Minhas avaliações, Quadro de professores com
busca/paginação, Perfil com médias e dez avaliações recentes, formulário de
criação/edição e Minha avaliação com exclusão confirmada. Os cinco critérios usam
seletores acessíveis de 0 a 5; zero é diferente de campo não preenchido. Há contador
do comentário, erros por campo, confirmação de sucesso e bloqueio durante envio.
Erros recebem foco e deixam de aparecer quando o aluno corrige o campo.

As telas seguem o visual escuro e verde dos protótipos das páginas 16–21 e do
projeto existente. O menu fecha ao navegar no celular. As páginas abrem no topo;
médias são exibidas com uma casa decimal e separador em pt-BR. As páginas 10–21
foram lidas, incluindo os protótipos. A extração textual está em
`docs/referencia/paginas-10-21.txt`; o PDF permanece como referência definitiva.

## 2. Arquivos criados e modificados

A lista completa, com caminhos relativos, está em
[ARQUIVOS-AVALIACOES.md](../ARQUIVOS-AVALIACOES.md).

| Área | Principais alterações |
|---|---|
| `dominio-core/.../core/professor` | Professor, identificador, serviço e SPI |
| `dominio-core/.../core/avaliacaoprofessor` | Avaliação, notas, autoria, datas, serviço e SPI |
| `aplicacao/.../core` | Consultas paginadas, projeções, estatísticas e serviços CQRS |
| `infraestrutura/.../core` | JPA, repositórios, agregações, restrições e bloqueio |
| `infraestrutura/src/main/resources/db/migration` | V3 cria professores, avaliações e registro mínimo do par |
| `ui-backend/.../core` | Controllers D5, DTO validado e erros por campo |
| `ui-backend/.../seguranca` | Permissões no login e perfis de demonstração somente em desenvolvimento |
| `ui-frontend/src/main/angular/src/app/avaliacoes` | Serviço HTTP, telas e componente reutilizável de critérios |
| `ui-frontend/.../app.*`, `inicio` | Rotas protegidas, menu, atalho, localização e navegação |
| Testes e documentação | Cucumber, integração real, Vitest, Playwright, CML, DBML e evidências |

## 3. Entidades e relacionamentos

`Aluno 1 → N AvaliacaoProfessor N ← 1 Professor`.
O Aluno e suas credenciais foram reutilizados. O domínio guarda IDs de agregados;
no JPA há associações ManyToOne LAZY e FKs. Não é carregada uma coleção de todas
as avaliações em cada aluno ou professor; as consultas usam projeções paginadas.

`NotasAvaliacao` valida as cinco notas. `AvaliacaoProfessor` controla comentário,
datas e autoria, preservando criação/professor na edição. `Professor` conserva
nome, especialidade, biografia e a identificação de perfil de demonstração.

VAP_AVALIACAO_REALIZADA registra exclusivamente o par aluno-professor. O conteúdo
fica em AVP_AVALIACAO_PROFESSOR, com UNIQUE no par, CHECKs nas notas e datas e FKs.
O vínculo mínimo continua após exclusão física; não participa das estatísticas.
Índices apoiam recentes por professor, avaliações por autor e quadro de professores.
Nenhuma média é redundante ou persistida.

## 4. Endpoints

O contrato completo, corpos, respostas, paginação e códigos HTTP estão em
[api-avaliacoes.md](../core/api-avaliacoes.md). As bases são
`/backend/core/professor` e `/backend/core/avaliacao-professor`.

- Professores: GET `pesquisa`, `{id}/visualizacao`, `{id}/avaliacao`,
  `{id}/estatistica`, `{id}/recentes`, `{id}/minha-avaliacao`; POST `{id}/avaliar`.
- Avaliações: GET `pesquisa`, `{id}/visualizacao`, `{id}/edicao`;
  POST `{id}/editar` e `{id}/remover`.

O autor vem da sessão; alterar o corpo não transfere autoria. POSTs usam a
transação e o CSRF do D5. Alterações genéricas herdadas são bloqueadas; consultas
privadas de outro aluno não retornam a avaliação.

## 5. Regras RN01 a RN05

| Regra | Evidência da implementação |
|---|---|
| RN01 | DTO BigDecimal rejeita fracionários/ausentes; domínio aceita inteiros 0–5; CHECKs no banco |
| RN02 | Comentário opcional, até 500; validação backend, contador e feedback no frontend |
| RN03 | Verificação antes de gravar, bloqueio pessimista do aluno, UNIQUE e chave do par no banco |
| RN04 | Sessão define autor; entidade verifica autoria antes de editar/excluir; teste de falsificação do corpo |
| RN05 | SQL/JPQL calcula contagem/somas; médias em DECIMAL128, arredondamento só visual; recentes limitadas a dez por criação |

## 6. Testes e resultados

| Verificação | Resultado |
|---|---|
| Maven `package`, Java 25 | BUILD SUCCESS; 112 testes, sem falhas |
| Cucumber | 31 cenários/exemplos (8 novos de avaliações) |
| Teste unitário de domínio existente | 1 aprovado |
| Integração backend, H2 e Flyway reais | 80 testes (46 de avaliações + 34 existentes) |
| Angular/Vitest | 9 testes aprovados em 3 arquivos |
| Playwright / Edge | 8 fluxos aprovados: 5 armários, 2 avaliações, 1 cadastro |
| Ajuste final de feedback do formulário | Os 2 fluxos de avaliações repetidos e aprovados; build Angular aprovado |
| Context Mapper | Core, compartilhado e raiz: `has been validated without errors` |
| Migração local V3 | Aplicada ao banco existente; backend iniciado e testado |

Os 18 casos solicitados estão cobertos em `AvaliacoesIntegracaoTest`:

| Caso | Teste(s) |
|---|---|
| 1. Criar cinco notas válidas | `criaCincoNotasInteirasIncluindoZeroEPersiste` |
| 2–4. Menor que zero, maior que cinco, decimal | `rejeitaForaDoIntervaloEDecimalEmTodosCriterios` (15 combinações) |
| 5. Critério ausente | `rejeitaCriterioAusente` (5 critérios) e `rejeitaNotaNula` |
| 6. Comentário vazio/ausente | `aceitaComentarioAusente`, `aceitaComentarioVazio`, `aceitaComentarioNulo` |
| 7. Comentário longo | `comentarioTemLimiteExatoDe500` |
| 8. Duplicidade | `duplicadaERejeitadaMasOutroProfessorEPermitido`, `concorrenciaDoMesmoParGravaSomenteUma` |
| 9. Edição pelo autor | `autorEditaNotasERemoveComentarioPreservandoCriacao` |
| 10 e 12. Edição/exclusão por outro aluno | `outroAlunoNaoEditaNemExcluiEConsultaPrivadaNaoVaza` |
| 11. Exclusão pelo autor | `autorExcluiFisicamenteEPreservaSomenteVinculoQueBloqueiaRecriacao` |
| 13–14. Recalcular após editar/excluir | `recalculaAposEdicao`, `recalculaAposExclusao` |
| 15. Sem avaliações | `professorSemAvaliacoesTemMediasNulasEListaVazia` |
| 16–17. Dez recentes e ordenação | `dezRecentesOrdenadasPorCriacaoEEdicaoNaoMudaOrdem`, `datasIguaisSaoDesempatadasPorIdentificador` |
| 18. Média geral | `calculaMediaGeralPorCincoCriteriosSemArredondamentoPrematuro`, `zeroEUmaNotaRealEDiferenteDeSemAvaliacoes` |

Também foram verificados sessão/CSRF, pesquisa paginada, escrita genérica negada,
professor inexistente, validação na edição e restrições diretas no banco.
Os testes Java usam bancos isolados em memória. Os testes de navegador criam
contas próprias e removem suas avaliações; o par mínimo e as contas de teste
permanecem no banco de desenvolvimento. Capturas mostram dados produzidos pelo
teste identificado como **Aluno de Teste de Avaliações**, não avaliações semeadas.

Evidências visuais inspecionadas:

- [Área de avaliações](13-avaliacoes.png)
- [Quadro de professores](14-professores.png)
- [Formulário](15-formulario-avaliacao.png)
- [Minha avaliação](16-minha-avaliacao.png)
- [Perfil e médias](17-perfil-professor.png)
- [Perfil no celular](18-avaliacoes-mobile.png)

## 7. Execução

Na raiz `treinoconectacore`, com JDK 25 e Maven:

```powershell
mvn package
java -jar ui-backend/target/treinoconectacore-ui-backend-1.0.0-SNAPSHOT.jar --spring.profiles.active=desenvolvimento
```

Em outro terminal:

```powershell
cd ui-frontend/src/main/angular
npm ci
npm start -- --host 127.0.0.1
```

Acesse **http://127.0.0.1:4200**. Crie sua conta em `/cadastro` ou use as contas
de desenvolvimento **u / u** e **v / v**. Entre e clique em **Avaliações**.
O backend usa porta 8080 e o frontend usa porta 4200, com proxy da mesma origem.

Neste workspace, o JDK portátil fica em `../.tools/java25/jdk-25.0.5+8` e o cache
Maven em `../.m2/repository`. Pode-se configurar JAVA_HOME e acrescentar
`"-Dmaven.repo.local=../.m2/repository"` ao Maven. Execute o JAR na raiz para
reutilizar `data/treino-conecta.mv.db`. Flyway aplica V3 automaticamente; não
recrie o banco. Foi preservada cópia anterior em
`../.tools/treino-conecta-antes-avaliacoes-20261105.mv.db`, fora do Git.

Para repetir a validação: `mvn test`; na pasta Angular,
`npm test -- --watch=false`, `npm run build` e, com ambos os servidores rodando e
Microsoft Edge instalado, `npm run test:e2e`.

## 8. Dependências e limites

- Treino e Academia não existem no código atual. A interface informa a ausência
  do histórico, sem inventar treinos concluídos. Marcar aula e Grupos de treino
  continuam ilustrativos. Reservas de armários permanecem operacionais.
- Professor foi acrescentado no mesmo domínio core. Em desenvolvimento existem
  três perfis identificados como demonstração, sem médias/avaliações predefinidas.
  Em produção, os perfis reais devem ser provisionados pela academia na tabela
  PRO_PROFESSOR; cadastro administrativo de professores não faz parte deste escopo.
  O perfil de produção não semeia exemplos.
- Validação realizada em H2, Windows, Java 25 e Edge. Hospedagem pública e outros
  SGBDs não foram validados. A implantação segue as condições do README.
- Build Angular aprovado com avisos de tamanho: bundle inicial 607,73 kB
  (aviso em 500); CSS de avaliações 4,82 kB e de armários 7,08 kB
  (aviso em 4). Os limites de erro existentes não foram aumentados.
- O Context Mapper confirmou os três modelos. A biblioteca emite avisos do Java
  25 e uma InterruptedException na finalização da thread interna, depois da
  confirmação; não foram reportados erros de modelo.

## 9. Decisão sobre a RN03

A inconsistência foi reportada antes do banco: o título fala em avaliação por
treino, mas a descrição formal limita o par aluno-professor. Foi adotada uma
avaliação por par, sem criar restrição por treino apenas com base no título.

Também foi preservada a ambiguidade entre a frase “excluí-la, mas não cadastrar
outra” e a promessa de remoção permanente no protótipo. A decisão conservadora,
explicitada antes da implementação, é excluir fisicamente notas e comentário e
manter somente o par para impedir recriação. Não há avaliação excluída logicamente
participando de cálculos. A interface avisa antes de excluir e deixa de oferecer
nova avaliação. O aluno pode editar enquanto a avaliação existir.

A modelagem usa identificadores independentes e uma tabela separada de
participação, permitindo que uma futura regra por treino seja introduzida por
migração explícita, com revisão das chaves e da política de recriação. Nenhuma
dependência de Treino inexistente bloqueia o fluxo atual. A decisão detalhada está
em [F003-avaliacoes.md](../core/funcionalidades/F003-avaliacoes.md).

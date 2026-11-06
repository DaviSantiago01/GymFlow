# API de avaliações por treino

Sessão, papel USUARIO e CSRF existentes no D5. POSTs são transacionais. Autor vem
da sessão; professor vem do treino, nunca do corpo enviado pelo aluno.

| Método | Caminho | Resultado |
|---|---|---|
| GET | `/backend/core/treino/pesquisa` | Histórico paginado de treinos concluídos do aluno |
| GET | `/backend/core/treino/{id}/visualizacao` | Treino concluído do próprio aluno |
| GET | `/backend/core/treino/{id}/avaliacao` | Mesmo registro para preparar o formulário |
| GET | `/backend/core/treino/{id}/minha-avaliacao` | Avaliação desse treino ou null, e jaAvaliou |
| POST | `/backend/core/treino/{id}/avaliar` | Cria uma avaliação vinculada ao treino (200) |
| GET | `/backend/core/treino/demonstracao` | `{disponivel: boolean}` para o ambiente atual |
| POST | `/backend/core/treino/demonstracao` | Carrega três exemplos identificados, uma vez por aluno (204); apenas desenvolvimento |
| GET | `/backend/core/professor/pesquisa` | Professores e estatísticas, com busca/paginação |
| GET | `/backend/core/professor/{id}/visualizacao` | Perfil, estatísticas e dez recentes |
| GET | `/backend/core/professor/{id}/estatistica` | Médias e quantidade |
| GET | `/backend/core/professor/{id}/recentes` | Até dez avaliações, ordenadas pela criação/ID |
| GET | `/backend/core/avaliacao-professor/pesquisa` | Avaliações do próprio aluno, paginadas |
| GET | `/backend/core/avaliacao-professor/{id}/visualizacao` | Própria avaliação |
| GET | `/backend/core/avaliacao-professor/{id}/edicao` | Própria avaliação para editar |
| POST | `/backend/core/avaliacao-professor/{id}/editar` | Atualiza notas/comentário (200) |
| POST | `/backend/core/avaliacao-professor/{id}/remover` | Exclui conteúdo, mantendo somente o treino avaliado (204) |

As antigas rotas de criação direta por professor foram removidas. Treino não pode
ser trocado na edição. Alunos não podem cadastrar treinos reais pela API genérica.

Pesquisa D5: `indicePagina` começa em zero, `tamanhoPagina` máximo 50. A interface
usa nove registros. Professores aceitam `busca` até 100 caracteres, sem distinção
de maiúsculas; caracteres LIKE são escapados. Respostas paginadas usam
`resultado.projecoes` e `resultado.total`.

Treinos expõem identificador, professor, professorNome, descricao, inicio, termino,
demonstracao, avaliacao (ID ou null) e jaAvaliou. Datas são instantes ISO-8601;
a interface apresenta data/hora em Brasília. Avaliações também expõem treino,
treinoInicio e demonstracao; treino null identifica registro anterior à V4.

Corpo de criação/edição, com `X-XSRF-TOKEN` correspondente ao cookie da sessão:

```json
{"auxilio":5,"pontualidade":4,"postura":5,"didatica":4,"adequacao":0,"comentario":"Comentário opcional"}
```

Cinco notas obrigatórias, inteiras 0–5; comentário opcional até 500 unidades UTF-16.
400 para validação, com `mensagem` e `campos`; 401 sem sessão; 403 por autoria,
papel, vínculo ou CSRF; 404 para registro inexistente/consulta privada não
encontrada; 409 para treino não concluído ou já avaliado. Nota zero é válida.
Identificadores malformados seguem o tratamento 422 do D5.

As médias são null sem avaliações; caso contrário são calculadas em DECIMAL128
sem médias persistidas e arredondadas apenas visualmente. Excluir remove a
contribuição às médias. Não se pode recriar avaliação no mesmo treino; outro
treino do mesmo professor pode ser avaliado normalmente.

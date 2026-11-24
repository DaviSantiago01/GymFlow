# API de grupos de treino

Prefixo: `/backend/core/grupo-treino`. Sessão de aluno obrigatória; POST exige `X-XSRF-TOKEN`. O autor e a academia são obtidos da sessão. Identificadores são strings no JSON. Datas usam ISO-8601 com fuso, persistidas como Instant UTC.

| Método | Sufixo | Operação / corpo |
|---|---|---|
| GET | `/pesquisa` | Meus grupos ativos e encerrados; `indicePagina` e `tamanhoPagina` (máximo 50) |
| GET | `/criacao` | Aluno autenticado, academia e horário do servidor |
| GET | `/alunos?busca=usuario&pagina=0` | Busca pelo usuário exato, sem diferenciar maiúsculas; busca vazia não lista contas. `grupo` opcional filtra membros e pendências |
| POST | `/criar` | `{"nome":"Colegas","convidados":["2","3"]}`; convidados opcionais, até 50 na criação |
| GET | `/{g}/visualizacao` | Grupo, membros/participações, pendências e treinos; `pagina`, `historico=false` |
| GET | `/{g}/agenda` | Contexto do formulário de agendamento |
| GET | `/{g}/historico` | Grupo com treinos passados/cancelados; `pagina` |
| GET | `/{g}/membros` | Participações com entrada, saída e indicação do organizador |
| POST | `/{g}/renomear` | `{"nome":"Novo nome"}` |
| POST | `/{g}/convidar` | `{"aluno":"2"}` |
| POST | `/{g}/sair` | Saída do membro, se permitida |
| POST | `/{g}/encerrar` | Encerramento pelo organizador |
| GET | `/convites` | Convites de entrada próprios; `pagina`, `historico=false` |
| POST | `/{g}/convites/{c}/aceitar` | Aceitar entrada |
| POST | `/{g}/convites/{c}/recusar` | Recusar entrada |
| GET | `/{g}/horarios?data=2026-12-10` | Somente o organizador. Lista os horários do dia (06h–21h, uma hora cada) com `inicio`, `fim`, `vagas` restantes, `aberto` (ainda aceita proposta) e os `professores` livres no horário. |
| POST | `/{g}/propor` | `{"inicio":"2026-12-10T18:00:00-03:00","professor":"1"}` — horário em hora cheia, com vaga, e professor livre nele (RN09–RN10). |
| GET | `/{g}/treinos/{t}/visualizacao` | Treino, contagens, convite próprio e respostas dos participantes |
| GET | `/{g}/treinos/{t}/convite` | Contexto do convite de treino |
| GET | `/{g}/treinos/{t}/confirmados` | Lista atual dos confirmados |
| POST | `/{g}/treinos/{t}/cancelar` | Cancelar treino antes do início, pelo organizador |
| GET | `/convites-treino` | Treinos futuros aos quais o aluno foi convidado; `pagina` |
| GET | `/meus-agendamentos` | Treinos de grupo em que o aluno confirmou presença (inclusive os que propôs), próximos primeiro e depois o histórico |
| GET | `/historico` | Treinos passados/cancelados do aluno, mesmo após saída; `pagina` |
| POST | `/{g}/treinos/{t}/convites/{c}/confirmar` | Confirmar presença |
| POST | `/{g}/treinos/{t}/convites/{c}/recusar` | Recusar presença |
| POST | `/{g}/treinos/{t}/convites/{c}/cancelar` | Cancelar a própria presença confirmada |

`GET /pesquisa` mantém o envelope D5: `{"resultado":{"projecoes":[...],"total":1},...}`. As demais listas paginadas usam `{"itens":[...],"total":1,"pagina":0,"tamanho":20}`. Ordenação de grupos por estado/nome/id; treinos futuros por início crescente e histórico decrescente, com desempate por id.

Criação retorna HTTP 200 com `{"identificador":"123"}`. Demais comandos retornam 204. As operações genéricas de escrita do D5 estão desautorizadas: utilize os comandos específicos.

Erros seguem `{"mensagem":"..."}`: 400 para entrada obrigatória ausente/corpo inválido, 401 sem autenticação, 403 sem permissão, 404 para treino/convite inexistente, 409 para estado incompatível/unicidade, 422 para nome, data, prazo ou academia inválidos. Exemplo: `{"mensagem":"A presença só pode ser cancelada até uma hora antes do treino"}`.

`TreinoResumo.agora` fornece o horário usado pelo servidor. Pendente é exibido como EXPIRADO a partir do prazo, mesmo antes do scheduler. Datas passadas representam encontros previstos: não comprovam comparecimento nem criam treinos avaliáveis com professor.

# F016 - Sistema de recomendações personalizadas

O sistema analisa o perfil, o treino, a frequência e as avaliações físicas de cada aluno e sugere ajustes de treino, horários, aulas coletivas, acompanhamento físico e planos. Sugestões que alteram o treino passam antes pelo personal responsável; as demais aparecem direto para o aluno, que pode aceitar ou recusar.

Entidades envolvidas: perfil do aluno, restrição física, recomendação, evidência, aula coletiva, inscrição em aula, classificação de exercício, plano de treino, execução de exercício, avaliação física, meta e registro de acesso (F006).

Perfis: o aluno (USUARIO) mantém o próprio perfil e responde às recomendações; o personal (PERSONAL) analisa, aprova ou rejeita as sugestões dos próprios alunos; cada um vê o histórico das recomendações que lhe dizem respeito.

## Regras de negócio

| Regra | Descrição |
| --- | --- |
| RN001 | Só alunos com matrícula ativa recebem recomendações. |
| RN002 | Objetivo, nível de experiência e disponibilidade são obrigatórios para recomendações personalizadas. |
| RN003 | Objetivo e nível vêm de listas do sistema; a disponibilidade tem de 1 a 7 dias e uma faixa de horário válida. |
| RN004 | Não se recomenda exercício ou aula contraindicado para uma restrição física do aluno. |
| RN005 | Nenhum item pode exigir nível de dificuldade acima do nível do aluno. |
| RN006 | Aula coletiva só é sugerida com vaga, dentro da disponibilidade e sem conflito com outra inscrição do aluno. |
| RN007 | Estagnação: exercício sem aumento de carga nem de repetições nas últimas execuções gera sugestão de substituição. |
| RN008 | Reengajamento: frequência abaixo do mínimo previsto no plano por semanas seguidas gera sugestão de horário menos cheio ou de conversa com o personal. |
| RN009 | Sem avaliação física, ou com a última vencida, gera sugestão de reavaliação. |
| RN010 | Indicador da avaliação com tendência contrária à meta ativa gera sugestão de acompanhamento. |
| RN011 | Sugestões que alteram o treino vão para o personal responsável; as demais vão para o aluno. |
| RN012 | Toda recomendação traz justificativa e as evidências usadas (treinos por semana, cargas, medidas). |
| RN013 | O aluno aceita ou recusa; a recusa pede um motivo. |
| RN014 | Recomendação recusada não volta antes da carência, salvo se houver nova avaliação física. |
| RN015 | Recomendação sem resposta expira na validade, deixa de ser exibida e permanece no histórico. |
| RN016 | As recomendações visíveis são priorizadas por categoria e limitadas por vez. |
| RN017 | O aluno pode desativar categorias opcionais; o acompanhamento físico não pode ser desativado. Duplicadas não são geradas. |
| RN018 | Categorias com baixa taxa de aceitação nas últimas respostas perdem prioridade. |
| RN019 | Nenhuma recomendação é excluída: situação, quem respondeu e quando ficam registrados. |
| RN020 | Ao aprovar uma substituição, o personal gera uma nova versão do plano de treino; a versão anterior é mantida e o aluno toma ciência da mudança. |

## Telas

| Tela | Endereço | Perfil |
| --- | --- | --- |
| Meu perfil de treino (objetivo, nível, disponibilidade e restrições) | `/core/perfil-aluno/meu` | aluno |
| Preferências de recomendação | `/core/perfil-aluno/preferencias` | aluno |
| Recomendações para você | `/core/recomendacao/minhas` | aluno |
| Detalhe da recomendação (justificativa, evidências, aceitar ou recusar) | `/core/recomendacao/:id/visualizacao` | aluno |
| Aulas coletivas sugeridas | `/core/aula-coletiva/para-voce` | aluno |
| Aprovações pendentes | `/core/recomendacao/aprovacoes` | personal |
| Análise da sugestão (comparação do treino atual com o sugerido) | `/core/recomendacao/:id/analise` | personal |
| Versões do plano de treino | `/core/recomendacao/:id/versoes` | personal, aluno |
| Histórico de recomendações | `/core/recomendacao/historico` | aluno, personal |

## API

Prefixo `/backend/core/recomendacao`: `POST gerar`, `GET minhas`, `GET taxas`, `GET aluno/{aluno}`, `GET {id}/detalhe`, `GET {id}/comparacao`, `POST {id}/aceitar`, `POST {id}/recusar`, `POST {id}/ciente`, `POST {id}/aprovar`, `POST {id}/rejeitar`.

Prefixo `/backend/core/perfil-aluno`: `GET meu`, `GET aluno/{aluno}`, `POST dados-treino`, `POST restricoes`, `POST restricoes/{regiao}/remover`, `POST preferencias`.

Prefixo `/backend/core/aula-coletiva`: `GET para-voce`, `POST {id}/inscrever`.

As recomendações vencidas são marcadas como expiradas por uma tarefa agendada.

## Dados de exemplo

No perfil `desenvolvimento` entram o catálogo de exercícios classificados, a grade semanal de aulas coletivas e o perfil da conta `aluno` (hipertrofia, intermediário, restrição no joelho), com entradas que caem nas últimas semanas para disparar o reengajamento. Com o perfil `demonstracao`, outros alunos de exemplo recebem sugestões aguardando aprovação do `professor`.

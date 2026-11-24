# F004 — Grupos de treino

## Decisões e limites
Implementação das páginas 21–28 de atividade.pdf. O agregado GrupoTreino mantém participações, convites de entrada e TreinoGrupo com convites de presença. As alterações bloqueiam a raiz e são transacionais; não existe exclusão física pela API. Consultas usam projeções independentes.

Academia é uma referência compartilhada, acrescentada ao Aluno. Como a instalação anterior representa uma academia, a migração associa os alunos existentes à academia 1, Treino Conecta. Cadastros públicos continuam vinculados a essa academia pelo servidor; não existe troca de academia pelo aluno. Outras academias podem ser provisionadas pela administração no banco.

TreinoGrupo é um encontro organizado por alunos, distinto de Treino (sessão individual concluída com professor, usada nas avaliações). Não gera avaliações ou frequência comprovada automaticamente. Como nos protótipos (passo 2: "Horário e Professor"), o organizador escolhe a data, um horário com vaga e um professor disponível naquele horário (RN09–RN10). A disponibilidade do professor vem das jornadas semanais cadastradas pelo gestor no módulo de profissionais. Salas, notificações externas e descrição mostradas nos desenhos continuam sem regras implementadas.

## Regras
- RN01: NomeGrupo normaliza espaços externos e exige 1–60 caracteres.
- RN02: criação inclui organizador como participante; apenas ele renomeia, convida, propõe/cancela treinos e encerra.
- RN03: mesma academia, nenhum membro ativo ou convite pendente duplicado; apenas destinatário responde. Participações encerradas permanecem; novo convite permite reentrada com nova participação.
- RN04: o prazo de confirmação é calculado automaticamente como início − 1 hora (não é informado); o treino precisa começar daqui a mais de 1 hora (agora < prazo < início); snapshot dos membros ativos, inclusive organizador, com um convite por aluno e treino. O convite do organizador, que propôs o treino, já nasce confirmado (com data de resposta igual à da proposta); os demais ficam pendentes.
- RN05: destinatário, participação ativa, treino não cancelado, convite pendente e agora < prazo.
- RN06: no instante exato do prazo, pendente torna-se expirado; confirmado permanece. Scheduler periódico e leitura lógica respeitam o Clock do servidor.
- RN07: somente confirmado pode cancelar a própria presença; agora <= início menos uma hora, incluindo o instante exato.
- RN08: membro sem confirmação futura pode sair; pendências são canceladas. Organizador encerra. Encerramento cancela treinos futuros, pendências de entrada e participações ativas, preservando dados. Cancelamento de treino só antes de seu início; pendências e confirmações tornam-se canceladas, mantendo data de resposta e registrando data/motivo do cancelamento. Expirados e recusados permanecem.
- Meus agendamentos: o aluno vê, além das aulas, os treinos de grupo em que confirmou presença — próximos primeiro, depois o histórico, preservando os cancelados.
- RN09: o treino ocupa um horário fixo de uma hora, começando em hora cheia entre 06h e 21h (Brasília). Cada horário comporta até `VAGAS_TREINO_GRUPO_POR_HORARIO` (2) treinos de grupo não cancelados da mesma academia; sem vaga, a proposta é recusada. Cancelar um treino libera a vaga.
- RN10: o professor é obrigatório, deve ser profissional ativo classificado como professor e precisa ter disponibilidade cadastrada cobrindo todo o horário no dia da semana do treino. Ele não pode conduzir outro treino de grupo nem ter aula no mesmo horário. As propostas bloqueiam a agenda da academia e o professor, para que duas propostas simultâneas não ocupem a mesma vaga nem o mesmo professor. Treinos anteriores a esta regra permanecem sem professor.

## Segurança e histórico
Identidade, academia e organizador vêm da sessão. Só membros atuais consultam dados restritos de grupos ativos. Antigos membros acessam apenas seu histórico e convites próprios; membros de grupos encerrados podem consultar seu histórico de grupo. Novos membros não recebem retroativamente convites de treinos já propostos. Convites são exibidos dentro do aplicativo.

## Concorrência e precisão
Bloqueio pessimista da raiz antes de qualquer escrita, inclusive expiração. Restrições únicas no banco para participação ativa, convite de entrada pendente e convite de treino. Instant UTC persistido com precisão de nanossegundos; interface exibe America/Sao_Paulo. Tempo é lido depois de adquirir o bloqueio. A expiração é idempotente.

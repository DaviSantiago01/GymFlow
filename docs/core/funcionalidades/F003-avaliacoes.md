# F003 — Avaliação por treino (funcionalidade 2 do PDF)

Referência inicial: páginas 10–21 de `docs/atividade.pdf`. A regra foi esclarecida
explicitamente pelo usuário em 05/11/2026: **uma avaliação por treino**, inclusive
quando o professor for o mesmo em treinos diferentes. A aba Avaliações abre no
histórico do próprio aluno, com data, hora, professor e ação Avaliar treino.
Aluno, sessão, Clock, D5, transações POST e a estrutura Angular são reutilizados.

## RN001 — Cinco notas obrigatórias

Auxílio, pontualidade, postura/respeito, didática/comunicação e adequação da
dificuldade: notas inteiras de 0 a 5. Zero é válido. Maior adequação significa
melhor ajuste às necessidades do aluno. DTO, domínio e banco validam os valores.

## RN002 — Comentário opcional

Até 500 unidades UTF-16, mesma contagem Java/JavaScript. Ausente, null ou vazio é
permitido; espaços externos são removidos após validar o tamanho. A interface
exibe contador e erros acessíveis por campo. Não interpreta HTML do comentário.

## RN003 — Uma avaliação por treino

O documento original diverge entre título (por treino) e descrição (por par
aluno-professor). A V3 adotava conservadoramente a descrição. O esclarecimento
posterior do usuário substitui essa decisão: **cada treino do aluno pode ser
avaliado separadamente**. A migração V4 remove a unicidade aluno-professor e cria
unicidade no TRN_ID, com FK composta garantindo que treino, aluno e professor
sejam os mesmos. Aluno e treino são bloqueados durante o comando transacional.

A política de exclusão já documentada permanece, agora aplicada ao treino:
apagam-se fisicamente notas/comentário e mantém-se somente TRN_ID em
VAT_TREINO_AVALIADO. Não é permitida recriação no mesmo treino; outros treinos
continuam avaliáveis, inclusive do mesmo professor. Não há exclusão lógica.

Avaliações anteriores à V4 são preservadas com treino ausente, sem inventar
data/hora ou treinos retroativos. VAP_AVALIACAO_REALIZADA é legado, sem controlar
novas avaliações. A interface identifica registros anteriores ao histórico.

## RN004 — Autoria e elegibilidade

O aluno vem da sessão; professor e treino vêm do registro persistido. Um treino
só pode ser avaliado pelo próprio aluno, se marcado concluído e com término não
posterior ao relógio do servidor. O corpo só fornece notas/comentário. A API de
avaliação direta de professor foi removida. Os comandos genéricos de escrita
continuam negados. Somente o autor edita/exclui; treino/professor/criação não
podem ser trocados na edição.

## RN005 — Médias e recentes

Somas e contagem vêm de JPQL. Média geral é a soma de todas as notas dividida por
cinco vezes a quantidade, sem arredondar critérios intermediários. Transporte
BigDecimal DECIMAL128 e apresentação pt-BR com uma casa decimal. Sem avaliações:
médias null, texto Sem avaliações. Dez recentes, criação decrescente, ID no
desempate. Edição conserva criação; exclusão remove a contribuição às médias.

## Histórico, dados reais e demonstração

Treino registra aluno, professor, descrição, início, término, conclusão e indicador
de demonstração. O histórico é paginado e inclui somente treinos concluídos do
aluno, em ordem decrescente de início/ID. Datas e horas são exibidas em Brasília.

O usuário autorizou exemplos cadastrados e claramente identificados. No perfil
desenvolvimento, u/v recebem três treinos de demonstração. Outros alunos podem
carregá-los uma única vez pelo botão da tela. A operação é idempotente e não apaga
treinos reais; demonstração é desabilitada fora desse perfil e em produção.
Não são semeadas avaliações. Treinos reais usam a mesma tabela com demonstração
false, conforme [registro de treinos reais](../treinos-reais.md).

Armários mantém Minha reserva e Histórico de reservas somente na navegação interna
da área, sem acessos separados no menu lateral nem na página inicial.

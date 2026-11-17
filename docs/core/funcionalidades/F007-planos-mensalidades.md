# F007 — Gerenciamento de planos e mensalidades

Permite cadastrar, pesquisar, alterar e inativar planos da academia; vincular alunos aos planos; realizar troca ou
cancelamento; gerar e consultar mensalidades; registrar pagamentos e controlar mensalidades em atraso.

Entidades: `Plano`, `AdesaoPlano`, `Aluno` (existente, só referenciado), `Mensalidade` e `Pagamento` (entidade da
mensalidade). Quem opera é o administrador da academia (papel `ADMINISTRADOR`). O aluno só consulta o próprio plano e
as próprias mensalidades em **Meu plano**.

## Decisões e limites
- `Plano` é o plano comercial da academia e não se confunde com `PlanoDeTreino`. O valor é cobrado a cada período, e a
  duração (1 a 12 meses) é o tamanho desse período: um plano trimestral gera uma cobrança a cada três meses.
- A adesão guarda a periodicidade do plano no momento da adesão. Alterar o plano depois muda o valor das próximas
  mensalidades geradas, mas não os períodos de uma adesão já iniciada.
- Vencimento: início do período + 5 dias. Multa de 2% e juros simples de 1% ao mês, proporcionais aos dias de atraso
  (`ParametrosFinanceiros`).
- O pagamento quita o valor devido na data, com multa e juros. Não há pagamento parcial.
- A geração de mensalidades é acionada pelo administrador (botão **Gerar mensalidades**) e também na adesão e na troca,
  que já criam a mensalidade do primeiro período. Não há agendador automático.
- Não existe exclusão física de plano, adesão, mensalidade ou pagamento: as rotas genéricas do D5 ficam bloqueadas pelas
  autorizações.

## Regras
- RN1: o plano exige nome com 2 a 60 caracteres, valor maior que zero (até 99.999,99) e duração de 1 a 12 meses.
- RN2: não pode haver dois planos **ativos** com o mesmo nome (sem diferenciar maiúsculas nem espaços repetidos) e a
  mesma duração. Vale no cadastro e na alteração. Um plano inativado libera o nome. O banco reforça com índice único.
- RN3: um aluno tem no máximo uma adesão ativa (verificada com o aluno bloqueado e reforçada por índice único). Plano
  inativo não recebe adesões.
- RN4: a troca encerra a adesão atual na data de hoje (situação `ENCERRADA`) e só então inicia a nova, que começa na
  mesma data. Os períodos são [início, encerramento), sem sobreposição. O cancelamento (`CANCELADA`) só muda a situação:
  a adesão e todas as mensalidades já geradas permanecem.
- RN5: são geradas as mensalidades dos períodos já iniciados de cada adesão ativa, com o valor atual do plano. Uma
  mensalidade por adesão e período, garantido pelo domínio e pela restrição única `(ADP_ID, MEN_PERIODO_INICIO)`.
- RN6: uma mensalidade `ABERTA` com o vencimento ultrapassado é `ATRASADA`. Esse estado é calculado, não gravado, e o
  valor devido inclui multa e juros.
- RN7: só uma mensalidade em aberto ou em atraso recebe pagamento. O pagamento fica associado à mensalidade, que passa a
  `PAGA`. Há no máximo um pagamento confirmado por mensalidade (restrição no banco).
- RN8: o estorno exige motivo (até 250 caracteres), marca o pagamento como `ESTORNADO` com data e motivo, preserva o
  registro e reabre a mensalidade, que volta a constar em atraso se já venceu.

## Contrato HTTP
Todos os POSTs exigem o cabeçalho `X-XSRF-TOKEN`.

| Método | Caminho | Operação |
|---|---|---|
| GET | `/backend/core/plano/pesquisa?nome=&situacao=` | Planos, com a quantidade de adesões ativas |
| GET | `/backend/core/plano/{id}/visualizacao` | Plano |
| POST | `/backend/core/plano/cadastrar` | `{nome, valor, duracaoMeses}` |
| POST | `/backend/core/plano/{id}/editar` | `{nome, valor, duracaoMeses}` |
| POST | `/backend/core/plano/{id}/inativar` | Inativar |
| GET | `/backend/core/adesao-plano/inclusao` | Alunos e planos ativos |
| GET | `/backend/core/adesao-plano/pesquisa?aluno=&situacao=` | Adesões |
| GET | `/backend/core/adesao-plano/{id}/visualizacao` | Adesão, mensalidades e planos para troca |
| GET | `/backend/core/adesao-plano/meu-plano` | Somente aluno: adesão ativa e mensalidades |
| POST | `/backend/core/adesao-plano/aderir` | `{aluno, plano}` |
| POST | `/backend/core/adesao-plano/{id}/trocar` | `{plano}` |
| POST | `/backend/core/adesao-plano/{id}/cancelar` | Cancelar |
| GET | `/backend/core/mensalidade/pesquisa?aluno=&situacao=ABERTA\|ATRASADA\|PAGA` | Mensalidades com atraso, multa, juros e valor devido |
| GET | `/backend/core/mensalidade/{id}/visualizacao` | Mensalidade e histórico de pagamentos |
| POST | `/backend/core/mensalidade/gerar` | `{"geradas": n}` |
| POST | `/backend/core/mensalidade/{id}/pagar` | `{forma: DINHEIRO\|PIX\|CARTAO_DEBITO\|CARTAO_CREDITO\|BOLETO}` |
| POST | `/backend/core/mensalidade/{id}/pagamentos/{pagamento}/estornar` | `{motivo}` |

## Testes
- `dominio-core/src/test/resources/features/planos-mensalidades.feature` (RN1 a RN8 e autorização).
- `PlanosMensalidadesRepositorioTest` (restrições do banco, troca, pagamento e estorno persistidos, filtros de atraso).
- `PlanosMensalidadesIntegracaoTest` (HTTP, login do administrador, CSRF, permissões do aluno, multa e juros).

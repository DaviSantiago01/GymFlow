# language: pt
Funcionalidade: Gerenciamento de planos e mensalidades
  Para controlar as matrículas e a cobrança dos alunos
  Como administrador da academia
  Quero manter planos, vincular alunos, gerar mensalidades e registrar pagamentos

  Contexto:
    Dado que estou autenticado como administrador no financeiro
    E que hoje é "2026-11-17" no financeiro
    E que existe o aluno "1" no financeiro

  Cenário: RN1 - Plano com nome, valor positivo e duração válida é cadastrado ativo
    Quando eu cadastrar o plano "Mensal" de "99.90" com duração de 1 meses
    Então o plano "Mensal" fica ativo com valor "99.90" e duração de 1 meses

  Esquema do Cenário: RN1 - Dados obrigatórios do plano
    Quando eu cadastrar o plano "<nome>" de "<valor>" com duração de <duracao> meses
    Então a operação financeira é rejeitada com a mensagem "<mensagem>"

    Exemplos:
      | nome    | valor | duracao | mensagem                                                     |
      |         | 99.90 | 1       | Informe o nome do plano com 2 a 60 caracteres                |
      | Mensal  | 0     | 1       | O valor do plano deve ser maior que zero e no máximo 99999.99 |
      | Mensal  | -10   | 1       | O valor do plano deve ser maior que zero e no máximo 99999.99 |
      | Mensal  | 99.90 | 0       | A duração do plano deve ser de 1 a 12 meses                  |
      | Mensal  | 99.90 | 13      | A duração do plano deve ser de 1 a 12 meses                  |

  Cenário: RN2 - Plano ativo com mesmo nome e duração é rejeitado
    Dado o plano ativo "Mensal" de "99.90" com duração de 1 meses
    Quando eu cadastrar o plano "  mensal " de "89.90" com duração de 1 meses
    Então a operação financeira é rejeitada com a mensagem "Já existe um plano ativo com este nome e esta duração"

  Cenário: RN2 - Mesmo nome com outra duração é aceito
    Dado o plano ativo "Mensal" de "99.90" com duração de 1 meses
    Quando eu cadastrar o plano "Mensal" de "259.90" com duração de 3 meses
    Então o plano "Mensal" fica ativo com valor "259.90" e duração de 3 meses

  Cenário: RN2 - Plano inativado libera o nome
    Dado o plano ativo "Mensal" de "99.90" com duração de 1 meses
    Quando eu inativar o plano "Mensal"
    E eu cadastrar o plano "Mensal" de "109.90" com duração de 1 meses
    Então o plano "Mensal" fica ativo com valor "109.90" e duração de 1 meses

  Cenário: RN2 - Alteração não pode duplicar outro plano ativo
    Dado o plano ativo "Mensal" de "99.90" com duração de 1 meses
    E o plano ativo "Trimestral" de "269.90" com duração de 3 meses
    Quando eu alterar o plano "Trimestral" para "Mensal" de "99.90" com duração de 1 meses
    Então a operação financeira é rejeitada com a mensagem "Já existe um plano ativo com este nome e esta duração"

  Cenário: RN3 - Aluno não pode ter duas adesões ativas
    Dado o plano ativo "Mensal" de "99.90" com duração de 1 meses
    E o plano ativo "Trimestral" de "269.90" com duração de 3 meses
    E que o aluno "1" aderiu ao plano "Mensal"
    Quando o aluno "1" aderir ao plano "Trimestral"
    Então a operação financeira é rejeitada com a mensagem "O aluno já possui um plano ativo. Troque ou cancele o plano atual antes de aderir a outro"

  Cenário: RN3 - Plano inativo não recebe adesões
    Dado o plano ativo "Mensal" de "99.90" com duração de 1 meses
    E que o plano "Mensal" foi inativado
    Quando o aluno "1" aderir ao plano "Mensal"
    Então a operação financeira é rejeitada com a mensagem "Somente planos ativos podem receber novas adesões"

  Cenário: RN4 - Troca encerra a adesão atual antes de iniciar a nova
    Dado o plano ativo "Mensal" de "99.90" com duração de 1 meses
    E o plano ativo "Trimestral" de "269.90" com duração de 3 meses
    E que o aluno "1" aderiu ao plano "Mensal" em "2026-10-17"
    Quando eu trocar o plano do aluno "1" para "Trimestral"
    Então a adesão ao plano "Mensal" fica "ENCERRADA" em "2026-11-17"
    E o aluno "1" tem uma única adesão ativa, no plano "Trimestral", iniciada em "2026-11-17"

  Cenário: RN4 - Troca para o mesmo plano é rejeitada
    Dado o plano ativo "Mensal" de "99.90" com duração de 1 meses
    E que o aluno "1" aderiu ao plano "Mensal"
    Quando eu trocar o plano do aluno "1" para "Mensal"
    Então a operação financeira é rejeitada com a mensagem "Escolha um plano diferente do plano atual"

  Cenário: RN4 - Cancelamento preserva a adesão e as mensalidades
    Dado o plano ativo "Mensal" de "99.90" com duração de 1 meses
    E que o aluno "1" aderiu ao plano "Mensal" em "2026-09-17"
    E que as mensalidades foram geradas
    Quando eu cancelar o plano do aluno "1"
    Então a adesão ao plano "Mensal" fica "CANCELADA" em "2026-11-17"
    E existem 3 mensalidades da adesão ao plano "Mensal"

  Cenário: RN4 - Adesão cancelada não pode ser cancelada de novo
    Dado o plano ativo "Mensal" de "99.90" com duração de 1 meses
    E que o aluno "1" aderiu ao plano "Mensal"
    Quando eu cancelar o plano do aluno "1"
    E eu cancelar novamente a última adesão
    Então a operação financeira é rejeitada com a mensagem "Somente uma adesão ativa pode ser trocada ou cancelada"

  Cenário: RN5 - A adesão gera a mensalidade do primeiro período com o valor do plano
    Dado o plano ativo "Mensal" de "99.90" com duração de 1 meses
    Quando o aluno "1" aderir ao plano "Mensal"
    Então existem 1 mensalidades da adesão ao plano "Mensal"
    E a mensalidade do período "2026-11-17" a "2026-12-16" vale "99.90" e vence em "2026-11-22"

  Cenário: RN5 - Geração cobre os períodos iniciados sem duplicar
    Dado o plano ativo "Mensal" de "99.90" com duração de 1 meses
    E que o aluno "1" aderiu ao plano "Mensal"
    E que hoje é "2027-01-20" no financeiro
    Quando eu gerar as mensalidades
    Então foram geradas 2 mensalidades
    E existem 3 mensalidades da adesão ao plano "Mensal"
    Quando eu gerar as mensalidades
    Então foram geradas 0 mensalidades

  Cenário: RN5 - Plano trimestral cobra um período de três meses
    Dado o plano ativo "Trimestral" de "269.90" com duração de 3 meses
    E que o aluno "1" aderiu ao plano "Trimestral"
    E que hoje é "2027-02-17" no financeiro
    Quando eu gerar as mensalidades
    Então foram geradas 1 mensalidades
    E a mensalidade do período "2027-02-17" a "2027-05-16" vale "269.90" e vence em "2027-02-22"

  Cenário: RN5 - Adesão cancelada não gera novas mensalidades
    Dado o plano ativo "Mensal" de "99.90" com duração de 1 meses
    E que o aluno "1" aderiu ao plano "Mensal"
    E que o plano do aluno "1" foi cancelado
    E que hoje é "2027-03-20" no financeiro
    Quando eu gerar as mensalidades
    Então foram geradas 0 mensalidades

  Cenário: RN6 - Mensalidade dentro do prazo está em aberto e sem acréscimos
    Dado o plano ativo "Mensal" de "99.90" com duração de 1 meses
    E que o aluno "1" aderiu ao plano "Mensal"
    E que hoje é "2026-11-22" no financeiro
    Então a mensalidade atual está "ABERTA" com valor devido de "99.90"

  Cenário: RN6 - Mensalidade vencida fica atrasada com multa e juros proporcionais
    Dado o plano ativo "Mensal" de "99.90" com duração de 1 meses
    E que o aluno "1" aderiu ao plano "Mensal"
    E que hoje é "2026-12-02" no financeiro
    Então a mensalidade atual está "ATRASADA" com valor devido de "102.23"

  Cenário: RN7 - Pagamento quita a mensalidade em aberto
    Dado o plano ativo "Mensal" de "99.90" com duração de 1 meses
    E que o aluno "1" aderiu ao plano "Mensal"
    Quando eu registrar o pagamento da mensalidade atual em "PIX"
    Então a mensalidade atual está "PAGA" com 1 pagamento "CONFIRMADO" de "99.90"

  Cenário: RN7 - Pagamento em atraso inclui multa e juros
    Dado o plano ativo "Mensal" de "99.90" com duração de 1 meses
    E que o aluno "1" aderiu ao plano "Mensal"
    E que hoje é "2026-12-02" no financeiro
    Quando eu registrar o pagamento da mensalidade atual em "DINHEIRO"
    Então a mensalidade atual está "PAGA" com 1 pagamento "CONFIRMADO" de "102.23"

  Cenário: RN7 - Mensalidade paga não recebe outro pagamento
    Dado o plano ativo "Mensal" de "99.90" com duração de 1 meses
    E que o aluno "1" aderiu ao plano "Mensal"
    E que a mensalidade atual foi paga em "PIX"
    Quando eu registrar o pagamento da mensalidade atual em "DINHEIRO"
    Então a operação financeira é rejeitada com a mensagem "Somente mensalidades em aberto ou em atraso podem receber pagamento"

  Cenário: RN8 - Estorno preserva o pagamento e reabre a mensalidade
    Dado o plano ativo "Mensal" de "99.90" com duração de 1 meses
    E que o aluno "1" aderiu ao plano "Mensal"
    E que a mensalidade atual foi paga em "PIX"
    Quando eu estornar o pagamento da mensalidade atual pelo motivo "Pagamento lançado no aluno errado"
    Então a mensalidade atual está "ABERTA" com 1 pagamento "ESTORNADO" de "99.90"

  Cenário: RN8 - Estorno de mensalidade vencida volta a indicar atraso
    Dado o plano ativo "Mensal" de "99.90" com duração de 1 meses
    E que o aluno "1" aderiu ao plano "Mensal"
    E que a mensalidade atual foi paga em "PIX"
    E que hoje é "2026-12-02" no financeiro
    Quando eu estornar o pagamento da mensalidade atual pelo motivo "Cartão contestado"
    Então a mensalidade atual está "ATRASADA" com valor devido de "102.23"

  Cenário: RN8 - Pagamento estornado não pode ser estornado de novo
    Dado o plano ativo "Mensal" de "99.90" com duração de 1 meses
    E que o aluno "1" aderiu ao plano "Mensal"
    E que a mensalidade atual foi paga em "PIX"
    Quando eu estornar o pagamento da mensalidade atual pelo motivo "Duplicado"
    E eu estornar o pagamento da mensalidade atual pelo motivo "Duplicado"
    Então a operação financeira é rejeitada com a mensagem "Este pagamento já foi estornado"

  Cenário: RN8 - Estorno exige motivo
    Dado o plano ativo "Mensal" de "99.90" com duração de 1 meses
    E que o aluno "1" aderiu ao plano "Mensal"
    E que a mensalidade atual foi paga em "PIX"
    Quando eu estornar o pagamento da mensalidade atual pelo motivo " "
    Então a operação financeira é rejeitada com a mensagem "Informe o motivo do estorno com até 250 caracteres"

  Cenário: Somente o administrador gerencia planos
    Dado que estou autenticado como aluno no financeiro
    Quando eu cadastrar o plano "Mensal" de "99.90" com duração de 1 meses
    Então a operação financeira é rejeitada com a mensagem "Ação não autorizada"

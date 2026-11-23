# language: pt
Funcionalidade: Recorrência da adesão ao plano
  Para combinar com o aluno por quanto tempo o plano será cobrado
  Como administrador da academia
  Quero informar quantas cobranças a adesão terá e acompanhar o término previsto

  Contexto:
    Dado que estou autenticado como administrador no financeiro
    E que hoje é "2026-11-17" no financeiro
    E que existe o aluno "1" no financeiro
    E o plano ativo "Mensal" de "99.90" com duração de 1 meses
    E o plano ativo "Trimestral" de "269.90" com duração de 3 meses

  Cenário: Adesão mensal com 5 cobranças registra a duração e o término
    Quando o aluno "1" aderir ao plano "Mensal" com recorrência de 5 cobranças
    Então a adesão ao plano "Mensal" prevê 5 cobranças até "2027-04-16"
    E existem 1 mensalidades da adesão ao plano "Mensal"

  Cenário: Na recorrência de plano trimestral cada cobrança cobre três meses
    Quando o aluno "1" aderir ao plano "Trimestral" com recorrência de 2 cobranças
    Então a adesão ao plano "Trimestral" prevê 2 cobranças até "2027-05-16"

  Esquema do Cenário: Recorrência fora do intervalo permitido é rejeitada
    Quando o aluno "1" aderir ao plano "Mensal" com recorrência de <quantidade> cobranças
    Então a operação financeira é rejeitada com a mensagem "A recorrência deve ter de 1 a 60 cobranças"

    Exemplos:
      | quantidade |
      | 0          |
      | 61         |

  Cenário: Sem recorrência a cobrança continua até o cancelamento
    Dado que o aluno "1" aderiu ao plano "Mensal"
    E que hoje é "2027-06-01" no financeiro
    Quando eu gerar as mensalidades
    Então foram geradas 6 mensalidades
    E a adesão ao plano "Mensal" não tem recorrência
    E a adesão ao plano "Mensal" está "ATIVA"

  Cenário: A geração para na última cobrança contratada e conclui a adesão
    Dado que o aluno "1" aderiu ao plano "Mensal" com recorrência de 3 cobranças
    E que hoje é "2027-06-01" no financeiro
    Quando eu gerar as mensalidades
    Então foram geradas 2 mensalidades
    E existem 3 mensalidades da adesão ao plano "Mensal"
    E a adesão ao plano "Mensal" fica "CONCLUIDA" em "2027-02-17"
    Quando eu gerar as mensalidades
    Então foram geradas 0 mensalidades

  Cenário: Antes do fim da última cobrança a adesão continua ativa
    Dado que o aluno "1" aderiu ao plano "Mensal" com recorrência de 1 cobranças
    E que hoje é "2026-12-16" no financeiro
    Quando a rotina do sistema concluir as adesões com prazo cumprido
    Então foram concluídas 0 adesões
    E a adesão ao plano "Mensal" está "ATIVA"

  Cenário: A rotina do sistema conclui a adesão no fim da recorrência
    Dado que o aluno "1" aderiu ao plano "Mensal" com recorrência de 1 cobranças
    E que hoje é "2026-12-17" no financeiro
    Quando a rotina do sistema concluir as adesões com prazo cumprido
    Então foram concluídas 1 adesões
    E a adesão ao plano "Mensal" fica "CONCLUIDA" em "2026-12-17"
    E existem 1 mensalidades da adesão ao plano "Mensal"

  Cenário: A rotina do sistema não altera adesões sem recorrência
    Dado que o aluno "1" aderiu ao plano "Mensal"
    E que hoje é "2028-01-01" no financeiro
    Quando a rotina do sistema concluir as adesões com prazo cumprido
    Então foram concluídas 0 adesões
    E a adesão ao plano "Mensal" está "ATIVA"

  Cenário: Depois do término o aluno pode aderir a outro plano
    Dado que o aluno "1" aderiu ao plano "Mensal" com recorrência de 1 cobranças
    E que hoje é "2026-12-20" no financeiro
    Quando o aluno "1" aderir ao plano "Trimestral"
    Então a adesão ao plano "Mensal" fica "CONCLUIDA" em "2026-12-17"
    E o aluno "1" tem uma única adesão ativa, no plano "Trimestral", iniciada em "2026-12-20"

  Cenário: Durante a recorrência o aluno continua com um único plano ativo
    Dado que o aluno "1" aderiu ao plano "Mensal" com recorrência de 5 cobranças
    E que hoje é "2027-01-01" no financeiro
    Quando o aluno "1" aderir ao plano "Trimestral"
    Então a operação financeira é rejeitada com a mensagem "O aluno já possui um plano ativo. Troque ou cancele o plano atual antes de aderir a outro"

  Cenário: A troca de plano pode definir a recorrência do novo plano
    Dado que o aluno "1" aderiu ao plano "Mensal" com recorrência de 5 cobranças
    Quando eu trocar o plano do aluno "1" para "Trimestral" com recorrência de 2 cobranças
    Então a adesão ao plano "Mensal" fica "ENCERRADA" em "2026-11-17"
    E a adesão ao plano "Trimestral" prevê 2 cobranças até "2027-05-16"

  Cenário: O cancelamento antes do término preserva as mensalidades e encerra a recorrência
    Dado que o aluno "1" aderiu ao plano "Mensal" com recorrência de 5 cobranças
    E que hoje é "2027-01-20" no financeiro
    E que as mensalidades foram geradas
    Quando eu cancelar o plano do aluno "1"
    Então a adesão ao plano "Mensal" fica "CANCELADA" em "2027-01-20"
    E existem 3 mensalidades da adesão ao plano "Mensal"
    Dado que hoje é "2027-06-01" no financeiro
    Quando a rotina do sistema concluir as adesões com prazo cumprido
    Então foram concluídas 0 adesões
    E existem 3 mensalidades da adesão ao plano "Mensal"

  Cenário: Somente o administrador define a recorrência
    Dado que estou autenticado como aluno no financeiro
    Quando o aluno "1" aderir ao plano "Mensal" com recorrência de 5 cobranças
    Então a operação financeira é rejeitada com a mensagem "Ação não autorizada"

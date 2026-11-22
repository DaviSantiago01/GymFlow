# language: pt
Funcionalidade: Relatórios e análise de desempenho da academia
  Para acompanhar os resultados da academia
  Como gestor
  Quero gerar relatórios por período, comparar com o período anterior e exportar os dados

  Contexto:
    Dado que a data atual dos relatórios é "2026-11-22"

  Cenário: RN002 - Período válido é aceito
    Quando eu informar o período de "2026-10-01" a "2026-10-31" no relatório
    Então o período do relatório tem 31 dias

  Cenário: RN002 - Data inicial posterior à final é rejeitada
    Quando eu informar o período de "2026-11-10" a "2026-11-01" no relatório
    Então o relatório é rejeitado com a mensagem "A data inicial não pode ser posterior à data final"

  Cenário: RN002 - Data final no futuro é rejeitada
    Quando eu informar o período de "2026-11-01" a "2026-11-30" no relatório
    Então o relatório é rejeitado com a mensagem "A data final não pode ser posterior à data de hoje"

  Cenário: RN002 - Período maior que 12 meses é rejeitado
    Quando eu informar o período de "2025-01-01" a "2026-11-01" no relatório
    Então o relatório é rejeitado com a mensagem "O período de análise pode ter no máximo 12 meses"

  Cenário: RN004 - Agrupamento por dia em período longo é rejeitado
    Quando eu agrupar por "DIA" o período de "2026-05-01" a "2026-11-01"
    Então o relatório é rejeitado com a mensagem "O agrupamento por dia só é permitido em períodos de até 90 dias"

  Cenário: RN004 - Agrupamento maior que o período é rejeitado
    Quando eu agrupar por "MES" o período de "2026-11-01" a "2026-11-10"
    Então o relatório é rejeitado com a mensagem "O agrupamento não pode ser maior que o próprio período"

  Cenário: RN004 - Agrupamento por semana divide o período
    Quando eu agrupar por "SEMANA" o período de "2026-11-01" a "2026-11-21"
    Então o relatório tem 3 grupos

  Cenário: RN003 - Filtro incompatível com o tipo é rejeitado
    Quando eu filtrar por plano o relatório de "DESEMPENHO_PERSONAIS"
    Então o relatório é rejeitado com a mensagem "O filtro de plano contratado não se aplica ao relatório de Desempenho dos personais"

  Cenário: RN003 - Filtro compatível com o tipo é aceito
    Quando eu filtrar por plano o relatório de "RECEITA_INADIMPLENCIA"
    Então o relatório não é rejeitado

  Esquema do Cenário: RN011 - Comparação com o período anterior
    Quando eu comparar o valor <atual> com o anterior <anterior> em um indicador em que "<sentido>"
    Então o resultado da comparação é "<resultado>"

    Exemplos:
      | atual | anterior | sentido      | resultado |
      | 120   | 100      | MAIOR_MELHOR | MELHOROU  |
      | 80    | 100      | MAIOR_MELHOR | PIOROU    |
      | 4     | 6        | MENOR_MELHOR | MELHOROU  |
      | 50    | 50       | MAIOR_MELHOR | ESTAVEL   |
      | 10    | 0        | MAIOR_MELHOR | SEM_BASE  |

  Cenário: RN011 - Variação percentual sobre o período anterior
    Quando eu comparar o valor 120 com o anterior 100 em um indicador em que "MAIOR_MELHOR"
    Então a variação percentual é "20.0"

  Esquema do Cenário: RN016 - Cumprimento da meta conforme o sentido do indicador
    Quando o realizado for <realizado> para uma meta de <desejado> em um indicador em que "<sentido>"
    Então a meta da academia é considerada "<situacao>"

    Exemplos:
      | realizado | desejado | sentido      | situacao     |
      | 52        | 50       | MAIOR_MELHOR | atingida     |
      | 45        | 50       | MAIOR_MELHOR | não atingida |
      | 4,5       | 5        | MENOR_MELHOR | atingida     |
      | 6         | 5        | MENOR_MELHOR | não atingida |

  Cenário: RN019 - CPF e telefone saem mascarados na exportação
    Quando eu exportar o aluno com CPF "529.982.247-25" e telefone "(81) 99876-4312"
    Então o CPF exportado é "***.982.247-**"
    E o telefone exportado é "(**) *****-4312"

  Cenário: RN019 - Aluno sem CPF cadastrado
    Quando eu exportar o aluno sem CPF e sem telefone
    Então o CPF exportado é "—"
    E o telefone exportado é "—"

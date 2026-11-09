# language: pt
Funcionalidade: Metas de evolução física
  Para orientar a evolução dos alunos
  Como personal
  Quero definir metas por indicador e acompanhar o cumprimento

  Contexto:
    Dado que estou autenticado como o personal "1" nas metas
    E que existe o aluno "1" nas metas
    E que a data atual nas metas é "2026-11-09"
    E que o aluno "1" tem uma avaliação em "2026-11-01" com "CINTURA" de "90"

  Cenário: RN009 - Personal define meta por indicador, valor desejado e período
    Quando eu criar uma meta de "CINTURA" com valor "80" de "2026-11-01" a "2026-12-31"
    Então a meta fica ativa partindo do valor "90"

  Cenário: RN009 - Período invertido é rejeitado
    Quando eu criar uma meta de "CINTURA" com valor "80" de "2026-12-31" a "2026-11-01"
    Então a meta é rejeitada com a mensagem "O início do período não pode ser posterior ao fim"

  Cenário: RN009 - Período já encerrado é rejeitado
    Quando eu criar uma meta de "CINTURA" com valor "80" de "2026-10-01" a "2026-11-08"
    Então a meta é rejeitada com a mensagem "O período da meta deve terminar hoje ou em uma data futura"

  Cenário: RN009 - Valor desejado igual ao valor atual é rejeitado
    Quando eu criar uma meta de "CINTURA" com valor "90" de "2026-11-01" a "2026-12-31"
    Então a meta é rejeitada com a mensagem "O valor desejado deve ser diferente do valor atual do indicador"

  Cenário: RN009 - Valor desejado precisa ser positivo
    Quando eu criar uma meta de "CINTURA" com valor "0" de "2026-11-01" a "2026-12-31"
    Então a meta é rejeitada com a mensagem "O valor desejado deve ser maior que zero"

  Cenário: RN009 - Indicador sem avaliação é rejeitado
    Quando eu criar uma meta de "BRACO" com valor "40" de "2026-11-01" a "2026-12-31"
    Então a meta é rejeitada com a mensagem "Registre uma avaliação com este indicador antes de definir a meta"

  Cenário: RN010 - Segunda meta ativa no mesmo período é rejeitada
    Dado uma meta ativa de "CINTURA" com valor "80" de "2026-11-01" a "2026-12-31"
    Quando eu criar uma meta de "CINTURA" com valor "75" de "2026-12-01" a "2027-01-31"
    Então a meta é rejeitada com a mensagem "Já existe uma meta ativa para este indicador no período. Altere, encerre ou substitua a meta atual"

  Cenário: RN010 - Meta em período sem sobreposição é aceita
    Dado uma meta ativa de "CINTURA" com valor "80" de "2026-11-01" a "2026-12-31"
    Quando eu criar uma meta de "CINTURA" com valor "75" de "2027-01-01" a "2027-01-31"
    Então a meta fica ativa partindo do valor "90"

  Cenário: RN010 - Encerrar a meta libera o período
    Dado uma meta ativa de "CINTURA" com valor "80" de "2026-11-01" a "2026-12-31"
    Quando eu encerrar a meta
    E eu criar uma meta de "CINTURA" com valor "75" de "2026-11-01" a "2026-12-31"
    Então a meta fica ativa partindo do valor "90"

  Cenário: RN010 - Substituir troca a meta ativa
    Dado uma meta ativa de "CINTURA" com valor "80" de "2026-11-01" a "2026-12-31"
    Quando eu substituir a meta pelo valor "75" de "2026-11-01" a "2026-12-31"
    Então a meta fica ativa partindo do valor "90"
    E a meta anterior está na situação "SUBSTITUIDA"

  Cenário: RN010 - Alterar a meta ativa mantém a mesma meta
    Dado uma meta ativa de "CINTURA" com valor "80" de "2026-11-01" a "2026-12-31"
    Quando eu ajustar a meta para o valor "78" de "2026-11-01" a "2027-01-31"
    Então o valor desejado da meta é "78"

  Esquema do Cenário: RN011 - Percentual de cumprimento e situação da meta
    Dado uma meta de "CINTURA" partindo de "<inicial>" com valor desejado "<desejado>"
    Quando eu calcular o cumprimento com o valor atual "<atual>"
    Então o percentual de cumprimento é "<percentual>"
    E o status da meta é "<status>"

    Exemplos:
      | inicial | desejado | atual | percentual | status   |
      | 90      | 80       | 90    | 0          | DISTANTE |
      | 90      | 80       | 85    | 50         | DISTANTE |
      | 90      | 80       | 82.5  | 75         | PROXIMA  |
      | 90      | 80       | 80    | 100        | ATINGIDA |
      | 90      | 80       | 75    | 100        | ATINGIDA |
      | 90      | 80       | 95    | 0          | DISTANTE |
      | 30      | 40       | 38    | 80         | PROXIMA  |

  Cenário: RN012 - Avaliação que atinge o objetivo marca a meta e a data
    Dado uma meta ativa de "CINTURA" com valor "80" de "2026-11-01" a "2026-12-31"
    Quando uma nova avaliação em "2026-11-08" registrar "CINTURA" de "80"
    Então a meta está atingida em "2026-11-08"

  Cenário: RN012 - Avaliação que ultrapassa o objetivo marca a meta
    Dado uma meta ativa de "CINTURA" com valor "80" de "2026-11-01" a "2026-12-31"
    Quando uma nova avaliação em "2026-11-08" registrar "CINTURA" de "78"
    Então a meta está atingida em "2026-11-08"

  Cenário: RN012 - Avaliação que não atinge mantém a meta ativa
    Dado uma meta ativa de "CINTURA" com valor "80" de "2026-11-01" a "2026-12-31"
    Quando uma nova avaliação em "2026-11-08" registrar "CINTURA" de "85"
    Então a meta continua ativa

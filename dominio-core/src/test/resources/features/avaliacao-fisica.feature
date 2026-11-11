# language: pt
Funcionalidade: Avaliação física e acompanhamento da evolução
  Para acompanhar a evolução dos alunos
  Como personal
  Quero registrar avaliações físicas e comparar os resultados

  Contexto:
    Dado que estou autenticado como o personal "1" nas avaliações
    E que existe o aluno "1" nas avaliações
    E que hoje é "2026-11-09"

  Cenário: RN001 - Avaliação fica associada ao aluno cadastrado
    Quando eu registrar para o aluno "1" em "2026-11-01" peso "80" e altura "180"
    Então a avaliação é registrada para o aluno "1"

  Cenário: RN001 - Aluno inexistente é rejeitado
    Quando eu registrar para o aluno "99" em "2026-11-01" peso "80" e altura "180"
    Então a avaliação é rejeitada por aluno inexistente

  Cenário: RN002 - Avaliação guarda a data em que foi realizada
    Quando eu registrar para o aluno "1" em "2026-11-01" peso "80" e altura "180"
    Então a data da avaliação é "2026-11-01"

  Cenário: RN002 - Avaliação sem data é rejeitada
    Quando eu registrar para o aluno "1" sem data peso "80" e altura "180"
    Então a avaliação é rejeitada com a mensagem "Informe a data da avaliação"

  Esquema do Cenário: RN003 - Peso e altura fora dos limites são rejeitados
    Quando eu registrar para o aluno "1" em "2026-11-01" peso "<peso>" e altura "<altura>"
    Então a avaliação é rejeitada com a mensagem "<mensagem>"

    Exemplos:
      | peso | altura | mensagem                               |
      | 19   | 180    | O peso deve estar entre 20 e 400 kg    |
      | 401  | 180    | O peso deve estar entre 20 e 400 kg    |
      | -80  | 180    | O peso deve estar entre 20 e 400 kg    |
      | 80   | 99     | A altura deve estar entre 100 e 250 cm |
      | 80   | 251    | A altura deve estar entre 100 e 250 cm |

  Esquema do Cenário: RN003 - Medida fora dos limites é rejeitada
    Quando eu registrar para o aluno "1" em "2026-11-01" com a medida "CINTURA" de "<valor>"
    Então a avaliação é rejeitada com a mensagem "A medida CINTURA deve estar entre 1 e 300 cm"

    Exemplos:
      | valor |
      | 0     |
      | 0.5   |
      | 301   |

  Esquema do Cenário: RN003 - Valores nos limites são aceitos
    Quando eu registrar para o aluno "1" em "2026-11-01" peso "<peso>" e altura "<altura>"
    Então a avaliação é registrada para o aluno "1"

    Exemplos:
      | peso | altura |
      | 20   | 100    |
      | 400  | 250    |

  Cenário: RN004 - IMC é calculado a partir de peso e altura
    Quando eu registrar para o aluno "1" em "2026-11-01" peso "80" e altura "180"
    Então o IMC da avaliação é "24.69"

  Esquema do Cenário: RN005 - IMC é classificado pelas faixas da academia
    Quando eu registrar para o aluno "1" em "2026-11-01" peso "<peso>" e altura "180"
    Então a classificação do IMC é "<classificacao>"

    Exemplos:
      | peso | classificacao      |
      | 50   | ABAIXO_DO_PESO     |
      | 70   | PESO_NORMAL        |
      | 85   | SOBREPESO          |
      | 100  | OBESIDADE_GRAU_I   |
      | 120  | OBESIDADE_GRAU_II  |
      | 140  | OBESIDADE_GRAU_III |

  Cenário: RN006 - Comparação calcula a diferença absoluta e percentual
    Dado uma avaliação do aluno "1" em "2026-11-01" com "CINTURA" de "90"
    E uma avaliação do aluno "1" em "2026-11-08" com "CINTURA" de "85"
    Quando eu comparar a avaliação de "2026-11-08" com a de "2026-11-01"
    Então a comparação de "CINTURA" tem diferença absoluta "5" e percentual "5.56"

  Cenário: RN007 - Referência é a avaliação imediatamente anterior
    Dado uma avaliação do aluno "1" em "2026-10-01" com "CINTURA" de "95"
    E uma avaliação do aluno "1" em "2026-10-15" com "CINTURA" de "92"
    E uma avaliação do aluno "1" em "2026-11-01" com "CINTURA" de "90"
    Quando eu consultar a referência da avaliação de "2026-11-01"
    Então a referência é a avaliação de "2026-10-15"

  Cenário: RN007 - Primeira avaliação não possui referência
    Dado uma avaliação do aluno "1" em "2026-10-01" com "CINTURA" de "95"
    Quando eu consultar a referência da avaliação de "2026-10-01"
    Então não há avaliação de referência

  Cenário: RN008 - Evolução percentual considera o sentido da variação
    Dado uma avaliação do aluno "1" em "2026-11-01" com "CINTURA" de "90"
    E uma avaliação do aluno "1" em "2026-11-08" com "CINTURA" de "85"
    Quando eu comparar a avaliação de "2026-11-08" com a de "2026-11-01"
    Então a evolução percentual de "CINTURA" é "-5.56"

  Cenário: RN013 - Nova avaliação não substitui as anteriores
    Dado uma avaliação do aluno "1" em "2026-11-01" com "CINTURA" de "90"
    Quando eu registrar para o aluno "1" em "2026-11-08" peso "80" e altura "180"
    Então o aluno "1" possui 2 avaliações válidas
    E nenhuma avaliação foi alterada

  Esquema do Cenário: RN014 - Tendência conforme a margem da academia
    Dado uma avaliação do aluno "1" em "2026-10-01" com "CINTURA" de "<primeira>"
    E uma avaliação do aluno "1" em "2026-10-15" com "CINTURA" de "<segunda>"
    E uma avaliação do aluno "1" em "2026-11-01" com "CINTURA" de "<terceira>"
    Quando eu consultar a tendência de "CINTURA" do aluno "1"
    Então a tendência é "<tendencia>"

    Exemplos:
      | primeira | segunda | terceira | tendencia  |
      | 80       | 82      | 84       | AUMENTANDO |
      | 84       | 82      | 80       | DIMINUINDO |
      | 80       | 80.2    | 80.4     | ESTAVEL    |

  Cenário: RN014 - Sem duas avaliações anteriores não há tendência
    Dado uma avaliação do aluno "1" em "2026-10-01" com "CINTURA" de "80"
    E uma avaliação do aluno "1" em "2026-11-01" com "CINTURA" de "84"
    Quando eu consultar a tendência de "CINTURA" do aluno "1"
    Então não há tendência calculada

  Cenário: RN015 - Histórico de um indicador em ordem cronológica
    Dado uma avaliação do aluno "1" em "2026-11-01" com "CINTURA" de "90"
    E uma avaliação do aluno "1" em "2026-10-01" com "CINTURA" de "95"
    Quando eu consultar o histórico de "CINTURA" do aluno "1"
    Então os valores do histórico são "95,90"

  Cenário: RN016 - Data futura é rejeitada
    Quando eu registrar para o aluno "1" em "2026-11-10" peso "80" e altura "180"
    Então a avaliação é rejeitada com a mensagem "A data da avaliação não pode ser futura"

  Esquema do Cenário: RN016 - Data precisa ser posterior à última avaliação válida
    Dado uma avaliação do aluno "1" em "2026-11-01" com "CINTURA" de "90"
    Quando eu registrar para o aluno "1" em "<data>" peso "80" e altura "180"
    Então a avaliação é rejeitada com a mensagem "A data da avaliação deve ser posterior à última avaliação, de 2026-11-01"

    Exemplos:
      | data       |
      | 2026-11-01 |
      | 2026-10-20 |

  Cenário: RN017 - Situação atual usa a avaliação válida mais recente
    Dado uma avaliação do aluno "1" em "2026-10-01" com "CINTURA" de "95"
    E uma avaliação do aluno "1" em "2026-11-01" com "CINTURA" de "90"
    Quando eu consultar a situação atual do aluno "1"
    Então a avaliação considerada é a de "2026-11-01"

  Cenário: RN017 - Avaliação excluída não compõe a situação atual
    Dado uma avaliação do aluno "1" em "2026-10-01" com "CINTURA" de "95"
    E uma avaliação do aluno "1" em "2026-11-01" com "CINTURA" de "90"
    Quando eu excluir a avaliação de "2026-11-01"
    E eu consultar a situação atual do aluno "1"
    Então a avaliação considerada é a de "2026-10-01"

  Esquema do Cenário: RN018 - Evolução é interpretada por indicador
    Dado uma avaliação do aluno "1" em "2026-11-01" com "<indicador>" de "<antes>"
    E uma avaliação do aluno "1" em "2026-11-08" com "<indicador>" de "<depois>"
    Quando eu comparar a avaliação de "2026-11-08" com a de "2026-11-01"
    Então a interpretação de "<indicador>" é "<interpretacao>"

    Exemplos:
      | indicador | antes | depois | interpretacao |
      | CINTURA   | 90    | 85     | MELHORA       |
      | CINTURA   | 85    | 90     | PIORA         |
      | BRACO     | 40    | 35     | PIORA         |
      | BRACO     | 35    | 40     | MELHORA       |
      | CINTURA   | 90    | 90.5   | ESTAVEL       |

  Cenário: RN018 - Peso não tem interpretação única
    Dado uma avaliação do aluno "1" em "2026-11-01" com peso "90"
    E uma avaliação do aluno "1" em "2026-11-08" com peso "80"
    Quando eu comparar a avaliação de "2026-11-08" com a de "2026-11-01"
    Então a interpretação de "PESO" é "DEPENDE_DO_OBJETIVO"

  Cenário: RN019 - Correção cria nova versão e preserva os dados anteriores
    Dado uma avaliação do aluno "1" em "2026-11-01" com "CINTURA" de "90"
    Quando eu corrigir a avaliação de "2026-11-01" para o peso "81"
    Então a correção está na versão 2 e referencia a avaliação original
    E a avaliação original está na situação "SUBSTITUIDA"
    E o aluno "1" possui 1 avaliações válidas

  Cenário: RN019 - Correção precisa manter a ordem cronológica
    Dado uma avaliação do aluno "1" em "2026-10-01" com "CINTURA" de "95"
    E uma avaliação do aluno "1" em "2026-11-01" com "CINTURA" de "90"
    Quando eu corrigir a data da avaliação de "2026-11-01" para "2026-09-01"
    Então a avaliação é rejeitada com a mensagem "A data corrigida deve manter a ordem cronológica das avaliações"

  Cenário: RN019 - Avaliação já corrigida não pode ser corrigida de novo
    Dado uma avaliação do aluno "1" em "2026-11-01" com "CINTURA" de "90"
    Quando eu corrigir a avaliação de "2026-11-01" para o peso "81"
    E eu corrigir a avaliação original para o peso "82"
    Então a avaliação é rejeitada com a mensagem "Esta avaliação já foi corrigida ou excluída"

  Cenário: RN020 - Última avaliação sem meta é excluída logicamente
    Dado uma avaliação do aluno "1" em "2026-11-01" com "CINTURA" de "90"
    Quando eu excluir a avaliação de "2026-11-01"
    Então a avaliação de "2026-11-01" está na situação "EXCLUIDA"
    E nenhum registro de avaliação foi apagado

  Cenário: RN020 - Avaliação usada como referência de comparação não é excluída
    Dado uma avaliação do aluno "1" em "2026-10-01" com "CINTURA" de "95"
    E uma avaliação do aluno "1" em "2026-11-01" com "CINTURA" de "90"
    Quando eu excluir a avaliação de "2026-10-01"
    Então a avaliação é rejeitada com a mensagem "Esta avaliação é referência de uma avaliação posterior e não pode ser excluída"

  Cenário: RN020 - Avaliação usada como referência de meta não é excluída
    Dado uma avaliação do aluno "1" em "2026-11-01" com "CINTURA" de "90"
    E que a avaliação de "2026-11-01" é referência de uma meta
    Quando eu excluir a avaliação de "2026-11-01"
    Então a avaliação é rejeitada com a mensagem "Esta avaliação é referência de uma meta e não pode ser excluída"

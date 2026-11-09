# language: pt
Funcionalidade: Registro e acompanhamento da execução de exercícios
  Para acompanhar minha evolução
  Como aluno
  Quero registrar a execução dos exercícios e consultar meu histórico de cargas

  Contexto:
    Dado que estou autenticado como o aluno "1"
    E o exercício "1" da categoria "PEITO" com carga máxima "100" e sem incremento próprio
    E o exercício "2" da categoria "PEITO" com carga máxima "100" e incremento próprio de "2.5"
    E o exercício "3" da categoria "COSTAS" com carga máxima "100" e sem incremento próprio
    E o plano "1" do aluno "1" na situação "ATIVO" com o exercício "1" na "SEGUNDA" em 4 séries de 8 a 12 repetições

  Cenário: RN001 - Plano ativo aceita a execução
    Quando eu registrar no plano "1" na "SEGUNDA" o exercício "1" com 4 séries, 12 repetições e carga "40"
    Então a execução é registrada

  Cenário: RN001 - Plano em rascunho não aceita execução
    Dado o plano "2" do aluno "1" na situação "RASCUNHO" com o exercício "1" na "SEGUNDA" em 4 séries de 8 a 12 repetições
    Quando eu registrar no plano "2" na "SEGUNDA" o exercício "1" com 4 séries, 12 repetições e carga "40"
    Então a execução é rejeitada porque só o plano ativo aceita execuções

  Cenário: RN005 - Plano encerrado não aceita execução
    Dado o plano "2" do aluno "1" na situação "ENCERRADO" com o exercício "1" na "SEGUNDA" em 4 séries de 8 a 12 repetições
    Quando eu registrar no plano "2" na "SEGUNDA" o exercício "1" com 4 séries, 12 repetições e carga "40"
    Então a execução é rejeitada porque o plano está encerrado

  Cenário: RN005 - Histórico de plano encerrado continua disponível
    Dado as execuções do exercício "1" no plano "1"
      | data                 | series | repeticoes | carga |
      | 2026-11-02T12:00:00Z | 4      | 10         | 40    |
    Quando o plano "1" for encerrado
    Então o histórico do exercício "1" tem 1 execuções

  Cenário: RN006 - Aluno sem vínculo com o plano é rejeitado
    Dado o plano "2" do aluno "2" na situação "ATIVO" com o exercício "1" na "SEGUNDA" em 4 séries de 8 a 12 repetições
    Quando eu registrar no plano "2" na "SEGUNDA" o exercício "1" com 4 séries, 12 repetições e carga "40"
    Então a execução é rejeitada por falta de vínculo com o plano

  Cenário: RN006 - Exercício fora do treino é rejeitado
    Quando eu registrar no plano "1" na "SEGUNDA" o exercício "2" com 4 séries, 12 repetições e carga "40"
    Então a execução é rejeitada porque o exercício "2" não pertence ao treino de "SEGUNDA"

  Cenário: RN006 - Treino de outro dia é rejeitado
    Quando eu registrar no plano "1" na "TERCA" o exercício "1" com 4 séries, 12 repetições e carga "40"
    Então a execução é rejeitada porque o exercício "1" não pertence ao treino de "TERCA"

  Cenário: RN007 - Execução guarda data, exercício, séries, repetições e carga
    Quando eu registrar no plano "1" na "SEGUNDA" o exercício "1" com 3 séries, 10 repetições e carga "42.5"
    Então a execução guarda o exercício "1", 3 séries, 10 repetições, carga "42.5" e a data atual

  Cenário: RN007 - Nova execução não sobrescreve as anteriores
    Dado as execuções do exercício "1" no plano "1"
      | data                 | series | repeticoes | carga |
      | 2026-11-02T12:00:00Z | 4      | 10         | 40    |
    Quando eu registrar no plano "1" na "SEGUNDA" o exercício "1" com 4 séries, 12 repetições e carga "45"
    Então o histórico do exercício "1" tem 2 execuções
    E nenhuma execução foi alterada ou excluída

  Cenário: RN008 - Limite superior em execuções consecutivas gera sugestão
    Dado as execuções do exercício "1" no plano "1"
      | data                 | series | repeticoes | carga |
      | 2026-11-02T12:00:00Z | 4      | 12         | 40    |
      | 2026-11-04T12:00:00Z | 4      | 12         | 40    |
    Quando eu consultar a sugestão de progressão do exercício "1"
    Então a carga sugerida é "42.00" sem atingir a progressão máxima

  Cenário: RN008 - Execuções não consecutivas no limite não geram sugestão
    Dado as execuções do exercício "1" no plano "1"
      | data                 | series | repeticoes | carga |
      | 2026-11-02T12:00:00Z | 4      | 12         | 40    |
      | 2026-11-04T12:00:00Z | 4      | 10         | 40    |
      | 2026-11-06T12:00:00Z | 4      | 12         | 40    |
    Quando eu consultar a sugestão de progressão do exercício "1"
    Então não há sugestão de progressão

  Cenário: RN008 - O histórico é ordenado por data antes de avaliar a sequência
    Dado as execuções do exercício "1" no plano "1"
      | data                 | series | repeticoes | carga |
      | 2026-11-06T12:00:00Z | 4      | 12         | 40    |
      | 2026-11-02T12:00:00Z | 4      | 12         | 40    |
      | 2026-11-04T12:00:00Z | 4      | 10         | 40    |
    Quando eu consultar a sugestão de progressão do exercício "1"
    Então não há sugestão de progressão

  Cenário: RN008 - Uma única execução no limite não gera sugestão
    Dado as execuções do exercício "1" no plano "1"
      | data                 | series | repeticoes | carga |
      | 2026-11-02T12:00:00Z | 4      | 12         | 40    |
    Quando eu consultar a sugestão de progressão do exercício "1"
    Então não há sugestão de progressão

  Cenário: RN009 - Percentual da categoria parte da última carga válida
    Dado as execuções do exercício "1" no plano "1"
      | data                 | series | repeticoes | carga |
      | 2026-11-02T12:00:00Z | 4      | 12         | 60    |
      | 2026-11-04T12:00:00Z | 4      | 12         | 50    |
      | 2026-11-06T12:00:00Z | 4      | 12         | 0     |
    Quando eu consultar a sugestão de progressão do exercício "1"
    Então a carga sugerida é "52.50" sem atingir a progressão máxima

  Cenário: RN009 - Incremento próprio do exercício tem prioridade
    Dado que o plano "1" foi encerrado
    E o plano "2" do aluno "1" na situação "ATIVO" com o exercício "2" na "SEGUNDA" em 4 séries de 8 a 12 repetições
    E as execuções do exercício "2" no plano "2"
      | data                 | series | repeticoes | carga |
      | 2026-11-02T12:00:00Z | 4      | 12         | 40    |
      | 2026-11-04T12:00:00Z | 4      | 12         | 40    |
    Quando eu consultar a sugestão de progressão do exercício "2"
    Então a carga sugerida é "42.50" sem atingir a progressão máxima

  Cenário: RN010 - Progressão acima do limite usa o limite e sinaliza
    Dado as execuções do exercício "1" no plano "1"
      | data                 | series | repeticoes | carga |
      | 2026-11-02T12:00:00Z | 4      | 12         | 98    |
      | 2026-11-04T12:00:00Z | 4      | 12         | 98    |
    Quando eu consultar a sugestão de progressão do exercício "1"
    Então a carga sugerida é "100" com a progressão máxima atingida

  Cenário: RN013 - Execuções são consultadas em ordem cronológica
    Dado as execuções do exercício "1" no plano "1"
      | data                 | series | repeticoes | carga |
      | 2026-11-06T12:00:00Z | 4      | 12         | 45    |
      | 2026-11-02T12:00:00Z | 3      | 8          | 40    |
      | 2026-11-04T12:00:00Z | 4      | 10         | 42    |
    Quando eu consultar o histórico do exercício "1"
    Então as cargas em ordem cronológica são "40,42,45"
    E as repetições em ordem cronológica são "8,10,12"
    E as séries em ordem cronológica são "3,4,4"

  Cenário: RN014 - Alterar o plano não modifica as execuções registradas
    Dado as execuções do exercício "1" no plano "1"
      | data                 | series | repeticoes | carga |
      | 2026-11-02T12:00:00Z | 4      | 10         | 40    |
    Quando o plano "1" for revisado para uma nova versão
    Então a execução continua vinculada ao plano "1" com carga "40"
    E nenhuma execução foi alterada ou excluída

  Esquema do Cenário: RN018 - Execução acima dos limites do plano é rejeitada
    Quando eu registrar no plano "1" na "SEGUNDA" o exercício "1" com <series> séries, <repeticoes> repetições e carga "40"
    Então a execução é rejeitada com a mensagem "<mensagem>"

    Exemplos:
      | series | repeticoes | mensagem                                               |
      | 5      | 12         | O plano prevê no máximo 4 séries para este exercício    |
      | 4      | 13         | O plano prevê no máximo 12 repetições para este exercício |

  Esquema do Cenário: RN018 - Execução com valores inválidos é rejeitada
    Quando eu registrar no plano "1" na "SEGUNDA" o exercício "1" com <series> séries, <repeticoes> repetições e carga "<carga>"
    Então a execução é rejeitada com a mensagem "<mensagem>"

    Exemplos:
      | series | repeticoes | carga | mensagem                                       |
      | 0      | 12         | 40    | A quantidade de séries deve ser maior que zero |
      | 4      | 0          | 40    | As repetições devem ser maiores que zero       |
      | 4      | 12         | -1    | A carga não pode ser negativa                  |

  Cenário: RN019 - Maior carga válida ignora cargas inválidas
    Dado as execuções do exercício "1" no plano "1"
      | data                 | series | repeticoes | carga |
      | 2026-11-02T12:00:00Z | 4      | 12         | 60    |
      | 2026-11-04T12:00:00Z | 4      | 12         | 150   |
      | 2026-11-06T12:00:00Z | 4      | 12         | 50    |
    Quando eu consultar o histórico do exercício "1"
    Então a maior carga válida é "60"

  Cenário: RN019 - Exercício sem carga válida não possui maior carga
    Dado as execuções do exercício "1" no plano "1"
      | data                 | series | repeticoes | carga |
      | 2026-11-02T12:00:00Z | 4      | 12         | 0     |
    Quando eu consultar o histórico do exercício "1"
    Então não há maior carga válida

# language: pt
Funcionalidade: Prescrição de planos de treino
  Para acompanhar o treino dos alunos
  Como personal
  Quero montar, ativar, alterar, consultar e encerrar planos de treino

  Contexto:
    Dado que estou autenticado como o personal "1"
    E que existe o aluno "1"
    E que existe o exercício "1" chamado "Supino reto"
    E que existe o exercício "2" chamado "Agachamento"

  Cenário: RN001 - Plano criado pertence a um único aluno
    Dado um treino "A" na "SEGUNDA" com objetivo "HIPERTROFIA"
    E no treino "A" o exercício "1" com 4 séries, de 8 a 12 repetições, carga "40" e intervalo 60
    Quando eu criar o plano "Hipertrofia" para o aluno "1" com frequência 1
    Então o plano pertence ao aluno "1"
    E a situação do plano é "RASCUNHO"

  Cenário: RN001 - Plano não pode ser criado para aluno inexistente
    Quando eu criar o plano "Hipertrofia" para o aluno "99" com frequência 1
    Então o plano é rejeitado por aluno inexistente

  Cenário: RN002 - Plano completo é ativado
    Dado um rascunho do aluno "1" com o treino "A" na "SEGUNDA" e frequência 1
    Quando eu ativar o plano
    Então a situação do plano é "ATIVO"

  Cenário: RN002 - Plano sem treinos não é ativado
    Dado um rascunho vazio do aluno "1" com frequência 1
    Quando eu ativar o plano
    Então o plano é rejeitado com a mensagem de plano vazio

  Cenário: RN002 - Treino sem exercícios impede a ativação
    Dado um rascunho do aluno "1" com o treino sem exercícios "A" na "SEGUNDA" e frequência 1
    Quando eu ativar o plano
    Então o plano é rejeitado com a mensagem de treino sem exercícios "A"

  Cenário: RN003 - Exercício repetido no mesmo treino é impedido com o motivo
    Dado um treino "A" na "SEGUNDA" com objetivo "HIPERTROFIA"
    E no treino "A" o exercício "1" com 4 séries, de 8 a 12 repetições, carga "40" e intervalo 60
    Quando eu incluir no treino "A" o exercício "1" com 3 séries, de 8 a 12 repetições, carga "30" e intervalo 60
    Então o plano é rejeitado com a mensagem de exercício "Supino reto" repetido no treino "A"

  Cenário: RN003 - O mesmo exercício pode estar em treinos diferentes
    Dado um treino "A" na "SEGUNDA" com objetivo "HIPERTROFIA"
    E um treino "B" na "QUARTA" com objetivo "HIPERTROFIA"
    E no treino "A" o exercício "1" com 4 séries, de 8 a 12 repetições, carga "40" e intervalo 60
    E no treino "B" o exercício "1" com 4 séries, de 8 a 12 repetições, carga "40" e intervalo 60
    Quando eu criar o plano "Hipertrofia" para o aluno "1" com frequência 2
    Então a situação do plano é "RASCUNHO"

  Esquema do Cenário: RN004 - Valores inválidos da prescrição são rejeitados
    Dado um treino "A" na "SEGUNDA" com objetivo "HIPERTROFIA"
    Quando eu incluir no treino "A" o exercício "1" com <series> séries, de <minimo> a <maximo> repetições, carga "<carga>" e intervalo <intervalo>
    Então o plano é rejeitado com a mensagem "<mensagem>"

    Exemplos:
      | series | minimo | maximo | carga | intervalo | mensagem                                                            |
      | 0      | 8      | 12     | 40    | 60        | A quantidade de séries deve ser maior que zero                      |
      | 4      | 0      | 12     | 40    | 60        | As repetições devem ser maiores que zero                            |
      | 4      | 12     | 8      | 40    | 60        | O limite superior de repetições não pode ser menor que o inferior   |
      | 4      | 8      | 12     | -1    | 60        | A carga não pode ser negativa                                       |
      | 4      | 8      | 12     | 40    | 14        | O intervalo deve estar entre 15 e 300 segundos                      |
      | 4      | 8      | 12     | 40    | 301       | O intervalo deve estar entre 15 e 300 segundos                      |

  Esquema do Cenário: RN004 - Valores no limite da academia são aceitos
    Dado um treino "A" na "SEGUNDA" com objetivo "HIPERTROFIA"
    E no treino "A" o exercício "1" com 4 séries, de 8 a 12 repetições, carga "<carga>" e intervalo <intervalo>
    Quando eu criar o plano "Hipertrofia" para o aluno "1" com frequência 1
    Então a situação do plano é "RASCUNHO"

    Exemplos:
      | carga | intervalo |
      | 0     | 15        |
      | 40    | 300       |

  Cenário: RN011 - Aluno com plano ativo não ativa outro
    Dado um plano ativo do aluno "1" sob responsabilidade do personal "1"
    E um rascunho do aluno "1" com o treino "A" na "SEGUNDA" e frequência 1
    Quando eu ativar o plano
    Então o plano é rejeitado com a mensagem de plano ativo existente

  Cenário: RN011 - Novo plano substitui o plano ativo
    Dado um plano ativo do aluno "1" sob responsabilidade do personal "1"
    E um rascunho do aluno "1" com o treino "A" na "SEGUNDA" e frequência 1
    Quando eu substituir o plano atual pelo rascunho
    Então a situação do plano é "ATIVO"
    E a situação do plano anterior é "SUBSTITUIDO"

  Cenário: RN012 - Personal responsável encerra o plano
    Dado um plano ativo do aluno "1" sob responsabilidade do personal "1"
    Quando eu encerrar o plano
    Então a situação do plano é "ENCERRADO"

  Cenário: RN012 - Outro personal não encerra o plano
    Dado um plano ativo do aluno "1" sob responsabilidade do personal "2"
    Quando eu encerrar o plano
    Então o plano é rejeitado por pertencer a outro personal
    E a situação do plano é "ATIVO"

  Cenário: RN012 - Outro personal não altera o plano
    Dado um plano ativo do aluno "1" sob responsabilidade do personal "2"
    Quando eu alterar o plano para o nome "Novo" com frequência 1
    Então o plano é rejeitado por pertencer a outro personal

  Cenário: RN015 - Plano encerrado continua consultável e não é excluído
    Dado um plano ativo do aluno "1" sob responsabilidade do personal "1"
    Quando eu encerrar o plano
    Então o plano continua consultável
    E a exclusão do plano não é autorizada

  Cenário: RN016 - Frequência diferente dos dias cadastrados impede a ativação
    Dado um rascunho do aluno "1" com o treino "A" na "SEGUNDA" e frequência 3
    Quando eu ativar o plano
    Então o plano é rejeitado com a mensagem de frequência divergente de 3 para 1

  Cenário: RN016 - Frequência igual aos dias cadastrados permite a ativação
    Dado um treino "A" na "SEGUNDA" com objetivo "HIPERTROFIA"
    E um treino "B" na "QUARTA" com objetivo "HIPERTROFIA"
    E no treino "A" o exercício "1" com 4 séries, de 8 a 12 repetições, carga "40" e intervalo 60
    E no treino "B" o exercício "2" com 4 séries, de 8 a 12 repetições, carga "40" e intervalo 60
    Quando eu criar o plano "Hipertrofia" para o aluno "1" com frequência 2
    E eu ativar o plano
    Então a situação do plano é "ATIVO"

  Esquema do Cenário: RN017 - Valores fora da faixa do objetivo são rejeitados
    Dado um treino "A" na "SEGUNDA" com objetivo "<objetivo>"
    Quando eu incluir no treino "A" o exercício "1" com <series> séries, de <minimo> a <maximo> repetições, carga "40" e intervalo 60
    Então o plano é rejeitado com a mensagem "<mensagem>"

    Exemplos:
      | objetivo    | series | minimo | maximo | mensagem                                    |
      | FORCA       | 2      | 3      | 6      | O objetivo FORCA exige de 3 a 6 séries      |
      | FORCA       | 4      | 3      | 8      | O objetivo FORCA exige de 1 a 6 repetições  |
      | HIPERTROFIA | 6      | 8      | 12     | O objetivo HIPERTROFIA exige de 3 a 5 séries |
      | HIPERTROFIA | 4      | 5      | 12     | O objetivo HIPERTROFIA exige de 6 a 12 repetições |
      | RESISTENCIA | 1      | 12     | 20     | O objetivo RESISTENCIA exige de 2 a 4 séries |
      | RESISTENCIA | 3      | 12     | 21     | O objetivo RESISTENCIA exige de 12 a 20 repetições |

  Esquema do Cenário: RN017 - Valores dentro da faixa do objetivo são aceitos
    Dado um treino "A" na "SEGUNDA" com objetivo "<objetivo>"
    E no treino "A" o exercício "1" com <series> séries, de <minimo> a <maximo> repetições, carga "40" e intervalo 60
    Quando eu criar o plano "Plano" para o aluno "1" com frequência 1
    Então a situação do plano é "RASCUNHO"

    Exemplos:
      | objetivo    | series | minimo | maximo |
      | FORCA       | 6      | 1      | 6      |
      | HIPERTROFIA | 3      | 6      | 12     |
      | RESISTENCIA | 4      | 12     | 20     |

  Cenário: RN020 - Alteração cria nova versão e preserva a anterior
    Dado um plano ativo do aluno "1" sob responsabilidade do personal "1"
    Quando eu alterar o plano para o nome "Novo" com frequência 1
    Então a situação do plano é "ATIVO"
    E o plano está na versão 2 e referencia a versão anterior
    E a situação do plano anterior é "SUBSTITUIDO"

  Cenário: RN020 - Plano encerrado não pode ser alterado
    Dado um plano encerrado do aluno "1" sob responsabilidade do personal "1"
    Quando eu alterar o plano para o nome "Novo" com frequência 1
    Então o plano é rejeitado com a mensagem de plano não alterável

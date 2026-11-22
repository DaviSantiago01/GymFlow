# language: pt
Funcionalidade: Recomendações personalizadas
  Para treinar de acordo com o meu objetivo e as minhas limitações
  Como aluno
  Quero manter meu perfil de treino e receber apenas sugestões compatíveis com ele

  Contexto:
    Dado um aluno sem perfil de treino nas recomendações

  Cenário: RN002 - Perfil sem objetivo, nível e disponibilidade não recebe recomendações personalizadas
    Então o perfil de treino está incompleto
    E faltam no perfil "objetivo, nível de experiência, disponibilidade semanal"

  Cenário: RN002 - Perfil completo
    Quando o aluno informar o objetivo "HIPERTROFIA", o nível "INTERMEDIARIO" e os dias "SEGUNDA,QUARTA,SEXTA" das "18:00" às "21:00"
    Então o perfil de treino está completo

  Cenário: RN003 - Disponibilidade sem dias é rejeitada
    Quando o aluno informar o objetivo "HIPERTROFIA", o nível "INTERMEDIARIO" e os dias "" das "18:00" às "21:00"
    Então o perfil é rejeitado com a mensagem "A disponibilidade semanal deve ter entre 1 e 7 dias"

  Cenário: RN003 - Horário inicial depois do final é rejeitado
    Quando o aluno informar o objetivo "HIPERTROFIA", o nível "INTERMEDIARIO" e os dias "SEGUNDA" das "21:00" às "18:00"
    Então o perfil é rejeitado com a mensagem "Informe um horário inicial anterior ao horário final"

  Cenário: RN004 - Exercício contraindicado para a restrição física não é recomendado
    Dado que o aluno tem o perfil "HIPERTROFIA" e nível "INTERMEDIARIO"
    E que o aluno registrou uma restrição no "JOELHO"
    Quando o sistema avaliar um exercício de nível "INICIANTE" para "HIPERTROFIA" contraindicado para "JOELHO"
    Então o exercício é descartado pelo motivo "Contraindicado para a restrição em joelho"

  Cenário: RN005 - Exercício acima do nível do aluno não é recomendado
    Dado que o aluno tem o perfil "HIPERTROFIA" e nível "INTERMEDIARIO"
    Quando o sistema avaliar um exercício de nível "AVANCADO" para "HIPERTROFIA" contraindicado para ""
    Então o exercício é descartado pelo motivo "Nível acima do nível do aluno"

  Cenário: RN004 e RN005 - Exercício compatível pode ser recomendado
    Dado que o aluno tem o perfil "HIPERTROFIA" e nível "INTERMEDIARIO"
    E que o aluno registrou uma restrição no "JOELHO"
    Quando o sistema avaliar um exercício de nível "INTERMEDIARIO" para "HIPERTROFIA" contraindicado para "OMBRO"
    Então o exercício pode ser recomendado

  Cenário: RN017 - Aluno desativa uma categoria opcional
    Quando o aluno desativar as recomendações de "AULAS_COLETIVAS"
    Então o aluno não recebe recomendações de "AULAS_COLETIVAS"
    E o aluno continua recebendo recomendações de "ACOMPANHAMENTO_FISICO"

  Cenário: RN017 - Acompanhamento físico não pode ser desativado
    Quando o aluno desativar as recomendações de "ACOMPANHAMENTO_FISICO"
    Então o perfil é rejeitado com a mensagem "As recomendações de acompanhamento físico não podem ser desativadas"

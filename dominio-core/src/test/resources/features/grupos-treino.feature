# language: pt
Funcionalidade: Grupos de treino da academia
  Contexto:
    Dado um grupo ativo com organizador e outro membro

  @RN01
  Cenário: Normalizar o nome do grupo
    Quando o organizador renomeia o grupo para "  Colegas  "
    Então o nome do grupo é "Colegas"

  @RN02
  Cenário: Reservar administração ao organizador
    Quando outro membro tenta renomear o grupo
    Então a operação do grupo é negada

  @RN03
  Cenário: Impedir convite entre academias
    Quando o organizador convida aluno de outra academia
    Então o convite entre academias é rejeitado

  @RN04
  Cenário: Convidar todos os membros do treino
    Quando o organizador propõe um treino com mais de uma hora de antecedência
    Então cada membro recebe um convite de treino incluindo o organizador

  @RN04
  Cenário: Organizador fica confirmado ao propor o treino
    Quando o organizador propõe um treino com mais de uma hora de antecedência
    Então o organizador está confirmado e o outro membro está pendente

  @RN04
  Cenário: Prazo de confirmação definido automaticamente
    Quando o organizador propõe um treino com mais de uma hora de antecedência
    Então o prazo de confirmação do treino é uma hora antes do início

  @RN04
  Cenário: Treino sem antecedência mínima é rejeitado
    Quando o organizador propõe um treino daqui a exatamente uma hora
    Então a proposta de treino é rejeitada por falta de antecedência

  @RN09
  Cenário: Registrar o professor escolhido no treino
    Quando o organizador propõe um treino com mais de uma hora de antecedência
    Então o treino proposto registra o professor escolhido

  @RN09
  Cenário: Treino fora de hora cheia é rejeitado
    Quando o organizador propõe um treino fora de hora cheia
    Então a proposta é rejeitada por horário não oferecido

  @RN09
  Cenário: Treino depois do último horário é rejeitado
    Quando o organizador propõe um treino depois do último horário da academia
    Então a proposta é rejeitada por horário não oferecido

  @RN10
  Cenário: Professor só atende horários inteiros dentro da jornada
    Dado um professor que trabalha às sextas das 6h às 13h
    Então o horário das 12h está dentro da jornada do professor
    E o horário das 13h está fora da jornada do professor
    E o horário das 6h está dentro da jornada do professor

  @RN05
  Cenário: Somente o destinatário responde sua presença
    Quando o organizador tenta responder o convite de treino de outro membro
    Então a operação do grupo é negada

  @RN06
  Cenário: Expirar pendências na fronteira do prazo
    Quando o membro confirma e o prazo de confirmação termina
    Então a presença fica confirmada e a pendência expira

  @RN07
  Cenário: Cancelar exatamente uma hora antes do treino
    Quando o membro cancela sua presença exatamente uma hora antes
    Então a presença cancelada preserva a data da confirmação

  @RN08
  Cenário: Encerrar sem apagar registros
    Quando o organizador encerra o grupo
    Então os encontros futuros são cancelados e o histórico é preservado

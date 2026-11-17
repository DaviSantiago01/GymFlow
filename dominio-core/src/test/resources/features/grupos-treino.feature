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
  Cenário: Prazo de confirmação definido automaticamente
    Quando o organizador propõe um treino com mais de uma hora de antecedência
    Então o prazo de confirmação do treino é uma hora antes do início

  @RN04
  Cenário: Treino sem antecedência mínima é rejeitado
    Quando o organizador propõe um treino daqui a exatamente uma hora
    Então a proposta de treino é rejeitada por falta de antecedência

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

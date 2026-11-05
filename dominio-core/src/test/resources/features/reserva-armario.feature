# language: pt
Funcionalidade: Gerenciamento de reservas e uso de armários
  Cenário: RN01 - Armário ocupado não aceita outra reserva
    Dado uma indisponibilidade por "armario"
    Quando solicitar a reserva do armário disponível no catálogo
    Então a operação é rejeitada

  Cenário: RN02 - Aluno com reserva não pode reservar outro armário
    Dado uma indisponibilidade por "aluno"
    Quando solicitar a reserva do armário disponível no catálogo
    Então a operação é rejeitada

  Cenário: RN03 - Reserva de três horas a partir da confirmação
    Dado uma reserva confirmada às "2026-11-04T12:00:00Z"
    Então o término previsto é "2026-11-04T15:00:00Z"
    E a reserva está ativa às "2026-11-04T14:59:59.999999999Z"

  Cenário: RN03 - Vencimento no limite exato
    Dado uma reserva confirmada às "2026-11-04T12:00:00Z"
    Quando processar o vencimento às "2026-11-04T15:00:00Z"
    Então a situação é "VENCIDA"
    E o encerramento real é "2026-11-04T15:00:00Z"

  Cenário: RN03 - Recuperação após servidor indisponível
    Dado uma reserva confirmada às "2026-11-04T12:00:00Z"
    Quando processar o vencimento às "2026-11-06T18:00:00Z"
    Então a situação é "VENCIDA"
    E o encerramento real é "2026-11-04T15:00:00Z"

  Esquema do Cenário: RN04 - Finalização pelo responsável
    Dado uma reserva confirmada às "2026-11-04T12:00:00Z"
    Quando o aluno "1" solicitar "<acao>" às "2026-11-04T13:00:00Z"
    Então a situação é "<situacao>"
    E o encerramento real é "2026-11-04T13:00:00Z"
    Exemplos:
      | acao     | situacao  |
      | cancelar | CANCELADA |
      | encerrar | ENCERRADA |

  Esquema do Cenário: RN04 - Outro aluno não pode finalizar
    Dado uma reserva confirmada às "2026-11-04T12:00:00Z"
    Quando o aluno "2" solicitar "<acao>" às "2026-11-04T13:00:00Z"
    Então a operação é rejeitada
    E a situação é "ATIVA"
    Exemplos:
      | acao     |
      | cancelar |
      | encerrar |

  Cenário: RN04 - Reserva finalizada não pode ser cancelada novamente
    Dado uma reserva confirmada às "2026-11-04T12:00:00Z"
    Quando o aluno "1" solicitar "cancelar" às "2026-11-04T13:00:00Z"
    E o aluno "1" solicitar "encerrar" às "2026-11-04T14:00:00Z"
    Então a operação é rejeitada
    E a situação é "CANCELADA"

  Cenário: RN04 - Cancelamento no vencimento é rejeitado
    Dado uma reserva confirmada às "2026-11-04T12:00:00Z"
    Quando o aluno "1" solicitar "cancelar" às "2026-11-04T15:00:00Z"
    Então a operação é rejeitada

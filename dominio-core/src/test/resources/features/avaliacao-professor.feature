# language: pt
Funcionalidade: Avaliar professores
  Cenário: Cinco notas válidas e comentário opcional
    Quando registrar as notas 0, 1, 2, 3 e 5 sem comentário
    Então a avaliação de domínio contém as cinco notas informadas
    E a avaliação de domínio não contém comentário

  Esquema do Cenário: Rejeitar nota fora do intervalo
    Quando registrar auxílio com nota <nota>
    Então a avaliação de domínio é rejeitada
    Exemplos:
      | nota |
      | -1   |
      | 6    |

  Cenário: Rejeitar comentário extenso
    Quando registrar um comentário com 501 caracteres
    Então a avaliação de domínio é rejeitada

  Cenário: Impedir avaliação duplicada
    Dado que o aluno já avaliou o treino
    Quando o aluno tentar avaliar novamente esse treino
    Então a avaliação duplicada não é persistida

  Esquema do Cenário: Somente o autor modifica a avaliação
    Dado uma avaliação de domínio do aluno "1"
    Quando o aluno "2" tentar <acao> a avaliação de domínio
    Então a avaliação de domínio é rejeitada
    Exemplos:
      | acao    |
      | editar  |
      | excluir |

  Cenário: Edição mantém a data de criação usada nas avaliações recentes
    Dado uma avaliação de domínio do aluno "1"
    Quando o aluno "1" tentar editar a avaliação de domínio
    Então a data de criação é preservada e a atualização é registrada

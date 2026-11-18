# language: pt
Funcionalidade: Problemas de equipamentos

  Cenário: RN001 - problema exige equipamento ativo e data válida
    Dado a gestão de problemas disponível para um administrador
    Então o sistema rejeita problema de equipamento inativo ou com data inválida

  Cenário: RN002 - edição para gravidade crítica interdita o equipamento
    Dado a gestão de problemas disponível para um administrador
    Então o sistema interdita o equipamento ao alterar um problema para crítico

  Cenário: RN003 - encerramento exige solução ou justificativa e impede edição posterior
    Dado a gestão de problemas disponível para um administrador
    Então o sistema exige solução ou justificativa e não permite editar problema encerrado

  Cenário: RN004 - ordem ativa impede resolver ou cancelar o problema
    Dado a gestão de problemas disponível para um administrador
    Então o sistema bloqueia encerramento direto enquanto existe ordem ativa

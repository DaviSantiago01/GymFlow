# language: pt
Funcionalidade: Ordens de manutenção de equipamentos

  Cenário: RN001 - abertura exige problema em aberto e rejeita ordem ativa duplicada
    Dado a gestão de ordens de manutenção disponível para um administrador
    Então o sistema só abre ordem para problema aberto e sem outra ordem ativa

  Cenário: RN002 - início valida técnico diagnóstico e data
    Dado a gestão de ordens de manutenção disponível para um administrador
    Então o sistema valida data técnico e diagnóstico para iniciar a manutenção

  Cenário: RN003 - conclusão resolve problema e mantém equipamento indisponível
    Dado a gestão de ordens de manutenção disponível para um administrador
    Então o sistema conclui com serviço válido resolve o problema e mantém a situação do equipamento

  Cenário: RN004 - cancelamento mantém problema aberto
    Dado a gestão de ordens de manutenção disponível para um administrador
    Então o sistema exige justificativa no cancelamento e mantém o problema aberto

  Cenário: RN005 - custos negativos ou com precisão inválida são rejeitados
    Dado a gestão de ordens de manutenção disponível para um administrador
    Então o sistema rejeita custos negativos ou com mais de duas casas decimais

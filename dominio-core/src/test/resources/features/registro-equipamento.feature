# language: pt
Funcionalidade: Situação e inspeções de equipamentos

  Cenário: RN001 - mudança de localização guarda estado anterior e usuário
    Dado a gestão de situação e inspeções disponível para um administrador
    Então o sistema preserva área anterior e usuário ao movimentar um equipamento ativo

  Cenário: RN002 - prazo usa aquisição e depois última inspeção
    Dado a gestão de situação e inspeções disponível para um administrador
    Então o sistema calcula prazo pela aquisição e depois pela última inspeção

  Cenário: RN003 - inspeção retroativa é rejeitada e reprovação interdita
    Dado a gestão de situação e inspeções disponível para um administrador
    Então o sistema rejeita data retroativa e interdita quando a inspeção é reprovada

  Cenário: RN004 - impedimentos bloqueiam a liberação e inspeção aprovada libera
    Dado a gestão de situação e inspeções disponível para um administrador
    Então o sistema exige inspeção aprovada na liberação e bloqueia pendências de segurança

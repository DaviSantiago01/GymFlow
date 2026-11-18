# language: pt
Funcionalidade: Cadastro e gestão de equipamentos

  Cenário: RN001 - cadastro válido e aquisição futura rejeitada
    Dado a gestão de cadastro e gestão disponível para um administrador
    Então o sistema valida os dados obrigatórios e o cadastro começa disponível

  Cenário: RN002 - patrimônio ou série duplicados e edição do próprio equipamento
    Dado a gestão de cadastro e gestão disponível para um administrador
    Então o sistema rejeita patrimônio e série duplicados e permite manter os valores do próprio equipamento

  Cenário: RN003 - edição cadastral preserva situação e área
    Dado a gestão de cadastro e gestão disponível para um administrador
    Então o sistema preserva área e situação durante a edição e rejeita edição de inativos

  Cenário: RN004 - exclusão com histórico bloqueada e inativação preserva registros
    Dado a gestão de cadastro e gestão disponível para um administrador
    Então o sistema exclui sem histórico e preserva registros ao inativar

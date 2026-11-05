# language: pt
Funcionalidade: Gerenciar unidades federativas
  Para manter o cadastro de unidades federativas da aplicação
  Como usuário do sistema
  Quero incluir, buscar, alterar e excluir unidades federativas,
  respeitando as regras de unicidade de sigla e nome e de autorização

  Contexto:
    Dado que não existe nenhuma unidade federativa cadastrada

  Cenário: Incluir uma unidade federativa
    Dado que sou um usuário administrador
    Quando eu incluo a unidade federativa de código IBGE "35", sigla "SP" e nome "São Paulo"
    Então a unidade federativa é incluída com sucesso
    E a unidade federativa de código IBGE "35" passa a existir

  Cenário: Não é possível incluir uma unidade federativa sem autorização de administrador
    Dado que sou um usuário sem autorização de administrador
    Quando eu tento incluir a unidade federativa de código IBGE "35", sigla "SP" e nome "São Paulo"
    Então a inclusão é rejeitada por falta de autorização

  Cenário: Não é possível incluir uma unidade federativa com código IBGE já existente
    Dado que sou um usuário administrador
    E que existe uma unidade federativa com código IBGE "35", sigla "SP" e nome "São Paulo"
    Quando eu tento incluir a unidade federativa de código IBGE "35", sigla "RJ" e nome "Rio de Janeiro"
    Então a inclusão é rejeitada porque já existe uma unidade federativa com o identificador "35"

  Cenário: Não é possível incluir uma unidade federativa com sigla já existente
    Dado que sou um usuário administrador
    E que existe uma unidade federativa com código IBGE "35", sigla "SP" e nome "São Paulo"
    Quando eu tento incluir a unidade federativa de código IBGE "33", sigla "SP" e nome "Rio de Janeiro"
    Então a inclusão é rejeitada porque já existe uma unidade federativa com a sigla "SP"

  Cenário: Não é possível incluir uma unidade federativa com nome já existente
    Dado que sou um usuário administrador
    E que existe uma unidade federativa com código IBGE "35", sigla "SP" e nome "São Paulo"
    Quando eu tento incluir a unidade federativa de código IBGE "33", sigla "RJ" e nome "São Paulo"
    Então a inclusão é rejeitada porque já existe uma unidade federativa com o nome "São Paulo"

  Cenário: Não é possível incluir uma unidade federativa com nome já existente, ainda que com outras maiúsculas e minúsculas
    Dado que sou um usuário administrador
    E que existe uma unidade federativa com código IBGE "35", sigla "SP" e nome "São Paulo"
    Quando eu tento incluir a unidade federativa de código IBGE "33", sigla "RJ" e nome "SÃO PAULO"
    Então a inclusão é rejeitada porque já existe uma unidade federativa com o nome "SÃO PAULO"

  Cenário: Buscar uma unidade federativa existente
    Dado que existe uma unidade federativa com código IBGE "35", sigla "SP" e nome "São Paulo"
    E que sou um usuário sem autorização de administrador
    Quando eu busco a unidade federativa de código IBGE "35"
    Então a unidade federativa encontrada tem sigla "SP" e nome "São Paulo"

  Cenário: Não é possível buscar uma unidade federativa inexistente
    Dado que sou um usuário sem autorização de administrador
    Quando eu tento buscar a unidade federativa de código IBGE "35"
    Então a busca é rejeitada porque não existe unidade federativa com o identificador "35"

  Cenário: Alterar uma unidade federativa
    Dado que sou um usuário administrador
    E que existe uma unidade federativa com código IBGE "35", sigla "SP" e nome "São Paulo"
    Quando eu altero a unidade federativa de código IBGE "35" para sigla "SP" e nome "Estado de São Paulo"
    Então a unidade federativa é alterada com sucesso
    E a unidade federativa de código IBGE "35" passa a ter sigla "SP" e nome "Estado de São Paulo"

  Cenário: Alterar uma unidade federativa mantendo sua própria sigla e nome
    Dado que sou um usuário administrador
    E que existe uma unidade federativa com código IBGE "35", sigla "SP" e nome "São Paulo"
    Quando eu altero a unidade federativa de código IBGE "35" para sigla "SP" e nome "São Paulo"
    Então a unidade federativa é alterada com sucesso

  Cenário: Não é possível alterar uma unidade federativa sem autorização de administrador
    Dado que sou um usuário sem autorização de administrador
    E que existe uma unidade federativa com código IBGE "35", sigla "SP" e nome "São Paulo"
    Quando eu tento alterar a unidade federativa de código IBGE "35" para sigla "SP" e nome "Estado de São Paulo"
    Então a alteração é rejeitada por falta de autorização

  Cenário: Não é possível alterar uma unidade federativa inexistente
    Dado que sou um usuário administrador
    Quando eu tento alterar a unidade federativa de código IBGE "35" para sigla "SP" e nome "São Paulo"
    Então a alteração é rejeitada porque não existe unidade federativa com o identificador "35"

  Cenário: Não é possível alterar uma unidade federativa para a sigla de outra
    Dado que sou um usuário administrador
    E que existe uma unidade federativa com código IBGE "35", sigla "SP" e nome "São Paulo"
    E que existe uma unidade federativa com código IBGE "33", sigla "RJ" e nome "Rio de Janeiro"
    Quando eu tento alterar a unidade federativa de código IBGE "33" para sigla "SP" e nome "Rio de Janeiro"
    Então a alteração é rejeitada porque já existe uma unidade federativa com a sigla "SP"

  Cenário: Excluir uma unidade federativa
    Dado que sou um usuário administrador
    E que existe uma unidade federativa com código IBGE "35", sigla "SP" e nome "São Paulo"
    Quando eu excluo a unidade federativa de código IBGE "35"
    Então a unidade federativa é excluída com sucesso
    E a unidade federativa de código IBGE "35" deixa de existir

  Cenário: Não é possível excluir uma unidade federativa sem autorização de administrador
    Dado que sou um usuário sem autorização de administrador
    E que existe uma unidade federativa com código IBGE "35", sigla "SP" e nome "São Paulo"
    Quando eu tento excluir a unidade federativa de código IBGE "35"
    Então a exclusão é rejeitada por falta de autorização

  Cenário: Não é possível excluir uma unidade federativa inexistente
    Dado que sou um usuário administrador
    Quando eu tento excluir a unidade federativa de código IBGE "35"
    Então a exclusão é rejeitada porque não existe unidade federativa com o identificador "35"

  Cenário: Não é possível excluir uma unidade federativa referenciada
    Dado que sou um usuário administrador
    E que existe uma unidade federativa com código IBGE "35", sigla "SP" e nome "São Paulo"
    E que a unidade federativa de código IBGE "35" é referenciada
    Quando eu tento excluir a unidade federativa de código IBGE "35"
    Então a exclusão é rejeitada porque a unidade federativa "35" é referenciada
    E a unidade federativa de código IBGE "35" continua a existir

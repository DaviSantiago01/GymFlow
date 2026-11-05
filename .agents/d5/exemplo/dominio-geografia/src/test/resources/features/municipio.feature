# language: pt
Funcionalidade: Gerenciar municípios
  Para manter o cadastro de municípios da aplicação
  Como usuário do sistema
  Quero incluir, buscar, alterar, excluir e extinguir municípios,
  respeitando as regras de unidade federativa, unicidade de nome e de autorização

  Contexto:
    Dado que não existe nenhum município cadastrado
    E que existe a unidade federativa de código IBGE "35"

  Cenário: Incluir um município
    Dado que sou um administrador do cadastro de municípios
    Quando eu incluo o município de código IBGE "3550308", nome "São Paulo"
    Então o município é incluído com sucesso
    E o município de código IBGE "3550308" passa a existir

  Cenário: Não é possível incluir um município sem autorização de administrador
    Dado que sou um usuário sem autorização para administrar municípios
    Quando eu tento incluir o município de código IBGE "3550308", nome "São Paulo"
    Então a inclusão do município é rejeitada por falta de autorização

  Cenário: Não é possível incluir um município com código IBGE já existente
    Dado que sou um administrador do cadastro de municípios
    E que existe um município com código IBGE "3550308", nome "São Paulo"
    Quando eu tento incluir o município de código IBGE "3550308", nome "Santos"
    Então a inclusão é rejeitada porque já existe um município com o identificador "3550308"

  Cenário: Não é possível incluir um município com nome já existente na mesma unidade federativa
    Dado que sou um administrador do cadastro de municípios
    E que existe um município com código IBGE "3550308", nome "São Paulo"
    Quando eu tento incluir o município de código IBGE "3548500", nome "São Paulo"
    Então a inclusão é rejeitada porque já existe um município com o nome "São Paulo" na unidade federativa

  Cenário: Não é possível incluir um município com nome já existente na mesma unidade federativa, ainda que com outras maiúsculas e minúsculas
    Dado que sou um administrador do cadastro de municípios
    E que existe um município com código IBGE "3550308", nome "São Paulo"
    Quando eu tento incluir o município de código IBGE "3548500", nome "SÃO PAULO"
    Então a inclusão é rejeitada porque já existe um município com o nome "SÃO PAULO" na unidade federativa

  Cenário: Não é possível incluir um município em uma unidade federativa inexistente
    Dado que sou um administrador do cadastro de municípios
    Quando eu tento incluir o município de código IBGE "3304557", nome "Rio de Janeiro"
    Então a inclusão é rejeitada porque não existe unidade federativa com o identificador "33"

  Cenário: Não é possível incluir um município já extinto
    Dado que sou um administrador do cadastro de municípios
    Quando eu tento incluir o município extinto de código IBGE "3550308" e nome "São Paulo"
    Então a inclusão é rejeitada porque o município só pode ser extinto pela ação de extinção

  Cenário: Não é possível incluir um município já extinto, ainda que com código IBGE já existente
    Dado que sou um administrador do cadastro de municípios
    E que existe um município com código IBGE "3550308", nome "São Paulo"
    Quando eu tento incluir o município extinto de código IBGE "3550308" e nome "São Paulo"
    Então a inclusão é rejeitada porque o município só pode ser extinto pela ação de extinção

  Cenário: Buscar um município existente
    Dado que existe um município com código IBGE "3550308", nome "São Paulo"
    E que sou um usuário sem autorização para administrar municípios
    Quando eu busco o município de código IBGE "3550308"
    Então o município encontrado tem nome "São Paulo"

  Cenário: Não é possível buscar um município inexistente
    Dado que sou um usuário sem autorização para administrar municípios
    Quando eu tento buscar o município de código IBGE "3550308"
    Então a busca é rejeitada porque não existe município com o identificador "3550308"

  Cenário: Alterar um município
    Dado que sou um administrador do cadastro de municípios
    E que existe um município com código IBGE "3550308", nome "São Paulo"
    Quando eu altero o município de código IBGE "3550308" para nome "Cidade de São Paulo"
    Então o município é alterado com sucesso
    E o município de código IBGE "3550308" passa a ter nome "Cidade de São Paulo"

  Cenário: Alterar um município mantendo seu próprio nome
    Dado que sou um administrador do cadastro de municípios
    E que existe um município com código IBGE "3550308", nome "São Paulo"
    Quando eu altero o município de código IBGE "3550308" para nome "São Paulo"
    Então o município é alterado com sucesso

  Cenário: Não é possível alterar um município sem autorização de administrador
    Dado que sou um usuário sem autorização para administrar municípios
    E que existe um município com código IBGE "3550308", nome "São Paulo"
    Quando eu tento alterar o município de código IBGE "3550308" para nome "Cidade de São Paulo"
    Então a alteração do município é rejeitada por falta de autorização

  Cenário: Não é possível alterar um município inexistente
    Dado que sou um administrador do cadastro de municípios
    Quando eu tento alterar o município de código IBGE "3550308" para nome "São Paulo"
    Então a alteração é rejeitada porque não existe município com o identificador "3550308"

  Cenário: Não é possível alterar um município extinto
    Dado que sou um administrador do cadastro de municípios
    E que existe um município extinto com código IBGE "3550308", nome "São Paulo"
    Quando eu tento alterar o município de código IBGE "3550308" para nome "Cidade de São Paulo"
    Então a alteração é rejeitada porque o município "3550308" está extinto

  Cenário: Não é possível alterar um município extinto, ainda que para o nome de outro município
    Dado que sou um administrador do cadastro de municípios
    E que existe um município extinto com código IBGE "3550308", nome "São Paulo"
    E que existe um município com código IBGE "3548500", nome "Santos"
    Quando eu tento alterar o município de código IBGE "3550308" para nome "Santos"
    Então a alteração é rejeitada porque o município "3550308" está extinto

  Cenário: Não é possível alterar um município extinto informando-o como extinto
    Dado que sou um administrador do cadastro de municípios
    E que existe um município extinto com código IBGE "3550308", nome "São Paulo"
    Quando eu tento alterar o município de código IBGE "3550308" para extinto com nome "Cidade de São Paulo"
    Então a alteração é rejeitada porque o município só pode ser extinto pela ação de extinção

  Cenário: Não é possível extinguir um município por meio da alteração
    Dado que sou um administrador do cadastro de municípios
    E que existe um município com código IBGE "3550308", nome "São Paulo"
    Quando eu tento alterar o município de código IBGE "3550308" para extinto com nome "São Paulo"
    Então a alteração é rejeitada porque o município só pode ser extinto pela ação de extinção

  Cenário: Excluir um município
    Dado que sou um administrador do cadastro de municípios
    E que existe um município com código IBGE "3550308", nome "São Paulo"
    Quando eu excluo o município de código IBGE "3550308"
    Então o município é excluído com sucesso
    E o município de código IBGE "3550308" deixa de existir

  Cenário: Não é possível excluir um município sem autorização de administrador
    Dado que sou um usuário sem autorização para administrar municípios
    E que existe um município com código IBGE "3550308", nome "São Paulo"
    Quando eu tento excluir o município de código IBGE "3550308"
    Então a exclusão do município é rejeitada por falta de autorização

  Cenário: Não é possível excluir um município inexistente
    Dado que sou um administrador do cadastro de municípios
    Quando eu tento excluir o município de código IBGE "3550308"
    Então a exclusão é rejeitada porque não existe município com o identificador "3550308"

  Cenário: Não é possível excluir um município referenciado
    Dado que sou um administrador do cadastro de municípios
    E que existe um município com código IBGE "3550308", nome "São Paulo"
    E que o município de código IBGE "3550308" é referenciado
    Quando eu tento excluir o município de código IBGE "3550308"
    Então a exclusão é rejeitada porque o município "3550308" é referenciado
    E o município de código IBGE "3550308" continua a existir

  Cenário: Extinguir um município
    Dado que sou um administrador do cadastro de municípios
    E que existe um município com código IBGE "3550308", nome "São Paulo"
    Quando eu extingo o município de código IBGE "3550308"
    Então o município é extinguido com sucesso
    E o município de código IBGE "3550308" passa a estar extinto

  Cenário: Não é possível extinguir um município sem autorização de administrador
    Dado que sou um usuário sem autorização para administrar municípios
    E que existe um município com código IBGE "3550308", nome "São Paulo"
    Quando eu tento extinguir o município de código IBGE "3550308"
    Então a extinção do município é rejeitada por falta de autorização

  Cenário: Não é possível extinguir um município inexistente
    Dado que sou um administrador do cadastro de municípios
    Quando eu tento extinguir o município de código IBGE "3550308"
    Então a extinção é rejeitada porque não existe município com o identificador "3550308"

  Cenário: Não é possível extinguir um município já extinto
    Dado que sou um administrador do cadastro de municípios
    E que existe um município extinto com código IBGE "3550308", nome "São Paulo"
    Quando eu tento extinguir o município de código IBGE "3550308"
    Então a extinção é rejeitada porque o município "3550308" está extinto

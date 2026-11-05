# language: pt
Funcionalidade: Gerenciar clientes
  Para manter o cadastro de clientes da aplicação
  Como usuário do sistema
  Quero incluir, buscar, alterar e excluir clientes,
  respeitando as regras de unicidade de CPF, de município e de autorização

  Contexto:
    Dado que não existe nenhum cliente cadastrado
    E que existe o município de código IBGE "3550308"

  Cenário: Incluir um cliente
    Dado que sou um administrador do cadastro de clientes
    Quando eu incluo o cliente de CPF "11111111111", nome "Maria" e município "3550308"
    Então o cliente é incluído com sucesso
    E o cliente de CPF "11111111111" passa a existir

  Cenário: Não é possível incluir um cliente sem autorização de administrador
    Dado que sou um usuário sem autorização para administrar clientes
    Quando eu tento incluir o cliente de CPF "11111111111", nome "Maria" e município "3550308"
    Então a inclusão do cliente é rejeitada por falta de autorização

  Cenário: Não é possível incluir um cliente com identificador informado
    Dado que sou um administrador do cadastro de clientes
    Quando eu tento incluir o cliente de identificador "999", CPF "11111111111", nome "Maria" e município "3550308"
    Então a inclusão é rejeitada porque o identificador do cliente é gerado na inclusão

  Cenário: Não é possível incluir um cliente com o identificador de outro cliente
    Dado que sou um administrador do cadastro de clientes
    E que existe um cliente com identificador "1", CPF "22222222222" e nome "Ana"
    Quando eu tento incluir o cliente de identificador "1", CPF "11111111111", nome "Maria" e município "3550308"
    Então a inclusão é rejeitada porque o identificador do cliente é gerado na inclusão

  Cenário: Não é possível incluir um cliente com CPF já existente
    Dado que sou um administrador do cadastro de clientes
    E que existe um cliente com identificador "1", CPF "11111111111" e nome "Maria"
    Quando eu tento incluir o cliente de CPF "11111111111", nome "Ana" e município "3550308"
    Então a inclusão é rejeitada porque já existe um cliente com o CPF "11111111111"

  Cenário: Não é possível incluir um cliente em um município inexistente
    Dado que sou um administrador do cadastro de clientes
    Quando eu tento incluir o cliente de CPF "11111111111", nome "Maria" e município "3304557"
    Então a inclusão é rejeitada porque não existe município com o identificador "3304557"

  Cenário: Não é possível incluir um cliente em um município extinto
    Dado que sou um administrador do cadastro de clientes
    E que existe o município extinto de código IBGE "3304557"
    Quando eu tento incluir o cliente de CPF "11111111111", nome "Maria" e município "3304557"
    Então a inclusão é rejeitada porque o município "3304557" está extinto

  Cenário: Buscar um cliente existente
    Dado que existe um cliente com identificador "1", CPF "11111111111" e nome "Maria"
    E que sou um usuário sem autorização para administrar clientes
    Quando eu busco o cliente de identificador "1"
    Então o cliente encontrado tem CPF "11111111111" e nome "Maria"

  Cenário: Não é possível buscar um cliente inexistente
    Dado que sou um usuário sem autorização para administrar clientes
    Quando eu tento buscar o cliente de identificador "1"
    Então a busca é rejeitada porque não existe cliente com o identificador "1"

  Cenário: Alterar um cliente
    Dado que sou um administrador do cadastro de clientes
    E que existe um cliente com identificador "1", CPF "11111111111" e nome "Maria"
    Quando eu altero o cliente de identificador "1" para CPF "11111111111" e nome "Maria Silva"
    Então o cliente é alterado com sucesso
    E o cliente de identificador "1" passa a ter CPF "11111111111" e nome "Maria Silva"

  Cenário: Não é possível alterar um cliente sem autorização de administrador
    Dado que sou um usuário sem autorização para administrar clientes
    E que existe um cliente com identificador "1", CPF "11111111111" e nome "Maria"
    Quando eu tento alterar o cliente de identificador "1" para CPF "11111111111" e nome "Maria Silva"
    Então a alteração do cliente é rejeitada por falta de autorização

  Cenário: Não é possível alterar um cliente inexistente
    Dado que sou um administrador do cadastro de clientes
    Quando eu tento alterar o cliente de identificador "1" para CPF "11111111111" e nome "Maria"
    Então a alteração é rejeitada porque não existe cliente com o identificador "1"

  Cenário: Não é possível alterar um cliente para o CPF de outro
    Dado que sou um administrador do cadastro de clientes
    E que existe um cliente com identificador "1", CPF "11111111111" e nome "Maria"
    E que existe um cliente com identificador "2", CPF "22222222222" e nome "Ana"
    Quando eu tento alterar o cliente de identificador "2" para CPF "11111111111" e nome "Ana"
    Então a alteração é rejeitada porque já existe um cliente com o CPF "11111111111"

  Cenário: Não é possível alterar um cliente de um município extinto sem trocar o município
    Dado que sou um administrador do cadastro de clientes
    E que existe um cliente com identificador "1", CPF "11111111111" e nome "Maria"
    E que o município de código IBGE "3550308" foi extinto
    Quando eu tento alterar o cliente de identificador "1" para CPF "11111111111" e nome "Maria Silva"
    Então a alteração é rejeitada porque o município "3550308" está extinto

  Cenário: Alterar um cliente de um município extinto para outro município
    Dado que sou um administrador do cadastro de clientes
    E que existe um cliente com identificador "1", CPF "11111111111" e nome "Maria"
    E que o município de código IBGE "3550308" foi extinto
    E que existe o município de código IBGE "3509502"
    Quando eu altero o cliente de identificador "1" para CPF "11111111111", nome "Maria" e município "3509502"
    Então o cliente é alterado com sucesso
    E o cliente de identificador "1" passa a ter o município "3509502"

  Cenário: Não é possível alterar um cliente para um município inexistente
    Dado que sou um administrador do cadastro de clientes
    E que existe um cliente com identificador "1", CPF "11111111111" e nome "Maria"
    Quando eu tento alterar o cliente de identificador "1" para CPF "11111111111", nome "Maria" e município "3304557"
    Então a alteração é rejeitada porque não existe município com o identificador "3304557"

  Cenário: Excluir um cliente
    Dado que sou um administrador do cadastro de clientes
    E que existe um cliente com identificador "1", CPF "11111111111" e nome "Maria"
    Quando eu excluo o cliente de identificador "1"
    Então o cliente é excluído com sucesso
    E o cliente de identificador "1" deixa de existir

  Cenário: Não é possível excluir um cliente sem autorização de administrador
    Dado que sou um usuário sem autorização para administrar clientes
    E que existe um cliente com identificador "1", CPF "11111111111" e nome "Maria"
    Quando eu tento excluir o cliente de identificador "1"
    Então a exclusão do cliente é rejeitada por falta de autorização

  Cenário: Não é possível excluir um cliente inexistente
    Dado que sou um administrador do cadastro de clientes
    Quando eu tento excluir o cliente de identificador "1"
    Então a exclusão é rejeitada porque não existe cliente com o identificador "1"

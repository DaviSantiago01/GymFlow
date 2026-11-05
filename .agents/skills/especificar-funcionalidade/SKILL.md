---
name: especificar-funcionalidade
description: Especifica uma funcionalidade de um subdomínio em docs/S/funcionalidades, com identificador, título, descrição e regras de negócio. Use ao iniciar uma funcionalidade ou alterar os requisitos de uma existente, antes de especificar seus testes.
---
Funcionalidades são identificadas por F001, F002, etc.

A especificação de uma funcionalidade **F** de um subdomínio **S** é armazenada no arquivo docs/**S**/funcionalidades/**F**.md, contendo identificador, título, descrição e regras de negócio de **F**.

Funcionalidades são completas. Por exemplo, se instâncias de um conceito são incluídas através de uma funcionalidade **F**, então outras ações relacionadas ao conceito, como alteração e exclusão de instâncias, também fazem parte de **F**.

Existe uma subseção para cada regra de negócio.

Regras de negócio são identificadas por RN001, RN002, etc., não entram em contradição entre si nem com regras de outras funcionalidades e não são redundantes.

Ver exemplo em [references/REFERENCE.md](references/REFERENCE.md).
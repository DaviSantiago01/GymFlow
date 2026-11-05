---
name: desenvolver-infraestrutura-dominio
description: Implementa com Spring Data JPA os repositórios e demais SPIs da camada de domínio de uma funcionalidade, com testes @DataJpaTest. Use depois de projetar o banco de dados, em projetos que não usam o D5.
---
Desenvolver a camada de infraestrutura para a camada de domínio consiste em implementar os repositórios e demais SPIs utilizados por uma funcionalidade além de testes automatizados com @DataJpaTest para os repositórios.

As implementações fazem uso de [programação defensiva](../programar-defensivamente/SKILL.md), exceto as entidades JPA, e são adaptadores para repositórios Spring Boot.

O nome da implementação de um repositório de um agregado **A** é **A**Repositorio.

As entidades JPA seguem o modelo do projeto de banco de dados docs/**S**/projeto.dbml: os nomes das tabelas e colunas do modelo são declarados em @Table e @Column.
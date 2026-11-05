---
name: projetar-entidades
description: Decide quais conceitos são entidades e quais regras de negócio elas encapsulam. Use durante projetar-software, depois de projetar os objetos de valor.
---
Conceitos que possuem identidade são projetados como entidades.

Para evitar o antipadrão objetos anêmicos, regras de negócio que dependem apenas dos atributos de uma entidade e/ou de parâmetros de métodos são encapsuladas em métodos da entidade.

Entidades são declaradas no módulo do contexto delimitado que corresponde ao subdomínio **S** no qual elas foram identificadas.

Regras relacionadas a atributos e coleções presentes em [projetar-objetos-valor](../projetar-objetos-valor/SKILL.md) também se aplicam a entidades.

Ver [projetar-agregados](../projetar-agregados/SKILL.md).
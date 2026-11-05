---
name: projetar-contextos-delimitados
description: Organiza o modelo do Context Mapper em um contexto delimitado compartilhado e um por subdomínio, com as relações entre eles. Use no início de projetar-software e ao criar um subdomínio ou uma dependência entre subdomínios.
---
O gerador de projetos já criou docs/compartilhado/projeto.cml, docs/**S**/projeto.cml de cada subdomínio **S** e docs/projeto.cml, com a estrutura descrita a seguir. A descrição do projeto está em `domainVisionStatement` do `Domain`, e o tipo e a descrição de cada subdomínio, informados na geração, estão em `type` e `domainVisionStatement` de sua declaração, em docs/compartilhado/projeto.cml. Ao acrescentar um subdomínio, siga o mesmo modelo.

Conceitualmente, existe um único contexto delimitado, pois a linguagem onipresente é válida em todo o software. Entretanto, como o Context Mapper não permite que um contexto delimitado seja definido em vários arquivos, [compartilhado/projeto.cml](../../../docs/compartilhado/projeto.cml) declara o domínio do software, seus subdomínios e o contexto delimitado compartilhado contendo o módulo compartilhado, que encapsula o projeto de software do módulo Maven dominio-compartilhado. Esse módulo contém apenas conceitos que não pertencem a nenhum subdomínio específico e são comuns a todos eles, por exemplo, os papéis de autorização.

docs/**S**/projeto.cml importa [compartilhado/projeto.cml](../../../docs/compartilhado/projeto.cml), declara o `ContextMap` com a relação `**S** Downstream-Upstream compartilhado` (que autoriza **S** a referenciar tipos do compartilhado), declara o contexto delimitado **S** que implementa o subdomínio **S** e contém o módulo **S**, que encapsula o projeto de software de **S**.

Quando **S** referencia tipos de outro subdomínio **T**, docs/**S**/projeto.cml também importa docs/**T**/projeto.cml, inclui **T** no `contains` do `ContextMap` e declara a relação `**S** Downstream-Upstream **T**`, que corresponde à dependência do módulo Maven dominio-**S** em relação ao módulo dominio-**T**. Essas relações não formam ciclos.

[projeto.cml](../../../docs/projeto.cml) apenas importa os demais arquivos *.cml.

Ver [projetar-objetos-valor](../projetar-objetos-valor/SKILL.md).
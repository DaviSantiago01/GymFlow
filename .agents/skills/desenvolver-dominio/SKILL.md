---
name: desenvolver-dominio
description: Desenvolve em Java os objetos de valor, entidades, serviços e interfaces de repositório de uma funcionalidade, a partir do modelo do Context Mapper. Use depois de projetar o software, em projetos que não usam o D5.
---
Desenvolver a camada de domínio de uma funcionalidade **F** consiste em desenvolver, com [programação defensiva](../programar-defensivamente/SKILL.md), os objetos de valor, entidades, serviços e interfaces de repositório projetados a partir da especificação e dos cenários de teste de **F**.

A interface do repositório, apesar de declarada dentro da entidade raiz no modelo (exigência do Context Mapper), é desenvolvida em arquivo próprio, fora da entidade.
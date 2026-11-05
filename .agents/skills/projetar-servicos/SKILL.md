---
name: projetar-servicos
description: Define o serviço de cada agregado, com um método para cada ação do usuário e as regras de negócio que dependem de outros agregados ou de repositórios. Use durante projetar-software, depois de projetar as associações.
---
Existe um serviço para cada agregado **A** chamado **A**Servico.

**A**Servico possui um método para cada ação que o usuário pode executar sobre o agregado.

Regras de negócio que dependem de mais de um agregado, objeto de valor ou que precisam acessar repositórios são implementadas nesses métodos.

Ver [projetar-repositorios](../projetar-repositorios/SKILL.md).
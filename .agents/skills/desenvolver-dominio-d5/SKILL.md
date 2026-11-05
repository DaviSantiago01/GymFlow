---
name: desenvolver-dominio-d5
description: Desenvolve a camada de domínio de uma funcionalidade com as classes e interfaces do framework D5. Use depois de projetar o software, em vez de desenvolver-dominio, em projetos que usam o D5.
---
Aplique [desenvolver-dominio](../desenvolver-dominio/SKILL.md).

Não decompile o D5. Seu código fonte está em [.agents/d5/framework](../../d5/framework). O código do exemplo está em [.agents/d5/exemplo](../../d5/exemplo).

Objetos de valor que possuem um único atributo de tipo String herdam de [ObjetoValorRegexAbstrato](../../d5/framework/dominio/src/main/java/dev/sauloaraujo/d5/framework/dominio/impl/ObjetoValorRegexAbstrato.java) (ver [UnidadeFederativaSigla](../../d5/exemplo/dominio-geografia/src/main/java/com/example/exemplo/dominio/geografia/unidadefederativa/UnidadeFederativaSigla.java)), quando os valores válidos para o atributo podem ser descritos por uma expressão regular. Caso contrário, devem herdar de [ObjetoValorStringAbstrato](../../d5/framework/dominio/src/main/java/dev/sauloaraujo/d5/framework/dominio/impl/ObjetoValorStringAbstrato.java) (ver [ClienteIdentificador](../../d5/exemplo/dominio-crm/src/main/java/com/example/exemplo/dominio/crm/cliente/ClienteIdentificador.java)).

Objetos de valor com mais de um atributo devem implementar [IObjetoValor](../../d5/framework/dominio/src/main/java/dev/sauloaraujo/d5/framework/dominio/IObjetoValor.java) diretamente (ver [Endereco](../../d5/exemplo/dominio-crm/src/main/java/com/example/exemplo/dominio/crm/cliente/Endereco.java)).

Objetos de valor que identificam entidades herdam de ObjetoValorStringAbstrato, diretamente ou através de ObjetoValorRegexAbstrato, e implementam [IIdentificador](../../d5/framework/dominio/src/main/java/dev/sauloaraujo/d5/framework/dominio/IIdentificador.java) (ver [ClienteIdentificador](../../d5/exemplo/dominio-crm/src/main/java/com/example/exemplo/dominio/crm/cliente/ClienteIdentificador.java) e [MunicipioCodigoIbge](../../d5/exemplo/dominio-geografia/src/main/java/com/example/exemplo/dominio/geografia/municipio/MunicipioCodigoIbge.java)).

Objetos de valor que herdam de ObjetoValorStringAbstrato possuem um construtor público com um único parâmetro do tipo String, através do qual o framework os cria a partir dos parâmetros de caminho e de consulta e dos valores do corpo JSON das requisições. Quando o valor possui um tamanho fixo ou máximo, ele é declarado em uma constante pública TAMANHO, utilizada também pela entidade JPA (ver [UnidadeFederativaSigla](../../d5/exemplo/dominio-geografia/src/main/java/com/example/exemplo/dominio/geografia/unidadefederativa/UnidadeFederativaSigla.java)).

Entidades raiz implementam [IAgregado](../../d5/framework/dominio/src/main/java/dev/sauloaraujo/d5/framework/dominio/IAgregado.java). Ver [UnidadeFederativa](../../d5/exemplo/dominio-geografia/src/main/java/com/example/exemplo/dominio/geografia/unidadefederativa/UnidadeFederativa.java) e [Cliente](../../d5/exemplo/dominio-crm/src/main/java/com/example/exemplo/dominio/crm/cliente/Cliente.java) para exemplos de entidades com identificador natural e artificial, respectivamente. No primeiro caso, o construtor da entidade recebe o identificador como parâmetro. No segundo caso, recebe o identificador envolto em um Optional.

O framework cria entidades raiz e objetos de valor com mais de um atributo a partir do corpo JSON das requisições através de seus construtores públicos, associando cada propriedade JSON ao parâmetro do construtor de mesmo nome. Por isso, os parâmetros do construtor têm os nomes das propriedades expostas pelos getters.

Serviços herdam de [AgregadoServicoAbstrato](../../d5/framework/dominio/src/main/java/dev/sauloaraujo/d5/framework/dominio/impl/AgregadoServicoAbstrato.java) e implementam o método criarAutorizacoes. [UnidadeFederativaServico](../../d5/exemplo/dominio-geografia/src/main/java/com/example/exemplo/dominio/geografia/unidadefederativa/UnidadeFederativaServico.java) exemplifica um serviço apenas com ações herdadas (incluir, buscar, alterar e excluir). [MunicipioServico](../../d5/exemplo/dominio-geografia/src/main/java/com/example/exemplo/dominio/geografia/municipio/MunicipioServico.java) exemplifica um serviço com uma ação adicional (extinguir), caso em que é necessário criar uma subclasse de [AgregadoAutorizacoes](../../d5/framework/dominio/src/main/java/dev/sauloaraujo/d5/framework/dominio/impl/AgregadoAutorizacoes.java) como [MunicipioAutorizacoes](../../d5/exemplo/dominio-geografia/src/main/java/com/example/exemplo/dominio/geografia/municipio/MunicipioAutorizacoes.java).

criarAutorizacoes decide, para cada ação, se o usuário pode executá-la, consultando os papéis do usuário com getSegurancaServico().possuiAutorizacao(Autorizacao.**P**.name()), onde Autorizacao é a enumeração de papéis do módulo dominio-compartilhado e **P** é um papel.

As ações herdadas não são sobrescritas. As regras de negócio que elas aplicam são implementadas sobrescrevendo os pontos de extensão de AgregadoServicoAbstrato:

- validarInclusao e validarAlteracao, para validações de argumento específicas de cada ação e, na alteração, para validações do estado do agregado existente;
- validarInclusaoAlteracao, para validações em relação aos demais agregados, como a unicidade de uma chave candidata ou a existência de um agregado referenciado;
- validarExclusao, para validações da exclusão, como a de que o agregado não é referenciado por outros.

Nesses métodos, validarExistencia e validarInexistencia, que recebem um predicado, um argumento e o modelo da mensagem de erro, verificam a existência e a unicidade com métodos do repositório. As mensagens de erro e seus modelos, no formato de String.format, são constantes públicas do serviço, por exemplo, IDENTIFICADOR_INEXISTENTE_MODELO, reutilizadas pela implementação do repositório e pelos testes.

Uma ação adicional é um método do serviço que chama validarAutorizacao com o predicado que seleciona sua autorização, valida a existência do agregado com validarExistencia, aplica a regra de negócio através de um método da entidade raiz e grava o resultado com o repositório (ver o método extinguir de MunicipioServico).

Serviços e implementações de repositórios não são anotados com anotações do Spring, como @Component, @Service ou @Repository: o framework os registra automaticamente como beans.

No modelo do projeto de software, por exigência do Context Mapper, os repositórios devem ser nomeados com o sufixo Repository. No código, interfaces de repositórios são nomeadas com prefixo I e sufixo Repositorio (por exemplo, I**A**Repositorio, onde **A** é um agregado).

Interfaces de repositório devem estender [IAgregadoRepositorio](../../d5/framework/dominio/src/main/java/dev/sauloaraujo/d5/framework/dominio/spi/IAgregadoRepositorio.java) e declarar métodos de consulta adicionais aos herdados e necessários à implementação de regras de negócio em serviços (ver [IMunicipioRepositorio](../../d5/exemplo/dominio-geografia/src/main/java/com/example/exemplo/dominio/geografia/municipio/spi/IMunicipioRepositorio.java)). Um método que verifica a unicidade de uma chave candidata recebe também o identificador opcional do agregado a ser ignorado, para que, na alteração, o agregado não seja comparado consigo mesmo, por exemplo, existePorCpf(Cpf cpf, Optional<ClienteIdentificador> identificadorIgnorado).
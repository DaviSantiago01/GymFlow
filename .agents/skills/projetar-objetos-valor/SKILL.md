---
name: projetar-objetos-valor
description: Decide quais conceitos são objetos de valor, onde são declarados e como as entidades são identificadas, por chaves naturais ou identificadores artificiais. Use durante projetar-software.
---
Conceitos que não possuem identidade são projetados como objetos de valor. Eles podem ser formados por um único atributo, por exemplo, CEP, CNPJ, CPF, e-mail e ISBN ou por mais de um atributo, por exemplo, endereço é formado por logradouro, número, complemento, bairro, município, unidade federativa e CEP.

Para evitar o antipadrão objetos anêmicos, regras de negócio que dependem apenas dos atributos de um objeto de valor e/ou de parâmetros de métodos são encapsuladas em métodos do objeto de valor. De maneira similar, conceitos que podem ser representados apenas por uma cadeia de caracteres, mas que possuem regras de negócio associadas, são projetados como objetos de valor. É o caso, por exemplo, do CPF, que é formado por 11 dígitos, sendo os últimos dois calculados a partir dos anteriores. Entretanto, se a única regra de negócio associada ao conceito for sua obrigatoriedade ou tamanho máximo, ele deve ser projetado como um atributo do tipo String.

Uma entidade pode possuir chaves candidatas, isto é, atributos ou conjuntos de atributos cujos valores não se repetem entre suas instâncias, por exemplo, o CPF de um cliente ou a sigla de uma unidade federativa. A unicidade de uma chave candidata é uma regra de negócio validada pelo serviço da entidade, com um método do repositório que verifica se existe outra instância com o mesmo valor.

O identificador de uma entidade **E** é escolhido entre suas chaves candidatas formadas por um único conceito do domínio e, nesse caso, é um objeto de valor natural, cujo nome é o do próprio conceito, por exemplo, o código IBGE de um município. A escolha considera, além da unicidade, se a chave é adequada para identificar **E** em todo o sistema, onde ela aparece em URLs, registros de log e referências de outras entidades: o identificador não muda e não é um dado sensível. Por isso, por exemplo, o CPF, um dado sensível, não é uma boa escolha para identificar um cliente.

Quando nenhuma chave candidata de **E** é adequada, existe um objeto de valor artificial chamado **E**Identificador que possui um único atributo do tipo String chamado valor.

Objetos de valor são declarados no módulo do contexto delimitado que corresponde ao subdomínio **S** ao qual eles pertencem, mesmo que sejam referenciados em outros subdomínios. Apenas objetos de valor que não pertencem a nenhum subdomínio específico e são comuns a todos eles são declarados no módulo compartilhado.

Atributos opcionais são declarados `nullable`.

Coleções em que a ordem é importante são declaradas como `List`, caso contrário, como `Collection`.

Ver [projetar-entidades](../projetar-entidades/SKILL.md).
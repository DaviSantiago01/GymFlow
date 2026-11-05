---
name: desenvolver-infraestrutura-dominio-d5
description: Implementa os repositórios da camada de domínio de uma funcionalidade com as classes do framework D5 e Spring Data JPA. Use depois de projetar o banco de dados, em vez de desenvolver-infraestrutura-dominio, em projetos que usam o D5.
---
Aplique [desenvolver-infraestrutura-dominio](../desenvolver-infraestrutura-dominio/SKILL.md).

Não decompile o D5. Seu código fonte está em [.agents/d5/framework](../../d5/framework). O código do exemplo está em [.agents/d5/exemplo](../../d5/exemplo).

A entidade JPA de um agregado **A** chama-se **A**Jpa e implementa [IAgregadoJpa](../../d5/framework/infraestrutura/src/main/java/dev/sauloaraujo/d5/framework/infraestrutura/IAgregadoJpa.java) (ver [MunicipioJpa](../../d5/exemplo/infraestrutura/src/main/java/com/example/exemplo/infraestrutura/geografia/municipio/MunicipioJpa.java)). Ela possui um construtor protegido sem parâmetros, exigido pelo JPA, e um construtor público com todos os atributos. As restrições das colunas que o Bean Validation consegue expressar são declaradas com @NotNull e @Size(max = ...), em vez de nullable e length na @Column, que declara apenas o que essas anotações não expressam, como name, unique e updatable. Quando o tamanho de uma coluna é o de um objeto de valor, @Size usa a constante TAMANHO do objeto de valor. Essas anotações expressam apenas restrições da persistência: as regras do domínio, já garantidas pelo agregado e por seus objetos de valor, não são repetidas na entidade JPA.

A unicidade de uma chave candidata, validada pelo serviço, também é declarada no banco de dados, com unique = true na @Column ou com uma @UniqueConstraint na @Table, para garanti-la sob concorrência.

A referência de um agregado a outro, feita no domínio pelo identificador, é mapeada na entidade JPA por uma associação @ManyToOne(fetch = FetchType.LAZY) com a entidade JPA do agregado referenciado, cuja coluna é uma chave estrangeira (ver [ClienteJpa](../../d5/exemplo/infraestrutura/src/main/java/com/example/exemplo/infraestrutura/crm/cliente/ClienteJpa.java)).

A interface Spring Data JPA de **A** chama-se I**A**RepositorioJpa, estende JpaRepository e declara, com @Query, as consultas usadas pelos métodos adicionais do repositório (ver [IMunicipioRepositorioJpa](../../d5/exemplo/infraestrutura/src/main/java/com/example/exemplo/infraestrutura/geografia/municipio/IMunicipioRepositorioJpa.java)).

Implementações de repositórios herdam de [AgregadoRepositorioAbstrato](../../d5/framework/infraestrutura/src/main/java/dev/sauloaraujo/d5/framework/infraestrutura/AgregadoRepositorioAbstrato.java) e implementam os métodos paraJpa, paraAgregado e paraIdentificadorJpa, que convertem entre o agregado e a entidade JPA (ver [MunicipioRepositorio](../../d5/exemplo/infraestrutura/src/main/java/com/example/exemplo/infraestrutura/geografia/municipio/MunicipioRepositorio.java)). paraJpa preenche as associações com getReferenceById da interface Spring Data JPA do agregado referenciado, sem carregá-lo. O construtor recebe as interfaces Spring Data JPA utilizadas e passa à superclasse o modelo da mensagem de identificador inexistente declarado no serviço do agregado.

Quando o identificador artificial do agregado corresponde a uma coluna autoincrementada, a entidade JPA usa um identificador Long anotado com @GeneratedValue(strategy = GenerationType.IDENTITY), e o repositório converte o identificador do domínio, uma String, para Long. Um valor que não é um Long não identifica nenhum agregado: paraIdentificadorJpa lança AgregadoInexistenteExcecao, e existePorIdentificador é sobrescrito para devolver false (ver [ClienteRepositorio](../../d5/exemplo/infraestrutura/src/main/java/com/example/exemplo/infraestrutura/crm/cliente/ClienteRepositorio.java)).

Implementações de repositórios não são anotadas com anotações do Spring, como @Component ou @Repository: o framework as registra automaticamente como beans.

Os testes de um repositório chamam-se **A**RepositorioTest, ficam no mesmo pacote da implementação, em src/test/java, e instanciam o repositório com as interfaces Spring Data JPA injetadas pelo @DataJpaTest (ver [MunicipioRepositorioTest](../../d5/exemplo/infraestrutura/src/test/java/com/example/exemplo/infraestrutura/geografia/municipio/MunicipioRepositorioTest.java)).
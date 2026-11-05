# treinoconectacore

Núcleo do sistema Treino Conecta para gerenciamento de alunos, professores e academias.

## Engenharia de software
- MDE (Model-Driven Engineering)
- DDD (Domain-Driven Design)
- Evitar antipadrão modelo de domínio anêmico
- CQRS (Command Query Responsibility Segregation)
- Arquitetura limpa
- SPI (Service Provider Interface)
- BDD (Behaviour-Driven Development)
- Programação defensiva
- ORM (Object-Relational Mapping)
- BFF (Backend For Frontend)

## Tecnologias
- Context Mapper 6.12.0
- Java 25
- Cucumber 7.34.8
- Mockito 5.23.0
- JSpecify 1.0
- DBML (Database Markup Language) 10.1.1
- Spring Boot 4.1.1
- Angular 22.1.4
- NG Bootstrap 21.0.0
- Bootstrap 5.3.8
- D5 1.0.0-SNAPSHOT

## Subdomínios
- core (subdomínio principal): Núcleo do sistema Treino Conecta para gerenciamento de alunos, professores e academias.

## Maven
- groupId: br.edu.cesar.treinoconecta
- artifactId do projeto raiz: treinoconectacore-pai
- version: 1.0.0-SNAPSHOT

### Módulos
- dominio-compartilhado (conceitos que não pertencem a nenhum subdomínio específico e são comuns a todos eles, por exemplo, os papéis de autorização)
- dominio-**S** (entidades, objetos de valor, serviços e SPIs de um subdomínio **S**, por exemplo, interfaces de repositórios)
- aplicacao (serviços de aplicação, projeções de entidades e SPIs da camada de aplicação, por exemplo, interfaces de repositórios de aplicação)
- infraestrutura (implementações das SPIs)
- ui-backend (controladores REST do backend para o frontend)
- ui-frontend (aplicação Angular do frontend)

Um conceito que pertence a um subdomínio fica no módulo dele, mesmo que outros subdomínios o usem. Todo módulo dominio-**S** depende de dominio-compartilhado e pode depender também do módulo de outro subdomínio cujos conceitos ele referencia, desde que as dependências entre módulos de subdomínio não formem um ciclo.

## Documentação e modelos
Para cada subdomínio **S**:

- docs/**S**/funcionalidades/*.md (especificações de funcionalidades de **S** no formato Markdown)
- dominio-**S**/src/test/resources/features/*.feature (especificações de testes de **S** na linguagem Gherkin)
- docs/**S**/projeto.cml (modelo de projeto de software de **S** na linguagem do Context Mapper)
- docs/**S**/projeto.dbml (modelo de projeto de banco de dados relacional de **S** na linguagem DBML)

Os arquivos [compartilhado/projeto.cml](docs/compartilhado/projeto.cml) e [projeto.cml](docs/projeto.cml) complementam o modelo de projeto de software. Já o arquivo [projeto.dbml](docs/projeto.dbml) complementa o modelo do projeto de banco de dados.

## Idioma
Todos os artefatos do projeto (documentação, modelos, código, comentários, etc.) devem ser escritos em português do Brasil, sempre que possível.

## Pacotes
O código de objetos de valor, entidades e serviço de um agregado **A** de um subdomínio **S** pertencem ao pacote br.edu.cesar.treinoconecta.treinoconectacore.dominio.**S**.**A**.

A interface do repositório de **A** pertence ao pacote br.edu.cesar.treinoconecta.treinoconectacore.dominio.**S**.**A**.spi.

Projeções e serviços de aplicação de **A** pertencem ao pacote br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.**S**.**A**.

A interface do repositório de aplicação de **A** pertence ao pacote br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.**S**.**A**.spi.

A implementação dos repositórios de **A** pertence ao pacote br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.**S**.**A**.

O controlador REST de **A** pertence ao pacote br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.**S**.**A**.

Todos os pacotes são anotados com @NullMarked.

## Processo de trabalho
1) [Especificar funcionalidade](.agents/skills/especificar-funcionalidade/SKILL.md)
2) [Especificar testes](.agents/skills/especificar-testes/SKILL.md)
3) [Projetar software](.agents/skills/projetar-software/SKILL.md)
4) [Desenvolver camada de domínio](.agents/skills/desenvolver-dominio-d5/SKILL.md)
5) [Desenvolver testes](.agents/skills/desenvolver-testes/SKILL.md)
6) [Projetar banco de dados](.agents/skills/projetar-banco/SKILL.md)
7) [Desenvolver camada de infraestrutura para a camada de domínio](.agents/skills/desenvolver-infraestrutura-dominio-d5/SKILL.md)
8) [Desenvolver backend](.agents/skills/desenvolver-backend-d5/SKILL.md)
9) [Desenvolver frontend](.agents/skills/desenvolver-frontend/SKILL.md)

## Execução
mvn spring-boot:test-run -pl ui-backend
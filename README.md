# SGA — Sistema de Gerenciamento de Academia

Base estrutural de um sistema de gerenciamento de academia, seguindo a arquitetura
do projeto de referência [sgb-2025-01](https://github.com/profsauloaraujo/sgb-2025-01).

> **Este projeto contém apenas a fundação.** As funcionalidades (cadastros, CRUD,
> autenticação, telas, regras de negócio) serão implementadas posteriormente.

## Arquitetura

DDD + arquitetura hexagonal, organizada em módulos Maven:

| Módulo | Responsabilidade |
|---|---|
| `pom.xml` (raiz) | Agregador dos módulos |
| `pai` | POM pai — herda o `spring-boot-starter-parent` e centraliza versões |
| `dominio-compartilhado` | *Shared kernel* — identificadores usados por mais de um contexto |
| `dominio-administracao` | Contexto delimitado: `Aluno`, `Instrutor` |
| `dominio-treinamento` | Contexto delimitado: `Treino`, `Exercicio` |
| `aplicacao` | Lado de consulta: projeções (`*Resumo`) e serviços de aplicação |
| `infraestrutura` | Persistência JPA (entidades, repositórios Spring Data, mapeador) |
| `apresentacao-backend` | API REST e configuração da aplicação |

O domínio é composto por **POJOs puros, sem nenhuma anotação JPA**. As entidades JPA
são classes espelho que vivem em `infraestrutura`, e o `JpaMapeador` (ModelMapper)
converte entre os dois mundos. Agregados referenciam outros agregados **por
identificador** (`AlunoId`, `InstrutorId`, `ExercicioId`), nunca por objeto.

## Entidades e relacionamentos

```
Aluno 1 ──< N Treino N >── 1 Instrutor
                 │
                 N
                 ⋮  (@ManyToMany, ordenado)
                 N
             Exercicio
```

* `Aluno` **@OneToMany** `Treino` (lado inverso de `Treino.aluno`)
* `Instrutor` **@OneToMany** `Treino` (lado inverso de `Treino.instrutor`)
* `Treino` **@ManyToOne** `Aluno` e **@ManyToOne** `Instrutor`
* `Treino` **@ManyToMany** `Exercicio`, com tabela de junção `TREINO_EXERCICIO`
  e ordem preservada via `@OrderColumn`

## Executando

Por padrão a aplicação usa **H2 em arquivo**, sem necessidade de instalar nada:

```
mvn clean install
mvn -pl apresentacao-backend spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

Para usar PostgreSQL, suba o banco e inverta os comentários em
`apresentacao-backend/src/main/resources/application.properties`:

```
docker compose up
```

## Endpoints de demonstração

Existem apenas para comprovar que as camadas estão conectadas — não são
funcionalidades do sistema:

| Endpoint | Demonstra |
|---|---|
| `GET /backend/aluno/pesquisa` | Projeção via *derived query* |
| `GET /backend/aluno/pesquisa?expandir=true` | Projeção via JPQL com `COUNT` e `GROUP BY` |
| `GET /backend/instrutor/pesquisa` | Projeção via *derived query* |
| `GET /backend/treino/pesquisa` | Projeção via *derived query* |
| `GET /backend/treino/pesquisa?expandir=true` | JPQL com `JOIN` e `SIZE` + projeções aninhadas |
| `GET /backend/exercicio/pesquisa` | Projeção via *derived query* |
| `GET /backend/exercicio/treino/{id}/pesquisa` | JPQL navegando a `@ManyToMany` com `INDEX` |

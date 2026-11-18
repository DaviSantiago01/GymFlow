# GymFlow

O GymFlow é um sistema acadêmico de gestão de academias. O gestor acompanha a operação da unidade, os professores prescrevem treinos e avaliam a evolução, e os alunos consultam e registram sua rotina. O projeto foi desenvolvido para a disciplina de Requisitos, Projeto de Software e Validação.

## O que já funciona

| Perfil | Recursos disponíveis |
| --- | --- |
| **Gestor** | Gerencia planos, adesões, mensalidades, profissionais e equipamentos. Registra inspeções, problemas e ordens de manutenção. |
| **Professor** | Consulta alunos, prescreve e acompanha treinos, registra avaliações físicas e define metas. |
| **Aluno** | Cria a própria conta, consulta treino e evolução, registra a execução, vê plano e mensalidades, reserva armários, participa de grupos e avalia treinos concluídos. |

O [cadastro administrativo de alunos](docs/core/funcionalidades/F005.md) e o [controle de frequência](docs/core/funcionalidades/F006.md) estão especificados e têm protótipos. O agendamento de aulas aparece no menu como opção ilustrativa.

## Executar localmente

Instale JDK 25, Maven 3.9+, Node.js 24 e npm. Configure `JAVA_HOME` para o JDK 25 e execute os comandos do backend na raiz do repositório.

```powershell
mvn package
java -jar ui-backend/target/treinoconectacore-ui-backend-1.0.0-SNAPSHOT.jar --spring.profiles.active=desenvolvimento
```

Abra outro terminal na raiz para iniciar o frontend.

```powershell
cd ui-frontend/src/main/angular
npm ci
npm start -- --host 127.0.0.1
```

Abra [http://127.0.0.1:4200](http://127.0.0.1:4200). O Angular encaminha `/backend` para a API na porta 8080. O Flyway prepara o banco H2 local, que fica em `data/treino-conecta.mv.db`.

### Contas de demonstração

| Perfil | Usuário | Senha |
| --- | --- | --- |
| Gestor | `gestor` | `gestor` |
| Professor | `professor` | `professor` |
| Aluno | `aluno` | `aluno` |

Use o campo **Usuário** no login. Execute com o perfil `desenvolvimento` para carregar as contas e os dados de demonstração. O perfil `producao` desativa a criação de contas de demonstração.

A conta `aluno` já tem plano de treino, histórico de cargas, avaliações físicas, meta, grupo de treino, histórico de armário e mensalidades. A conta `professor` pode acompanhar e editar o plano desse aluno. A conta `gestor` pode consultar a adesão, as cobranças e os dados de profissionais de exemplo. A carga de dados fictícios é aplicada uma vez por banco, para preservar as alterações feitas durante a demonstração.

No perfil Gestor, o módulo de equipamentos inclui exemplos com prazos de inspeção diferentes e uma ordem de manutenção em andamento.

Em produção, o backend recebe `TREINO_BANCO_URL`, `TREINO_BANCO_USUARIO` e `TREINO_BANCO_SENHA` pelo ambiente. A aplicação usa HTTPS para enviar os cookies seguros. As migrações desta versão foram verificadas com H2.

## Arquitetura e documentação

O backend usa **Java 25, Spring Boot, Maven e o framework D5**. O frontend usa **Angular**, e a persistência local usa **JPA, Flyway e H2**. A autenticação por sessão identifica o perfil e o backend valida as permissões de cada operação.

| Módulo | Responsabilidade |
| --- | --- |
| `dominio-compartilhado` e `dominio-core` | Papéis, agregados, regras de negócio e interfaces de repositório |
| `aplicacao` | Consultas e projeções |
| `infraestrutura` | Implementações de repositório e migrações do banco |
| `ui-backend` e `ui-frontend` | API Spring Boot e interface Angular |

- [Especificações de funcionalidades](docs/core/funcionalidades) e [cenários Gherkin](dominio-core/src/test/resources/features)
- [Modelo Context Mapper](docs/core/projeto.cml) e [modelo de dados DBML](docs/core/projeto.dbml)
- Contratos HTTP de [equipamentos](docs/core/api-equipamentos.md), [armários](docs/core/api-armarios.md), [avaliações](docs/core/api-avaliacoes.md) e [grupos](docs/core/api-grupos.md)
- [Evidências de validação](docs/validacao)
- [Rastreabilidade e validação da gestão de equipamentos](docs/validacao/EQUIPAMENTOS.md)

## Testes

Na raiz, execute `mvn test`. Na pasta `ui-frontend/src/main/angular`, use `npm test -- --watch=false` e `npm run build`. Para os testes de navegador, mantenha backend e frontend em execução nas portas 8080 e 4200 e use `npm run test:e2e` com Microsoft Edge instalado.

---
name: desenvolver-backend
description: Desenvolve as projeções, consultas, repositórios e serviços de aplicação e o controlador REST de uma funcionalidade. Use depois de desenvolver a camada de infraestrutura para a camada de domínio, em projetos que não usam o D5.
---
Desenvolver o backend de uma funcionalidade **F** associada a um agregado **A** de um subdomínio **S** consiste em:

- desenvolver projeções e repositórios de aplicação e serviços de aplicação utilizados por essas operações no módulo da camada de aplicação;
- implementar os repositórios de aplicação no módulo da camada de infraestrutura;
- desenvolver as operações utilizadas pelas rotas de **F** no controlador **A**Controlador.

O frontend depende da autenticação do backend, desenvolvida, se o projeto ainda não a possui, no pacote ui.backend.seguranca do módulo ui-backend:

- GET /backend/autenticacao autentica com HTTP Basic, quando a requisição tem o cabeçalho Authorization, mantém a sessão em um cookie e responde o JSON do usuário autenticado: a propriedade usuario, com seu nome, e, para cada agregado acessível pelo menu do frontend, uma propriedade com o nome do agregado iniciado em minúscula e as autorizações incluir, buscar, alterar e excluir do usuário sobre ele;
- POST /backend/sair encerra a sessão e responde 204;
- o token CSRF é gravado no cookie XSRF-TOKEN, legível pelo JavaScript, e recebido no cabeçalho X-XSRF-TOKEN;
- uma requisição sem sessão válida é respondida com 401, sem o cabeçalho WWW-Authenticate, para que o navegador não exiba sua própria janela de login;
- erros são respondidos com o JSON {"mensagem": "..."}, que o frontend exibe ao usuário.
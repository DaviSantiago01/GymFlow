# F002 — Cadastro de conta de aluno

O visitante pode criar uma conta em `/cadastro`, acessível pelo link da tela de login.
Informe nome, nome de usuário, senha e confirmação. Após criar a conta, a tela oferece
o acesso ao login com o usuário preenchido. A conta já pode reservar armários e consultar
seu histórico pelo mesmo mecanismo de sessão dos demais alunos.

## RN001 — Dados válidos

Nome entre 2 e 100 caracteres. Usuário entre 3 e 40 caracteres, começando com letra
ou número e permitindo letras ASCII, números, ponto, hífen e sublinhado. O usuário
é normalizado para minúsculas e sem espaços externos. Senha com pelo menos 8 caracteres
e até 72 bytes UTF-8, confirmada por um segundo campo. A senha não é normalizada.

## RN002 — Identidade única e persistência atômica

O usuário não pode se repetir. A restrição UNIQUE de `ALU_ALUNO.ALU_USUARIO` protege
solicitações concorrentes. Aluno e credencial são gravados na mesma transação D5;
uma falha na credencial desfaz também o aluno. Usa as tabelas já existentes da V1,
sem alterar ou recriar o banco e sem perder usuários ou reservas.

## RN003 — Credencial protegida e acesso de aluno

A senha é gravada com o `PasswordEncoder` existente, usando hash BCrypt com sal.
O cadastro não aceita papéis administrativos: todas as novas contas autenticam com
o papel USUARIO. O cliente nunca recebe hash ou senha na resposta. O código Basic
do frontend usa UTF-8 para permitir senhas acentuadas.

## Operações

- `GET /backend/cadastro`: público; prepara o formulário e permite a emissão do cookie CSRF pelo D5.
- `POST /backend/cadastro`: público, exigindo CSRF; recebe `nome`, `usuario`, `senha`, `confirmacaoSenha`.
- Sucesso: HTTP 201 com `{"nome":"Ana Silva","usuario":"ana.silva"}`.
- Dados inválidos: 422. Usuário duplicado: 409. CSRF inválido: 403.

Login permanece em `GET /backend/autenticacao`, e aceita o nome de usuário sem
diferenciar maiúsculas de minúsculas. Cadastro e login são operações separadas.

## Verificação

`CadastroIntegracaoTest` testa CSRF, gravação de hash, login e reserva reais,
duplicidade, dados inválidos, concorrência e rollback da criação do aluno.
`e2e/cadastro.spec.ts` cria a conta pela interface, entra com senha acentuada,
reserva, encerra, sai, entra novamente e consulta o histórico.

# F008 — Gerenciamento de professores e personal trainers

Permite cadastrar, pesquisar, alterar e inativar professores e personal trainers; registrar dados profissionais,
especialidades e disponibilidade de trabalho; e associar personal trainers aos alunos da academia.

Entidades: `Profissional` (com as entidades `Especialidade` e `DisponibilidadeProfissional`), `VinculoPersonalAluno`
e `Aluno` (existente, só referenciado). Quem opera é o administrador da academia (papel `ADMINISTRADOR`).

## Decisões e limites
- `Profissional` é o cadastro de RH da academia. Ele não altera os cadastros existentes `Professor` (perfis avaliados em
  F003) nem `Personal` (contas de login que prescrevem treinos em F003/F004). Unificar esses cadastros fica como
  evolução futura, com migração própria.
- CPF é guardado só com os dígitos e validado pelos dígitos verificadores. CREF segue o formato `000000-G/UF` (G
  graduado, P provisionado) com UF válida. O contato é texto livre (telefone ou e-mail), com até 100 caracteres.
- Especialidade é comparada sem diferenciar maiúsculas, acentos e espaços repetidos.
- A disponibilidade é semanal (dia da semana, horário inicial e final). Limite de carga semanal: 44 horas
  (`ParametrosProfissionais`).
- Profissionais nunca são excluídos: são inativados e podem ser reativados. Não existe rota de exclusão.

## Regras
- RN1: nome (2 a 100 caracteres), CPF, CREF, contato e ao menos uma classificação: `PROFESSOR`, `PERSONAL_TRAINER` ou
  ambas.
- RN2: CPF e CREF únicos entre todos os profissionais, ativos ou não (verificado no domínio e por índices únicos).
- RN3: uma especialidade não se repete no mesmo profissional (chave normalizada também é chave primária no banco).
- RN4: horário inicial anterior ao final. Não pode haver períodos sobrepostos no mesmo dia (um pode começar quando o
  outro termina).
- RN5: ao incluir, alterar ou remover uma disponibilidade, a carga semanal é recalculada e não pode passar de 44 horas.
- RN6: profissional inativo não recebe novos vínculos, especialidades nem disponibilidades.
- RN7: só um personal trainer ativo pode ser vinculado a um aluno, e não pode haver dois vínculos ativos entre o mesmo
  par (índice único no banco). Retirar a classificação de personal trainer exige encerrar os vínculos ativos.
- RN8: um profissional com vínculos ativos não pode ser inativado. Vínculos encerrados permanecem no histórico.

## Contrato HTTP
Todos os POSTs exigem o cabeçalho `X-XSRF-TOKEN`.

| Método | Caminho | Operação |
|---|---|---|
| GET | `/backend/core/profissional/pesquisa?busca=&classificacao=&situacao=` | Busca por nome, CPF (com ou sem pontuação) ou CREF |
| GET | `/backend/core/profissional/{id}/visualizacao` | Dados, carga semanal, vínculos e alunos disponíveis |
| POST | `/backend/core/profissional/cadastrar` | `{nome, cpf, cref, contato, classificacoes[]}` |
| POST | `/backend/core/profissional/{id}/editar` | Mesmo corpo do cadastro |
| POST | `/backend/core/profissional/{id}/especialidades/adicionar` | `{nome}` |
| POST | `/backend/core/profissional/{id}/especialidades/remover` | `{nome}` |
| POST | `/backend/core/profissional/{id}/disponibilidades/adicionar` | `{dia: MONDAY..SUNDAY, inicio: "08:00", fim: "12:00"}` |
| POST | `/backend/core/profissional/{id}/disponibilidades/{disponibilidade}/alterar` | Mesmo corpo |
| POST | `/backend/core/profissional/{id}/disponibilidades/{disponibilidade}/remover` | Remover |
| POST | `/backend/core/profissional/{id}/inativar` e `/reativar` | Situação |
| GET | `/backend/core/vinculo-personal-aluno/pesquisa?profissional=&aluno=&situacao=` | Vínculos |
| POST | `/backend/core/vinculo-personal-aluno/vincular` | `{profissional, aluno}` |
| POST | `/backend/core/vinculo-personal-aluno/{id}/encerrar` | Encerrar |

## Testes
- `dominio-core/src/test/resources/features/profissionais.feature` (RN1 a RN8 e autorização).
- `ProfissionaisRepositorioTest` (especialidades e disponibilidades persistidas, unicidade de CPF, CREF e vínculo ativo).
- `ProfissionaisIntegracaoTest` (HTTP, mensagens das regras, permissões).

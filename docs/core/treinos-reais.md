# Cadastro e integração de treinos reais

O histórico não é uma lista fixa: consulta TRN_TREINO no banco, filtrando pelo
Aluno autenticado. Professores e alunos precisam existir. Cada linha é o registro
de um treino de um aluno, permitindo dois treinos do mesmo professor com avaliações
independentes. Uma futura turma pode gerar uma linha para cada aluno participante.

O registro de frequência é responsabilidade da academia; o aluno não pode declarar
um treino como concluído para liberar a própria avaliação. Por isso os comandos
genéricos de inclusão/edição de Treino são negados ao aluno. Nesta entrega não foi
criada uma interface administrativa de cadastro/frequência.

Uma integração administrativa ou carga autorizada pode usar `ITreinoRepositorio`
em transação, construindo um `Treino` válido, ou uma inserção SQL parametrizada:

```sql
INSERT INTO TRN_TREINO
  (ALU_ID, PRO_ID, TRN_DESCRICAO, TRN_INICIO, TRN_TERMINO,
   TRN_CONCLUIDO, TRN_DEMONSTRACAO)
VALUES (?, ?, ?, ?, ?, ?, FALSE);
```

Os parâmetros representam IDs reais do aluno/professor, descrição de até 120
caracteres, instantes com fuso para início/fim e confirmação de conclusão pela
academia. O término deve ser posterior ao início. Só entram no histórico avaliável
os registros concluídos cujo término já passou. `TRN_DEMONSTRACAO=FALSE` identifica
um registro real; o aluno não recebe um formulário para trocar esse indicador.

No H2 em arquivo local, use o procedimento administrativo com o backend parado,
cópia de segurança e o driver H2 compatível, ou uma integração transacional dentro
da aplicação. Não tente abrir o arquivo simultaneamente por outro processo.
Não grave diretamente AVP_AVALIACAO_PROFESSOR: as avaliações passam pelo comando
autenticado `/backend/core/treino/{id}/avaliar` e suas regras de negócio.

Os exemplos são outra origem de dados, marcada explicitamente como demonstração,
com professores também de demonstração. Carregá-los não substitui nem remove os
treinos reais. O histórico mostra ambas as origens e calcula as avaliações a partir
dos registros efetivamente criados pelos usuários.

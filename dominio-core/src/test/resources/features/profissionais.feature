# language: pt
Funcionalidade: Gerenciamento de professores e personal trainers
  Para organizar a equipe da academia
  Como administrador
  Quero manter profissionais, suas especialidades, sua disponibilidade e os vínculos com alunos

  Contexto:
    Dado que estou autenticado como administrador da equipe
    E que hoje é "2026-11-17" para a equipe
    E que existe o aluno "1" para a equipe

  Cenário: RN1 - Profissional com dados obrigatórios é cadastrado ativo
    Quando eu cadastrar o profissional "Rafael Souza" com CPF "529.982.247-25", CREF "123456-G/PE" e classificação "PERSONAL_TRAINER"
    Então o profissional "Rafael Souza" fica "ATIVO" com CPF "52998224725" e CREF "123456-G/PE"

  Esquema do Cenário: RN1 - Dados obrigatórios do profissional
    Quando eu cadastrar o profissional "<nome>" com CPF "<cpf>", CREF "<cref>" e classificação "<classificacao>"
    Então a operação da equipe é rejeitada com a mensagem "<mensagem>"

    Exemplos:
      | nome         | cpf            | cref        | classificacao    | mensagem                                                            |
      |              | 529.982.247-25 | 123456-G/PE | PROFESSOR        | Informe o nome do profissional com 2 a 100 caracteres               |
      | Rafael Souza | 111.111.111-11 | 123456-G/PE | PROFESSOR        | Informe um CPF válido                                               |
      | Rafael Souza | 529.982.247-24 | 123456-G/PE | PROFESSOR        | Informe um CPF válido                                               |
      | Rafael Souza | 529.982.247-25 | 12345-G/PE  | PROFESSOR        | Informe o CREF no formato 000000-G/UF                               |
      | Rafael Souza | 529.982.247-25 | 123456-G/XX | PROFESSOR        | Informe o CREF no formato 000000-G/UF                               |
      | Rafael Souza | 529.982.247-25 | 123456-G/PE | NENHUMA          | Informe ao menos uma classificação: professor, personal trainer ou ambos |

  Cenário: RN1 - Profissional pode ser professor e personal trainer ao mesmo tempo
    Quando eu cadastrar o profissional "Camila Rocha" com CPF "390.533.447-05", CREF "654321-G/SP" e classificação "PROFESSOR,PERSONAL_TRAINER"
    Então o profissional "Camila Rocha" possui as classificações "PROFESSOR,PERSONAL_TRAINER"

  Cenário: RN2 - CPF repetido é rejeitado
    Dado o profissional "Rafael Souza" com CPF "529.982.247-25", CREF "123456-G/PE" e classificação "PROFESSOR"
    Quando eu cadastrar o profissional "Outro Nome" com CPF "52998224725", CREF "654321-G/SP" e classificação "PROFESSOR"
    Então a operação da equipe é rejeitada com a mensagem "Já existe outro profissional cadastrado com este CPF"

  Cenário: RN2 - CREF repetido é rejeitado
    Dado o profissional "Rafael Souza" com CPF "529.982.247-25", CREF "123456-G/PE" e classificação "PROFESSOR"
    Quando eu cadastrar o profissional "Outro Nome" com CPF "390.533.447-05", CREF "123456-g/pe" e classificação "PROFESSOR"
    Então a operação da equipe é rejeitada com a mensagem "Já existe outro profissional cadastrado com este CREF"

  Cenário: RN2 - Alterar mantendo o próprio CPF e CREF é permitido
    Dado o profissional "Rafael Souza" com CPF "529.982.247-25", CREF "123456-G/PE" e classificação "PROFESSOR"
    Quando eu alterar o contato do profissional "Rafael Souza" para "rafael@academia.com"
    Então o profissional "Rafael Souza" tem o contato "rafael@academia.com"

  Cenário: RN3 - Especialidade não pode ser associada duas vezes
    Dado o profissional "Rafael Souza" com CPF "529.982.247-25", CREF "123456-G/PE" e classificação "PROFESSOR"
    E que o profissional "Rafael Souza" possui a especialidade "Musculação"
    Quando eu adicionar a especialidade " MUSCULACAO " ao profissional "Rafael Souza"
    Então a operação da equipe é rejeitada com a mensagem "Esta especialidade já está associada ao profissional"

  Cenário: RN3 - Profissional pode ter várias especialidades e removê-las
    Dado o profissional "Rafael Souza" com CPF "529.982.247-25", CREF "123456-G/PE" e classificação "PROFESSOR"
    Quando eu adicionar a especialidade "Musculação" ao profissional "Rafael Souza"
    E eu adicionar a especialidade "Pilates" ao profissional "Rafael Souza"
    E eu remover a especialidade "musculação" do profissional "Rafael Souza"
    Então o profissional "Rafael Souza" possui as especialidades "Pilates"

  Cenário: RN4 - Horário inicial precisa ser anterior ao final
    Dado o profissional "Rafael Souza" com CPF "529.982.247-25", CREF "123456-G/PE" e classificação "PROFESSOR"
    Quando eu adicionar a disponibilidade "MONDAY" das "18:00" às "08:00" ao profissional "Rafael Souza"
    Então a operação da equipe é rejeitada com a mensagem "O horário inicial deve ser anterior ao horário final"

  Cenário: RN4 - Períodos sobrepostos no mesmo dia são rejeitados
    Dado o profissional "Rafael Souza" com CPF "529.982.247-25", CREF "123456-G/PE" e classificação "PROFESSOR"
    E que o profissional "Rafael Souza" está disponível "MONDAY" das "08:00" às "12:00"
    Quando eu adicionar a disponibilidade "MONDAY" das "11:00" às "14:00" ao profissional "Rafael Souza"
    Então a operação da equipe é rejeitada com a mensagem "O período se sobrepõe a outra disponibilidade do profissional no mesmo dia"

  Cenário: RN4 - Períodos encostados ou em dias diferentes são aceitos
    Dado o profissional "Rafael Souza" com CPF "529.982.247-25", CREF "123456-G/PE" e classificação "PROFESSOR"
    E que o profissional "Rafael Souza" está disponível "MONDAY" das "08:00" às "12:00"
    Quando eu adicionar a disponibilidade "MONDAY" das "12:00" às "14:00" ao profissional "Rafael Souza"
    E eu adicionar a disponibilidade "TUESDAY" das "08:00" às "12:00" ao profissional "Rafael Souza"
    Então a carga horária semanal do profissional "Rafael Souza" é de 10 horas

  Cenário: RN5 - Carga semanal acima do limite é rejeitada
    Dado o profissional "Rafael Souza" com CPF "529.982.247-25", CREF "123456-G/PE" e classificação "PROFESSOR"
    E que o profissional "Rafael Souza" trabalha das "06:00" às "14:48" de segunda a sexta
    Então a carga horária semanal do profissional "Rafael Souza" é de 44 horas
    Quando eu adicionar a disponibilidade "SATURDAY" das "08:00" às "08:30" ao profissional "Rafael Souza"
    Então a operação da equipe é rejeitada com a mensagem "A carga horária semanal ultrapassaria o limite de 44 horas"

  Cenário: RN5 - Alterar e remover disponibilidade recalcula a carga
    Dado o profissional "Rafael Souza" com CPF "529.982.247-25", CREF "123456-G/PE" e classificação "PROFESSOR"
    E que o profissional "Rafael Souza" trabalha das "06:00" às "14:00" de segunda a sexta
    Quando eu alterar a disponibilidade de "MONDAY" do profissional "Rafael Souza" para "MONDAY" das "06:00" às "10:00"
    Então a carga horária semanal do profissional "Rafael Souza" é de 36 horas
    Quando eu remover a disponibilidade de "TUESDAY" do profissional "Rafael Souza"
    Então a carga horária semanal do profissional "Rafael Souza" é de 28 horas

  Cenário: RN5 - Alteração que ultrapassa o limite é rejeitada
    Dado o profissional "Rafael Souza" com CPF "529.982.247-25", CREF "123456-G/PE" e classificação "PROFESSOR"
    E que o profissional "Rafael Souza" trabalha das "06:00" às "14:00" de segunda a sexta
    Quando eu alterar a disponibilidade de "MONDAY" do profissional "Rafael Souza" para "MONDAY" das "06:00" às "22:00"
    Então a operação da equipe é rejeitada com a mensagem "A carga horária semanal ultrapassaria o limite de 44 horas"

  Cenário: RN6 - Profissional inativo não recebe vínculos
    Dado o profissional "Rafael Souza" com CPF "529.982.247-25", CREF "123456-G/PE" e classificação "PERSONAL_TRAINER"
    E que o profissional "Rafael Souza" foi inativado
    Quando eu vincular o aluno "1" ao profissional "Rafael Souza"
    Então a operação da equipe é rejeitada com a mensagem "Profissionais inativos não podem receber novos vínculos"

  Cenário: RN6 - Profissional inativo não recebe especialidades
    Dado o profissional "Rafael Souza" com CPF "529.982.247-25", CREF "123456-G/PE" e classificação "PROFESSOR"
    E que o profissional "Rafael Souza" foi inativado
    Quando eu adicionar a especialidade "Pilates" ao profissional "Rafael Souza"
    Então a operação da equipe é rejeitada com a mensagem "Profissionais inativos não podem receber novas associações"

  Cenário: RN7 - Personal trainer é vinculado ao aluno
    Dado o profissional "Rafael Souza" com CPF "529.982.247-25", CREF "123456-G/PE" e classificação "PERSONAL_TRAINER"
    Quando eu vincular o aluno "1" ao profissional "Rafael Souza"
    Então o aluno "1" tem 1 vínculo "ATIVO" com o profissional "Rafael Souza"

  Cenário: RN7 - Somente personal trainer pode ser vinculado
    Dado o profissional "Rafael Souza" com CPF "529.982.247-25", CREF "123456-G/PE" e classificação "PROFESSOR"
    Quando eu vincular o aluno "1" ao profissional "Rafael Souza"
    Então a operação da equipe é rejeitada com a mensagem "Somente profissionais classificados como personal trainer podem ser vinculados a alunos"

  Cenário: RN7 - Não pode haver dois vínculos ativos com o mesmo aluno
    Dado o profissional "Rafael Souza" com CPF "529.982.247-25", CREF "123456-G/PE" e classificação "PERSONAL_TRAINER"
    E que o aluno "1" está vinculado ao profissional "Rafael Souza"
    Quando eu vincular o aluno "1" ao profissional "Rafael Souza"
    Então a operação da equipe é rejeitada com a mensagem "Já existe um vínculo ativo entre este personal trainer e o aluno"

  Cenário: RN7 - Vínculo encerrado permite novo vínculo
    Dado o profissional "Rafael Souza" com CPF "529.982.247-25", CREF "123456-G/PE" e classificação "PERSONAL_TRAINER"
    E que o aluno "1" está vinculado ao profissional "Rafael Souza"
    Quando eu encerrar o vínculo do aluno "1" com o profissional "Rafael Souza"
    E eu vincular o aluno "1" ao profissional "Rafael Souza"
    Então o aluno "1" tem 1 vínculo "ATIVO" com o profissional "Rafael Souza"
    E o aluno "1" tem 1 vínculo "ENCERRADO" com o profissional "Rafael Souza"

  Cenário: RN7 - Remover a classificação de personal exige encerrar os vínculos
    Dado o profissional "Rafael Souza" com CPF "529.982.247-25", CREF "123456-G/PE" e classificação "PERSONAL_TRAINER"
    E que o aluno "1" está vinculado ao profissional "Rafael Souza"
    Quando eu alterar a classificação do profissional "Rafael Souza" para "PROFESSOR"
    Então a operação da equipe é rejeitada com a mensagem "Encerre os vínculos ativos com alunos antes de remover a classificação de personal trainer"

  Cenário: RN8 - Profissional com vínculos ativos não pode ser inativado
    Dado o profissional "Rafael Souza" com CPF "529.982.247-25", CREF "123456-G/PE" e classificação "PERSONAL_TRAINER"
    E que o aluno "1" está vinculado ao profissional "Rafael Souza"
    Quando eu inativar o profissional "Rafael Souza"
    Então a operação da equipe é rejeitada com a mensagem "Encerre os vínculos ativos com alunos antes de inativar o profissional"

  Cenário: RN8 - Inativação após encerrar os vínculos preserva o histórico
    Dado o profissional "Rafael Souza" com CPF "529.982.247-25", CREF "123456-G/PE" e classificação "PERSONAL_TRAINER"
    E que o aluno "1" está vinculado ao profissional "Rafael Souza"
    Quando eu encerrar o vínculo do aluno "1" com o profissional "Rafael Souza"
    E eu inativar o profissional "Rafael Souza"
    Então o profissional "Rafael Souza" fica "INATIVO" com CPF "52998224725" e CREF "123456-G/PE"
    E o aluno "1" tem 1 vínculo "ENCERRADO" com o profissional "Rafael Souza"

  Cenário: RN8 - Profissional inativo pode ser reativado
    Dado o profissional "Rafael Souza" com CPF "529.982.247-25", CREF "123456-G/PE" e classificação "PROFESSOR"
    E que o profissional "Rafael Souza" foi inativado
    Quando eu reativar o profissional "Rafael Souza"
    Então o profissional "Rafael Souza" fica "ATIVO" com CPF "52998224725" e CREF "123456-G/PE"

  Cenário: Somente o administrador gerencia profissionais
    Dado que estou autenticado como personal na equipe
    Quando eu cadastrar o profissional "Rafael Souza" com CPF "529.982.247-25", CREF "123456-G/PE" e classificação "PROFESSOR"
    Então a operação da equipe é rejeitada com a mensagem "Ação não autorizada"

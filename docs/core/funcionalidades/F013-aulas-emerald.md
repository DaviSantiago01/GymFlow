# F013 - Agendamento e gestão de aulas

Gestores e instrutores gerenciam turmas; alunos consultam a grade, inscrevem-se e acompanham o histórico.

## Regras de negócio

### RN001 - Cadastro da turma

Título, modalidade, data, horário e professor ativo são obrigatórios. O limite de vagas deve ser maior que zero e não pode ficar abaixo da ocupação atual.

### RN002 - Choque de horário

Um mesmo professor não pode ministrar aulas com horários sobrepostos no mesmo dia.

### RN003 - Inscrição e lotação

O aluno não pode manter inscrição duplicada na mesma turma. Turmas canceladas ou lotadas não aceitam novas inscrições; cancelar uma inscrição reabre a vaga.

### RN004 - Cancelamento pelo aluno

O aluno pode cancelar até 30 minutos antes do início. A partir desse limite, a inscrição permanece no histórico e não pode ser cancelada.

### RN005 - Cancelamento pela academia

Ao cancelar uma turma, todas as inscrições ativas passam a canceladas pela academia e cada aluno inscrito recebe uma notificação persistida.

### RN006 - Histórico

Inscrições canceladas e concluídas permanecem associadas à turma e ao cadastro real do aluno.

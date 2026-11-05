# Estado da funcionalidade de agenda

Concluída e validada em 05/11/2026. Este documento substitui a lista de pendências
registrada no commit inicial.

- Escolha de data e início em blocos de uma hora.
- Duração de 1, 2 ou 3 horas consecutivas conforme disponibilidade.
- Funcionamento diário das 06h às 22h, no horário de Brasília.
- Validação de conflitos e datas no servidor, inclusive sob concorrência.
- Reserva agendada, tempo até começar e contador durante o uso.
- Cancelamento, encerramento, vencimento e histórico preservados.
- Migração V2 validada com registros antigos e aplicada ao banco local com cópia prévia.
- 58 casos Java, 6 testes Angular e 6 testes de navegador aprovados.

Consulte [resultados e limites](validacao/RESULTADOS.md) e [contratos da API](core/api-armarios.md).

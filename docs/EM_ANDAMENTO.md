# Estado do projeto neste commit

Este commit inicial registra o trabalho em andamento em 05/11/2026.

Cadastro, login, tela inicial minimalista e menu lateral foram implementados e
validados antes da mudança da agenda. Aulas e grupos de treino são ilustrativos.

A reserva por data e horário ainda não está concluída. O backend contém alterações
para períodos de uma a três horas, todos os dias das 06h às 22h no fuso
America/Sao_Paulo, além da migração V2. Essas alterações ainda precisam ser validadas.

Pendências:

- Integrar seleção de data, horário e duração no frontend ao novo corpo do POST.
- Adaptar os testes Java e de navegador que ainda usam a assinatura anterior.
- Atualizar a exibição de reserva agendada, início, duração e tempo restante.
- Validar migração, conflitos de horários, compilação e fluxo completo.
- Compilar e iniciar a versão atualizada para teste pelo usuário.

Os resultados anteriores em `validacao/RESULTADOS.md` referem-se à versão anterior
à alteração de agendamento, não à conclusão desta mudança. O JAR local já existente
também pode corresponder à versão anterior. Banco local e artefatos gerados não
integram o repositório.

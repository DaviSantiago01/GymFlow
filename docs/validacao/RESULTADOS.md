# Validação da implementação

Atualizado em 5 de novembro de 2026. Windows, Java 25.0.5, Maven 3.9.16,
Node 24.16.0, H2 2.4.240, Spring Boot 4.1.1 e Angular 22.

## Testes e compilação

| Verificação | Resultado |
|---|---|
| Cucumber — domínio e agenda | 23 cenários passaram |
| Serviço de domínio — ordem dos bloqueios | 1 teste passou |
| API, agenda, concorrência, segurança e transações | 26 testes passaram |
| Agendador real sem chamadas HTTP | 1 teste passou |
| Migração V1 para V2 com reservas existentes | 1 teste passou |
| Cadastro, hash, login, concorrência e rollback | 6 testes passaram |
| Angular / Vitest | 6 testes passaram |
| Playwright / Microsoft Edge | 6 testes passaram |
| Maven | Testes Java aprovados; JAR final empacotado |
| Angular `build` com `strictTemplates` | Aprovado |
| Context Mapper `docs/core/projeto.cml` | Validado sem erros |

Total Java: 58 casos, incluindo exemplos de cenários parametrizados do Cucumber.
O empacotamento final exigiu parar o backend, pois o Windows bloqueava a troca do
JAR em uso. Depois de passar a suíte Java, o pacote foi gerado com `-DskipTests`.
Os testes de navegador foram repetidos com o JAR final em execução.

## Agenda e concorrência

Verificados: durações de 1, 2 e 3 horas; limites 06h e 22h; data passada; horário
passado no mesmo dia; campos ausentes; números fracionados; preservação da identidade
da sessão; ausência de informações privadas na agenda; cancelamento antes do início;
encerramento somente após começar; vencimento no limite e após indisponibilidade.

Disputas pelo mesmo armário e pelo mesmo aluno são repetidas cinco vezes. Também
há concorrência entre intervalos parcialmente sobrepostos (somente um sucesso) e
intervalos adjacentes (dois sucessos). A unicidade de aluno continua protegida no
banco. A exclusividade de armário por intervalo é validada sob bloqueio transacional.

No navegador, duas sessões consultam o mesmo horário: uma confirma e a outra recebe
conflito, atualiza a agenda e consegue reservar o intervalo seguinte. A troca rápida
de datas descarta uma resposta atrasada. O teste de fechamento confirma uma hora às
21h e bloqueia durações maiores. Os testes usam contas próprias, sem cancelar reservas
de usuários reais. Cadastro e login com senha acentuada continuam funcionando.

## Migração e execução local

O teste de migração cria um banco na V1, insere uma reserva ativa com início fora da
hora cheia e uma cancelada, aplica V2 e verifica os valores preservados. Em registros
antigos, início recebe confirmação. Um segundo aluno passa a poder reservar outro
intervalo do mesmo armário. A migração usa pool de conexões, como a aplicação.

O banco local recebeu a migração V2 com sucesso. Antes dela foi salva uma cópia em
`../.tools/treino-conecta-antes-agenda-20261105.mv.db`, fora do Git. Backend e frontend
foram iniciados nas portas 8080 e 4200. A evidência anterior `persistencia.json` e
as capturas 01–09 pertencem à versão anterior da interface.

Capturas atuais inspecionadas:

- [Data, horários e duração](10-agenda.png)
- [Reserva agendada](11-reserva-agendada.png)
- [Agenda no celular](12-agenda-celular.png)

Relatórios: `dominio-core/target/surefire-reports`, `ui-backend/target/surefire-reports`
e `ui-frontend/src/main/angular/playwright-report`. Artefatos de teste, banco e
dependências são ignorados pelo Git.

## Limites

O build Angular mantém avisos de tamanho: bundle inicial de aproximadamente 595 kB
(aviso em 500 kB) e CSS do componente de 7,08 kB (aviso em 4 kB). Ambos abaixo dos
limites de erro originais; os limites não foram ampliados.

Não foram validados outros SGBDs, implantação pública ou integração com fechaduras.
O limite de uma reserva pendente por aluno foi preservado. Aulas e grupos de treino
continuam ilustrativos, como solicitado. Os ZIPs anteriores são versões históricas;
o código atualizado está na pasta do projeto e na branch de desenvolvimento.

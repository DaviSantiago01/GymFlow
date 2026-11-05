# Validação da implementação

Data: 4 de novembro de 2026. Ambiente: Windows 11, Java 25.0.5, Maven 3.9.16,
Node 24.16.0, H2 fornecido pelo D5, Spring Boot 4.1.1 e Angular 22.

## Testes automatizados executados

| Verificação | Resultado |
|---|---|
| Cucumber — regras RN01 a RN04 | 11 cenários passaram |
| Serviço de domínio — ordem dos bloqueios e confirmação | 1 teste passou |
| API, segurança, transações e banco H2 real | 19 testes passaram |
| Agendador executado pelo Spring sem chamadas HTTP | 1 teste passou |
| Cadastro público, hash de senha, login, validação, concorrência e rollback | 6 testes passaram |
| Angular / Vitest | 6 testes passaram |
| Playwright / Microsoft Edge | 4 testes passaram, incluindo cadastro e login com senha acentuada |
| Maven `package` | Compilou e gerou o JAR executável |
| Angular `build` com `strictTemplates` | Compilou o bundle de produção |
| Context Mapper | Os três arquivos CML foram validados sem erros de sintaxe |

Os testes de concorrência repetem cinco vezes cada disputa (mesmo armário e mesmo
aluno), sincronizando duas threads e requisições HTTP independentes. Em todas,
um comando retornou 200, o outro 409 e só uma reserva foi gravada.
Há ainda tentativas de inserir duplicidades diretamente no banco, rejeitadas pelas
restrições únicas, e um teste que comprova rollback após uma gravação intermediária.

## Cobertura dos casos solicitados

| Caso | Evidência |
|---|---|
| 1. Reserva disponível | `reservaDisponivelPersisteComPrazoExato` e navegador |
| 2. Armário ocupado | `armarioOcupadoRejeitaOutraReserva` |
| 3. Solicitações simultâneas | `concorrenciaNoMesmoArmarioConfirmaSomenteUma` |
| 4. Dois armários pelo mesmo aluno | Teste sequencial e `concorrenciaDoMesmoAlunoConfirmaSomenteUma` |
| 5. Expiração em três horas | Cucumber no limite exato e teste real do agendador |
| 6. Disponibilidade após expiração | `consultasRespeitamVencimentoAntesDoAgendador` |
| 7. Cancelamento pelo responsável | `cancelarLiberaArmarioEPreservaHistorico` e navegador |
| 8. Cancelamento por outro aluno | `outroAlunoNaoConsultaNemManipulaReserva` e duas sessões no Edge |
| 9. Encerramento antecipado | `encerrarLiberaArmarioEPreservaHistorico` e navegador |
| 10. Histórico preservado | Histórico filtrado por aluno, finalizações e reinício do backend |
| 11. Reserva já finalizada | `finalizadaNaoPodeSerCanceladaOuEncerrada` e limite exato |
| 12. Horários e transações | UTC/fuso -03:00, relógio monotônico, rollback e concorrência |

## Interface e inicialização

Backend e frontend foram iniciados em `localhost:8080` e `127.0.0.1:4200`.
O backend foi encerrado e iniciado novamente com o mesmo arquivo H2: os três
registros de histórico existentes mantiveram todos os atributos e a mesma contagem.
A comparação foi feita antes de uma segunda execução dos testes de navegador;
veja a [evidência de persistência](persistencia.json).
O navegador percorreu login, catálogo, filtro, detalhes, confirmação, reserva ativa,
cancelamento, histórico e encerramento antecipado, usando somente dados da API.
O teste com duas sessões verificou ocupação e rejeição HTTP 403 ao tentar cancelar
a reserva de outro aluno. O teste móvel verificou ausência de rolagem horizontal
em uma janela de 390 × 844 pixels. Nenhum erro de JavaScript foi detectado no fluxo completo.

Capturas inspecionadas visualmente:

- [Lista](01-lista.png)
- [Detalhes](02-detalhe.png)
- [Confirmação](03-confirmacao.png)
- [Reserva ativa](04-reserva.png)
- [Histórico](05-historico.png)
- [Celular](06-celular.png)
- [Cadastro de conta](07-cadastro.png)
- [Início com menu lateral](08-inicio.png)
- [Menu aberto no celular](09-menu-celular.png)

Atualização da navegação: build Angular, seis testes unitários e quatro testes
Playwright passaram. Os fluxos verificam a entrada em `/inicio`, opções ilustrativas
desabilitadas, navegação para armários, fechamento do menu no celular e cadastro/login.
Os testes de reserva escolhem armários disponíveis, respeitando a ocupação existente.

Os relatórios gerados pelos testes ficam em `dominio-core/target/surefire-reports`,
`ui-backend/target/surefire-reports` e `ui-frontend/src/main/angular/playwright-report`.
O ZIP de entrega contém código, documentação e capturas; dependências instaladas,
banco de desenvolvimento e diretórios de build não fazem parte dele.

## Avisos e limites da validação

O build Angular passa com dois avisos de tamanho: bundle inicial de aproximadamente
594 kB (limite de aviso 500 kB, limite de erro 1 MB) e estilo do componente de
6,11 kB (limite de aviso 4 kB, limite de erro 8 kB). Os limites originais não foram
aumentados para ocultar os avisos.

O validador Context Mapper imprime avisos de API Java obsoleta e uma interrupção
da thread de limpeza ao terminar. Os três resultados contêm explicitamente
`has been validated without errors`. O escape `^core` corrige o uso de palavra
reservada que já existia no modelo gerado.

Não foram validados outros SGBDs, implantação pública, balanceamento entre múltiplas
instâncias ou integração com fechaduras físicas. O ambiente de produção depende
do provisionamento administrativo e de HTTPS, conforme o README.

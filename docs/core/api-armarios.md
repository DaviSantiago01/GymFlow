# Contratos das telas de armários

Base: `/backend`. As consultas exigem sessão; os POSTs exigem também `X-XSRF-TOKEN`.
O fuso de funcionamento é `America/Sao_Paulo`. Instantes são transportados em UTC.

## Catálogo e detalhe

`GET /backend/core/armario/pesquisa?tamanhoPagina=1000` retorna o envelope D5
`{consulta, resultado: {total, projecoes}}`. Cada projeção possui identificador,
localizacao, descricao e disponivel. **Disponível significa livre agora**, não em
todas as datas. Um armário em uso agora pode ser reservado para outro período.

`GET /backend/core/armario/A-01/visualizacao` retorna
`{dados: {identificador, localizacao, descricao, disponivel}, horarioServidor}`.

## Consultar agenda

`GET /backend/core/armario/A-01/horarios?data=2026-11-06`

Sem `data`, utiliza o dia corrente da academia. Datas passadas ou inválidas são
rejeitadas. Exemplo abreviado (a resposta real contém 16 blocos, das 06h às 22h):

```json
{
  "dados": {
    "data": "2026-11-06",
    "hoje": "2026-11-05",
    "fuso": "America/Sao_Paulo",
    "horarios": [
      {"hora": 6, "inicio": "2026-11-06T09:00:00Z", "termino": "2026-11-06T10:00:00Z", "disponivel": true, "duracoesDisponiveis": [1,2,3]},
      {"hora": 21, "inicio": "2026-11-07T00:00:00Z", "termino": "2026-11-07T01:00:00Z", "disponivel": true, "duracoesDisponiveis": [1]}
    ]
  },
  "horarioServidor": "2026-11-05T13:00:00Z"
}
```

`duracoesDisponiveis` é vazio quando o bloco passou ou está ocupado. Um início livre
pode permitir só uma hora se houver reserva no bloco seguinte. Não são expostos
nomes nem identificadores de reservas de outros alunos.

## Confirmar agendamento

`POST /backend/core/armario/A-01/reservar`, `Content-Type: application/json`:

```json
{"data":"2026-11-06","hora":6,"duracaoHoras":2}
```

Data é a do calendário da academia, hora é inteira de 6 a 21, duração é 1, 2 ou 3.
A reserva deve terminar até 22h. Nenhum início no passado é aceito. O backend
revalida o intervalo sob bloqueio, mesmo que a consulta de disponibilidade seja antiga.
Confirmação, término e responsável são calculados pelo servidor.

```json
{
  "dados": {
    "identificador":"1", "alunoNome":"Aluno", "armario":"A-01",
    "localizacao":"Vestiário A", "confirmacao":"2026-11-05T13:00:00Z",
    "inicio":"2026-11-06T09:00:00Z", "termino":"2026-11-06T11:00:00Z",
    "encerramento":null, "situacao":"ATIVA"
  },
  "horarioServidor":"2026-11-05T13:00:00Z"
}
```

`ATIVA` identifica uma reserva pendente, tanto antes quanto depois do início.
A interface apresenta **Agendada** antes de `inicio` e **Em uso** durante o período.

## Consultar e finalizar

- `GET /backend/core/reserva-armario/ativa`: a reserva em uso ou agendada do aluno;
  sem reserva retorna `{dados: null, horarioServidor}`.
- `GET /backend/core/reserva-armario/{id}/confirmacao` e `.../visualizacao`:
  o mesmo envelope, somente para o responsável.
- `GET /backend/core/reserva-armario/pesquisa?indicePagina=0&tamanhoPagina=10`:
  histórico finalizado, no envelope D5, ordenado do mais recente ao mais antigo.
- `POST /backend/core/reserva-armario/{id}/cancelar`: sem corpo; permite cancelar
  antes ou durante o período, mas nunca após o término.
- `POST /backend/core/reserva-armario/{id}/encerrar`: sem corpo; somente durante o uso.

Finalizações mantêm confirmação, início e término, preenchem encerramento e alteram
a situação para CANCELADA ou ENCERRADA. O agendador usa VENCIDA no fim do período.

## Erros

| Status | Significado |
|---|---|
| 400 | Corpo incompleto ou campos fora das restrições do DTO |
| 401 | Sessão ou credencial inválida |
| 403 | Sem permissão, vínculo de aluno ou token CSRF válido |
| 404 | Registro inexistente ou reserva que não pertence ao aluno |
| 409 | Sobreposição, limite de reserva, finalização indevida ou uso ainda não iniciado |
| 422 | Data, identificador ou combinação de período inválida |

Exemplo: `{"mensagem":"Este horário não está mais disponível. Escolha outro período"}`.
Os exemplos documentam o contrato; não são utilizados como dados simulados.

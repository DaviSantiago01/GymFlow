# Contratos das telas de armários

Base: `/backend`. As rotas Angular têm o mesmo caminho do GET que resolve seus dados.
O histórico usa a rota `core/reserva-armario/pesquisa`, com o rótulo visual Histórico.

## Pesquisar

`GET /backend/core/armario/pesquisa?tamanhoPagina=1000`

```json
{"consulta":{"indicePagina":0,"tamanhoPagina":1000},"resultado":{"total":1,"projecoes":[{"identificador":"A-01","localizacao":"Vestiário A","descricao":"Padrão","disponivel":true}]}}
```

Os atributos de consulta seguem o D5 (`indicePagina`, `tamanhoPagina`, `ordenacao`,
`descendente`). O histórico usa o mesmo envelope e ordena do mais recente ao mais antigo.

## Reservar e consultar a reserva

`POST /backend/core/armario/A-01/reservar`, sem corpo. Exemplo de resposta:

```json
{
  "dados": {
    "identificador": "1",
    "alunoNome": "Mariana Costa",
    "armario": "A-01",
    "localizacao": "Ala Vestiário A · Piso térreo",
    "confirmacao": "2026-11-04T12:00:00Z",
    "termino": "2026-11-04T15:00:00Z",
    "encerramento": null,
    "situacao": "ATIVA"
  },
  "horarioServidor": "2026-11-04T12:00:00Z"
}
```

`GET /backend/core/reserva-armario/ativa` usa esse envelope. Sem reserva ativa:

```json
{"dados":null,"horarioServidor":"2026-11-04T15:00:00Z"}
```

`GET /backend/core/reserva-armario/1/confirmacao` e `.../visualizacao` retornam a
reserva somente para seu responsável, inclusive após finalizar.

## Detalhes do armário

`GET /backend/core/armario/A-01/visualizacao`

```json
{"dados":{"identificador":"A-01","localizacao":"Vestiário A","descricao":"Padrão","disponivel":true},"horarioServidor":"2026-11-04T12:00:00Z"}
```

## Finalizar

`POST /backend/core/reserva-armario/1/cancelar` ou `.../encerrar`, sem corpo.
Resposta temporal com a mesma reserva, `encerramento` preenchido e situação
`CANCELADA` ou `ENCERRADA`. Vencimentos usam `VENCIDA`.

## Autenticação e erros

O login é `GET /backend/autenticacao` com Basic; o D5 mantém o contexto de segurança
na sessão HttpOnly e fornece as permissões de menu `armario` e `reservaArmario`.
As solicitações seguintes usam cookies. Comandos exigem CSRF.

| Status | Significado |
|---|---|
| 200 | Consulta/comando concluído |
| 401 | Sessão ou credencial inválida |
| 403 | Sem permissão, vínculo de aluno ou token CSRF válido |
| 404 | Registro não encontrado nas consultas permitidas ao aluno |
| 409 | Ocupação, cota excedida ou reserva finalizada |
| 422 | Argumento/identificador inválido |

Exemplo: `{"mensagem":"Você já possui uma reserva ativa"}`.
Os exemplos ilustram o contrato; a aplicação nunca usa esses objetos como mocks.

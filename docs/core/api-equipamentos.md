# API de gestão de equipamentos

As rotas abaixo usam o prefixo `/backend/core`, a sessão autenticada e a permissão `ADMINISTRADOR`. Envie o token CSRF nos comandos POST, conforme o cliente Angular do projeto.

| Recurso | Pesquisa e leitura | Comandos POST |
| --- | --- | --- |
| `equipamento` | `pesquisa` por `termo`, `tipo`, `area` e `situacao`. `{id}/visualizacao` e `{id}/operacoes`. | `cadastrar`, `{id}/editar`, `{id}/inativar` e `{id}/excluir`. |
| `registro-equipamento` | `pesquisa` por `equipamento` e `{id}/visualizacao`. | `{equipamento}/movimentar` e `{equipamento}/inspecionar`. |
| `problema-equipamento` | `pesquisa` por `equipamento`, `situacao` e `gravidade`. `{id}/visualizacao`. | `registrar`, `{id}/editar`, `{id}/resolver` e `{id}/cancelar`. |
| `ordem-manutencao` | `pesquisa` por `problema` e `situacao`. `{id}/visualizacao`. | `abrir`, `{id}/editar`, `{id}/iniciar`, `{id}/concluir` e `{id}/cancelar`. |

As pesquisas usam `indicePagina` a partir de zero e `tamanhoPagina`. Ordens, problemas e registros podem ser filtrados pelos identificadores exibidos nos detalhes e nas pesquisas relacionadas.

## Exemplo de inspeção

Uma inspeção aprovada na data corrente pode liberar o equipamento quando já foram resolvidos os riscos em aberto e encerradas as ordens ativas.

```http
POST /backend/core/registro-equipamento/12/inspecionar
Content-Type: application/json

{
  "data": "2026-11-17",
  "resultado": "APROVADO",
  "area": "MUSCULACAO",
  "situacao": "DISPONIVEL",
  "observacao": "Dispositivos de segurança conferidos após a manutenção."
}
```

A consulta do equipamento devolve a última e a próxima inspeção. A consulta de registros mantém a situação e a área anterior e atual, o resultado, a data e o responsável da sessão.

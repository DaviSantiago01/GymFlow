# Histórico de treinos e reorganização de Armários

Entrega validada em 05/11/2026 (horário de Brasília).

## Comportamento

Avaliações abre diretamente em Histórico de treinos. Cada registro mostra
descrição, data, horário, professor e Avaliar treino ou Ver avaliação. A API e a
interface não permitem avaliar diretamente pelo professor. A regra, confirmada
pelo usuário, é uma avaliação por treino, inclusive em treinos diferentes com o
mesmo professor. Após excluir, não é permitida recriação no mesmo treino.

Armários é o único acesso dessa área no menu e na página inicial. Minha reserva
e Histórico de reservas aparecem apenas na navegação interna de Armários, também
no celular. URLs e funcionalidade de reservas foram preservadas.

## Modelo e segurança

Novo agregado Treino no mesmo core D5, com Aluno, Professor, descrição, início,
término, conclusão e indicador de demonstração. A migração V4 troca a unicidade
aluno-professor por treino e usa FK composta para garantir a correspondência entre
treino, autor e professor. VAT_TREINO_AVALIADO conserva apenas o vínculo após
excluir o conteúdo. Avaliações antigas e reservas são preservadas; não foram
inventados treinos retroativos para registros legados.

Para avaliar, o backend verifica sessão, pertencimento do treino, conclusão e
término passado. Bloqueia aluno/treino na mesma transação e verifica duplicidade.
Nenhum ID de autor, professor ou treino no corpo troca o registro selecionado.
As validações das cinco notas, comentário, autoria e médias continuam vigentes.

## Dados de demonstração e reais

As contas u/u e v/v recebem três treinos de demonstração no desenvolvimento.
Contas novas podem usar Carregar treinos de demonstração. A ação é idempotente,
não apaga registros reais e só usa professores de demonstração. Não cria
avaliações fictícias. Fora de desenvolvimento, ou com produção ativo, é negada.

Treinos reais usam a mesma persistência e aparecem automaticamente após cadastro
e conclusão pela academia. Não foi incluída uma tela administrativa de frequência:
o procedimento de integração/cadastro está em [treinos-reais.md](../core/treinos-reais.md).
Os alunos não podem falsificar frequência criando treinos reais por conta própria.

## Validação

- Maven package: BUILD SUCCESS, 121 testes Java (31 Cucumber, 1 unitário e 89 de integração/migração).
- Novos casos: dois treinos do mesmo professor, treino alheio, futuro ou não concluído,
  histórico paginado, data/hora/professor, tentativa de trocar treino no corpo,
  avaliação direta removida, demonstração indisponível em ambiente normal,
  carga idempotente com treino real existente e preservação da V4 na V5.
- Angular/Vitest: 9 testes aprovados.
- Playwright/Edge: 8 fluxos aprovados, incluindo desktop/celular, dois treinos do
  mesmo professor, edição, exclusão, nota zero e comentário vazio, cadastro e armários.
- Build Angular aprovado. Permanecem avisos de tamanho: bundle inicial ~608 kB,
  CSS de avaliações ~5,46 kB e armários ~7,11 kB; limites de erro não foram ampliados.

Evidências:

- [Histórico com data, hora e professor](19-historico-treinos.png)
- [Formulário vinculado ao treino](20-avaliar-treino.png)
- [Histórico no celular](21-historico-treinos-mobile.png)

## Execução e documentação

Backend em 8080, frontend em http://127.0.0.1:4200. Entre novamente após o reinício
e abra Avaliações. O README contém os comandos Maven/Java/npm. A migração é
automática na inicialização. Foi salva cópia do banco anterior à V4 em
`../.tools/treino-conecta-antes-treinos-20261105.mv.db`, fora do Git.

Contrato atualizado em [api-avaliacoes.md](../core/api-avaliacoes.md), regras em
[F003-avaliacoes.md](../core/funcionalidades/F003-avaliacoes.md) e modelos em
`docs/core/projeto.cml` e `docs/core/projeto.dbml`. A decisão anterior da RN03 e
as capturas 13–18 são históricas; o esclarecimento do usuário e esta entrega
substituem a regra por aluno-professor e o antigo acesso direto ao formulário.

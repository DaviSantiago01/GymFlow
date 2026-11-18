# Gestão de equipamentos

A gestão de equipamentos é restrita ao Gestor. Seus agregados representam o equipamento físico, seu histórico de situação e inspeções, os problemas e as ordens de manutenção. O técnico indicado na ordem é um dado do atendimento. O cadastro de profissionais da academia continua no módulo responsável por professores e personal trainers.

| Funcionalidade | Regra central | Cenários de verificação |
| --- | --- | --- |
| [F009 — Cadastro e gestão](../core/funcionalidades/F009.md) | Patrimônio e série identificam o equipamento. A exclusão exige ausência de histórico. | [Cadastro, alteração, exclusão e inativação](../../dominio-core/src/test/resources/features/equipamento.feature) |
| [F010 — Situação, localização e inspeções](../core/funcionalidades/F010.md) | A inspeção registra resultado, atualiza o prazo e pode liberar o equipamento nas condições previstas. | [Prazo, histórico e liberação](../../dominio-core/src/test/resources/features/registro-equipamento.feature) |
| [F011 — Gestão de problemas](../core/funcionalidades/F011.md) | Um problema crítico ou com risco interdita o equipamento. O encerramento registra o desfecho. | [Interdição, resolução e cancelamento](../../dominio-core/src/test/resources/features/problema-equipamento.feature) |
| [F012 — Ordens de manutenção](../core/funcionalidades/F012.md) | A ordem parte de um problema aberto. A conclusão resolve o problema e mantém a liberação sujeita a inspeção. | [Abertura, início, conclusão e cancelamento](../../dominio-core/src/test/resources/features/ordem-manutencao.feature) |

O [modelo CML](../core/projeto.cml) descreve os agregados e os serviços. O [modelo DBML](../core/projeto.dbml) e a [migração Flyway](../../infraestrutura/src/main/resources/db/migration/V8__gestao_equipamentos.sql) descrevem a persistência. As operações passam pelo domínio Java, pela infraestrutura JPA e pela API Spring, com telas Angular. Os testes de [integração da API](../../ui-backend/src/test/java/br/edu/cesar/treinoconecta/treinoconectacore/ui/backend/core/equipamento/EquipamentosIntegracaoTest.java) e de [navegador](../../ui-frontend/src/main/angular/e2e/equipamentos.spec.ts) cobrem etapas desse fluxo.

## Padrões identificados no projeto

- **Template Method** em `AgregadoServicoAbstrato.excluir`, do D5. O serviço de equipamentos especializa a etapa de validação para proteger o histórico.
- **Proxy** nas referências JPA obtidas por `getReferenceById`. A ordem associa o problema pelo identificador sem carregar seus dados naquele ponto.
- **Observer** na assinatura dos eventos do Angular Router. O componente principal reage à navegação e fecha o menu móvel.
- **Strategy** no uso de `Comparator` em `HistoricoDeAvaliacao`. A comparação por data é fornecida à operação de ordenação.

# F015 - Geração de relatórios e análise de desempenho da academia

O gestor gera relatórios de um período sobre matrículas, frequência, ocupação, receita, desempenho dos personais, risco de evasão, adesão aos treinos e metas da academia. Cada relatório gerado fica guardado com os valores calculados naquele momento, pode ser comparado com o período anterior e exportado em PDF ou planilha.

Entidades envolvidas: relatório, meta da academia, aluno, matrícula, trancamento, mensalidade, pagamento, registro de acesso (F006), aula coletiva, uso do estacionamento, plano de treino e personal.

Perfis: o gestor (ADMINISTRADOR) acessa tudo; o personal (PERSONAL) gera e consulta apenas relatórios de desempenho e adesão dos próprios alunos; o aluno não acessa relatórios.

## Regras de negócio

| Regra | Descrição |
| --- | --- |
| RN001 | Autorização por perfil: gestor gera todos os tipos; personal só os de desempenho e adesão, restritos aos próprios alunos. |
| RN002 | O período precisa de início e fim, o início não pode ser posterior ao fim, o fim não pode passar de hoje e a duração máxima é de 366 dias. |
| RN003 | Cada filtro (plano, modalidade, personal, aula) só é aceito nos tipos de relatório em que faz sentido. |
| RN004 | Agrupamento por dia, semana ou mês; o diário vale para períodos de até 90 dias. |
| RN005 | Nova matrícula é a de quem não tinha vínculo anterior; troca de plano não conta como nova, e o retorno de quem tinha cancelado conta como reativação. |
| RN006 | A frequência só considera os dias em que o aluno estava ativo e não trancado; várias entradas no mesmo dia contam como uma presença. As entradas vêm do controle de acesso (F006). |
| RN007 | Evasão: cancelamentos e encerramentos sem renovação dentro do período, sobre os ativos no início. Trancamento não é evasão. |
| RN008 | Renovação: matrícula que continua até 10 dias após o fim do ciclo contratado. |
| RN009 | Receita: pagamentos confirmados no período menos os estornos do período. |
| RN010 | Inadimplência: mensalidades vencidas no período e sem pagamento após 5 dias de carência. |
| RN011 | Comparação com o período anterior de mesma duração, com variação absoluta e percentual; sem base anterior a variação não é calculada. |
| RN012 | Ocupação média da sala por faixa de 2 horas (pico acima de 85%, baixa abaixo de 30%), das aulas coletivas e do estacionamento, só nos horários de funcionamento. |
| RN013 | Desempenho dos personais: alunos com plano ativo, adesão (dias com treino ÷ dias previstos) e metas atingidas. |
| RN014 | Alunos em risco: ativos sem entrada há mais de 14 dias ou com queda de frequência acima de 50%. |
| RN015 | Não pode haver duas metas ativas para o mesmo indicador em períodos sobrepostos. |
| RN016 | Cumprimento da meta conforme o sentido do indicador (para evasão e inadimplência, menor é melhor). |
| RN017 | Os relatórios ficam no histórico com tipo, período, filtros, autor e data de geração. |
| RN018 | Os valores não mudam após a geração. Quando há registros incluídos ou corrigidos depois, o sistema avisa e permite gerar uma nova versão; a anterior é mantida. |
| RN019 | Na exportação, CPF e telefone saem mascarados, e o personal não recebe dados nominais de alunos de outros personais. |
| RN020 | O arquivo exportado usa os valores guardados, sem recálculo, e informa período, filtros, versão, autor e data de geração. |

## Telas

| Tela | Endereço | Perfil |
| --- | --- | --- |
| Painel da academia | `/core/relatorio/painel` | gestor |
| Novo relatório (tipo, período, filtros e agrupamento) | `/core/relatorio/novo` | gestor, personal |
| Histórico de relatórios | `/core/relatorio/historico` | gestor, personal |
| Relatório gerado (indicadores, gráficos e tabelas por tipo) | `/core/relatorio/:id/visualizacao` | gestor, personal |
| Comparação com o período anterior | `/core/relatorio/:id/comparacao` | gestor, personal |
| Exportação em PDF ou planilha | diálogo na tela do relatório | gestor, personal |
| Metas da academia (cadastro, encerramento, substituição e acompanhamento) | `/core/meta-academia` | gestor |

## API

Prefixo `/backend/core/relatorio`: `GET inclusao`, `POST verificar`, `POST gerar`, `GET {id}/visualizacao`, `POST {id}/nova-versao`, `GET painel`, `GET {id}/exportacao`, `GET {id}/exportar?formato=PDF|PLANILHA`.

Prefixo `/backend/core/meta-academia`: `GET inclusao`, `GET acompanhamento`, `POST cadastrar`, `POST {id}/encerrar`, `POST {id}/substituir`.

## Dados de exemplo

Com o perfil `demonstracao` (além de `desenvolvimento`), o sistema carrega uma única vez 48 alunos fictícios (`demo01` a `demo48`, sem senha) com matrículas, trancamentos, mensalidades, entradas dos últimos 120 dias, uso do estacionamento e execuções de treino, para que os relatórios tenham conteúdo. Sem esse perfil, os exemplos das demais funcionalidades ficam como estavam.

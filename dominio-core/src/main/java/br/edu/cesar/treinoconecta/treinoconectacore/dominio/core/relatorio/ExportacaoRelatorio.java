package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.DiaSemana;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.ConteudoRelatorio.ClassificacaoOcupacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.ConteudoRelatorio.MotivoRisco;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.ConteudoRelatorio.ValorIndicador;

/**
 * RN020: conteúdo do arquivo exportado, montado a partir dos valores guardados no histórico, sem recálculo. Leva o
 * período, os filtros, a data de geração e o responsável. Pela RN019, CPF e telefone saem mascarados e informações de
 * saúde não são exportadas.
 */
public record ExportacaoRelatorio(String titulo, List<String> cabecalho, List<Tabela> tabelas) {
    private static final Locale BRASIL = Locale.of("pt", "BR");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    public ExportacaoRelatorio {
        Objects.requireNonNull(titulo);
        cabecalho = List.copyOf(cabecalho);
        tabelas = List.copyOf(tabelas);
    }

    public record Tabela(String titulo, List<String> colunas, List<List<String>> linhas) {
        public Tabela {
            colunas = List.copyOf(colunas);
            linhas = linhas.stream().map(List::copyOf).toList();
        }
    }

    public static ExportacaoRelatorio de(Relatorio relatorio, String descricaoFiltros) {
        Objects.requireNonNull(relatorio);
        var conteudo = relatorio.getConteudo();
        var cabecalho = new ArrayList<String>();
        cabecalho.add("Período: " + data(relatorio.getPeriodo().inicio()) + " a " + data(relatorio.getPeriodo().fim())
                + " (" + relatorio.getPeriodo().dias() + " dias), agrupado por " + agrupamento(relatorio.getAgrupamento()));
        cabecalho.add("Filtros: " + descricaoFiltros);
        cabecalho.add("Gerado em " + relatorio.getGeracao().atZone(ParametrosAcademia.FUSO).format(DATA_HORA) + " por "
                + relatorio.getUsuarioNome() + " (" + perfil(relatorio) + "), versão " + relatorio.getVersao());
        if (relatorio.isComparar()) {
            var anterior = relatorio.getPeriodo().anterior();
            cabecalho.add("Comparação com " + data(anterior.inicio()) + " a " + data(anterior.fim()));
        }
        var tabelas = new ArrayList<Tabela>();
        if (!conteudo.indicadores().isEmpty()) {
            var linhas = new ArrayList<List<String>>();
            for (var indicador : conteudo.indicadores()) {
                linhas.add(linhaIndicador(indicador, relatorio.isComparar()));
            }
            tabelas.add(new Tabela("Indicadores", relatorio.isComparar()
                    ? List.of("Indicador", "Período atual", "Período anterior", "Variação absoluta", "Variação percentual", "Resultado")
                    : List.of("Indicador", "Valor"), linhas));
        }
        conteudo.movimento().ifPresent(movimento -> tabelas.add(new Tabela("Movimento de alunos",
                List.of("Início", "Novas matrículas", "Reativações", "Renovações", "Evasões", "Saldo de trancamentos", "Fim"),
                List.of(List.of(inteiro(movimento.inicio()), inteiro(movimento.novasMatriculas()),
                        inteiro(movimento.reativacoes()), inteiro(movimento.renovacoes()), inteiro(movimento.evasoes()),
                        inteiro(movimento.saldoTrancamentos()), inteiro(movimento.fim()))))));
        if (!conteudo.grupos().isEmpty()) {
            tabelas.add(new Tabela("Resultados por " + agrupamento(relatorio.getAgrupamento()),
                    List.of("Grupo", "Frequência por semana", "Presenças", "Novas matrículas", "Receita líquida"),
                    conteudo.grupos().stream().map(grupo -> List.of(grupo.rotulo(), decimal(grupo.frequencia()),
                            inteiro(grupo.presencas()), inteiro(grupo.novasMatriculas()), moeda(grupo.receita()))).toList()));
        }
        if (!conteudo.planos().isEmpty()) {
            tabelas.add(new Tabela("Resultados por plano contratado",
                    List.of("Plano", "Alunos ativos", "Novas matrículas", "Taxa de evasão", "Receita líquida", "Participação"),
                    conteudo.planos().stream().map(plano -> List.of(plano.nome(), inteiro(plano.alunosAtivos()),
                            inteiro(plano.novasMatriculas()), percentual(plano.taxaEvasao()), moeda(plano.receita()),
                            percentual(plano.participacao()))).toList()));
        }
        conteudo.ocupacao().ifPresent(ocupacao -> {
            var linhas = new ArrayList<List<String>>();
            for (var celula : ocupacao.sala()) {
                if (celula.classificacao() != ClassificacaoOcupacao.FECHADO) {
                    linhas.add(List.of(dia(celula.dia()), celula.inicio() + " às " + celula.inicio().plusHours(2),
                            decimal(celula.media()), percentual(celula.percentual()), classificacao(celula.classificacao())));
                }
            }
            tabelas.add(new Tabela("Ocupação da sala de musculação (capacidade " + ocupacao.capacidadeSala() + ")",
                    List.of("Dia", "Faixa", "Média de entradas", "Ocupação", "Classificação"), linhas));
            tabelas.add(new Tabela("Aulas coletivas", List.of("Aula", "Modalidade", "Horário", "Inscritos", "Ocupação"),
                    ocupacao.aulas().stream().map(aula -> List.of(aula.nome(), aula.modalidade(),
                            dia(aula.dia()) + ", " + aula.inicio(), aula.inscritos() + " de " + aula.capacidade(),
                            percentual(aula.percentual()))).toList()));
            var estacionamento = ocupacao.estacionamento();
            tabelas.add(new Tabela("Estacionamento", List.of("Vagas", "Ocupação média", "Horário de pico", "Ocupação no pico"),
                    List.of(List.of(inteiro(estacionamento.vagas()), percentual(estacionamento.percentualMedio()),
                            estacionamento.horarioPico().map(Object::toString).orElse("—"),
                            percentual(estacionamento.percentualPico())))));
        });
        conteudo.financeiro().ifPresent(financeiro -> {
            tabelas.add(new Tabela("Receita", List.of("Pagamentos confirmados", "Quantidade", "Estornos", "Quantidade",
                    "Receita líquida"), List.of(List.of(moeda(financeiro.receitaBruta()), inteiro(financeiro.pagamentos()),
                            moeda(financeiro.estornos()), inteiro(financeiro.quantidadeEstornos()),
                            moeda(financeiro.receitaLiquida())))));
            tabelas.add(new Tabela("Inadimplência", List.of("Valor com vencimento no período", "Valor em aberto", "Taxa"),
                    List.of(List.of(moeda(financeiro.totalComVencimento()), moeda(financeiro.valorEmAberto()),
                            percentual(financeiro.taxaInadimplencia())))));
            tabelas.add(new Tabela("Mensalidades vencidas e não pagas",
                    List.of("Aluno", "CPF", "Telefone", "Plano", "Vencimento", "Dias de atraso", "Valor"),
                    financeiro.atrasadas().stream().map(item -> List.of(item.nome(), MascaraDadosPessoais.cpf(item.cpf()),
                            MascaraDadosPessoais.telefone(item.telefone()), item.plano(), data(item.vencimento()),
                            String.valueOf(item.diasAtraso()), moeda(item.valor()))).toList()));
        });
        if (!conteudo.personais().isEmpty()) {
            tabelas.add(new Tabela("Desempenho dos personais",
                    List.of("Personal", "Alunos com plano", "Dias previstos", "Dias com treino", "Adesão", "Variação",
                            "Metas atingidas"),
                    conteudo.personais().stream().map(personal -> List.of(personal.nome(),
                            inteiro(personal.alunosComPlano()), inteiro(personal.diasPrevistos()),
                            inteiro(personal.diasComTreino()), percentual(personal.adesao()),
                            personal.variacaoAdesao().map(ExportacaoRelatorio::pontos).orElse("—"),
                            inteiro(personal.metasAtingidas()))).toList()));
        }
        if (!conteudo.alunosEmRisco().isEmpty()) {
            tabelas.add(new Tabela("Alunos em risco de evasão",
                    List.of("Aluno", "CPF", "Telefone", "Plano", "Personal", "Última entrada", "Dias sem entrar",
                            "Frequência semanal", "Motivo"),
                    conteudo.alunosEmRisco().stream().map(aluno -> List.of(aluno.nome(),
                            MascaraDadosPessoais.cpf(aluno.cpf()), MascaraDadosPessoais.telefone(aluno.telefone()),
                            aluno.plano(), aluno.personal().orElse("—"),
                            aluno.ultimaEntrada().map(ExportacaoRelatorio::data).orElse("—"),
                            String.valueOf(aluno.diasSemEntrar()),
                            decimal(aluno.frequenciaAnterior()) + " → " + decimal(aluno.frequenciaAtual()),
                            aluno.motivo() == MotivoRisco.SEM_ENTRADA ? "Sem entrada"
                                    : "Queda de " + aluno.queda().map(ExportacaoRelatorio::percentual).orElse("—")))
                            .toList()));
        }
        if (!conteudo.metas().isEmpty()) {
            tabelas.add(new Tabela("Metas da academia",
                    List.of("Indicador", "Período", "Meta", "Realizado", "Cumprimento", "Situação"),
                    conteudo.metas().stream().map(meta -> List.of(meta.indicador().getDescricao(),
                            data(meta.inicio()) + " a " + data(meta.fim()), valor(meta.indicador(), meta.desejado()),
                            valor(meta.indicador(), meta.realizado()), percentual(meta.percentual()),
                            meta.atingida() ? "Atingida" : "Não atingida")).toList()));
        }
        return new ExportacaoRelatorio("Relatório de " + relatorio.getTipo().getTitulo().toLowerCase(BRASIL), cabecalho,
                tabelas);
    }

    private static List<String> linhaIndicador(ValorIndicador valor, boolean comparar) {
        var indicador = valor.indicador();
        if (!comparar) {
            return List.of(indicador.getDescricao(), valor(indicador, valor.valor()));
        }
        var variacao = valor.variacao().orElseThrow();
        boolean taxa = indicador.getUnidade() == IndicadorAcademia.Unidade.PERCENTUAL;
        var absoluta = valor.anterior().isEmpty() ? "—"
                : taxa ? pontos(variacao.absoluta()) : sinal(variacao.absoluta()) + valor(indicador, variacao.absoluta().abs());
        return List.of(indicador.getDescricao(), valor(indicador, valor.valor()),
                valor.anterior().map(anterior -> valor(indicador, anterior)).orElse("sem dados"), absoluta,
                variacao.percentual().map(p -> sinal(p) + decimal(p.abs()) + "%").orElse("não calculada"),
                switch (variacao.resultado()) {
                case MELHOROU -> "Melhorou";
                case PIOROU -> "Piorou";
                case ESTAVEL -> "Estável";
                case SEM_BASE -> "Sem base";
                });
    }

    static String valor(IndicadorAcademia indicador, BigDecimal valor) {
        return switch (indicador.getUnidade()) {
        case QUANTIDADE -> inteiro(valor.setScale(0, RoundingMode.HALF_UP).intValue());
        case DECIMAL -> decimal(valor);
        case PERCENTUAL -> percentual(valor);
        case MOEDA -> moeda(valor);
        };
    }

    private static String sinal(BigDecimal valor) {
        return valor.signum() > 0 ? "+" : valor.signum() < 0 ? "−" : "";
    }

    private static String pontos(BigDecimal valor) {
        return sinal(valor) + decimal(valor.abs()) + " p.p.";
    }

    static String data(LocalDate data) {
        return data.format(DATA);
    }

    static String inteiro(int valor) {
        return NumberFormat.getIntegerInstance(BRASIL).format(valor);
    }

    static String decimal(BigDecimal valor) {
        var formato = NumberFormat.getNumberInstance(BRASIL);
        formato.setMinimumFractionDigits(1);
        formato.setMaximumFractionDigits(1);
        return formato.format(valor);
    }

    static String percentual(BigDecimal valor) {
        return decimal(valor) + "%";
    }

    static String moeda(BigDecimal valor) {
        var formato = NumberFormat.getCurrencyInstance(BRASIL);
        return formato.format(valor).replace(' ', ' ');
    }

    private static String agrupamento(Agrupamento agrupamento) {
        return switch (agrupamento) {
        case DIA -> "dia";
        case SEMANA -> "semana";
        case MES -> "mês";
        };
    }

    private static String perfil(Relatorio relatorio) {
        return switch (relatorio.getPerfil()) {
        case ADMINISTRADOR -> "gestor";
        case PERSONAL -> "personal";
        case USUARIO -> "aluno";
        };
    }

    private static String dia(DiaSemana dia) {
        return switch (dia) {
        case SEGUNDA -> "Seg";
        case TERCA -> "Ter";
        case QUARTA -> "Qua";
        case QUINTA -> "Qui";
        case SEXTA -> "Sex";
        case SABADO -> "Sáb";
        case DOMINGO -> "Dom";
        };
    }

    private static String classificacao(ClassificacaoOcupacao classificacao) {
        return switch (classificacao) {
        case PICO -> "Pico";
        case BAIXA -> "Baixa ocupação";
        case NORMAL -> "Normal";
        case FECHADO -> "Fechado";
        };
    }
}

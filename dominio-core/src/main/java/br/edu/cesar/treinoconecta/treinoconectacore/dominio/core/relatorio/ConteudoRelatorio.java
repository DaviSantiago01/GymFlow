package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.DiaSemana;
import dev.sauloaraujo.d5.framework.dominio.IObjetoValor;

/**
 * Valores calculados na geração do relatório. Cada tipo de relatório preenche apenas as seções que apresenta; as demais
 * ficam vazias. Depois de gravado, o conteúdo não é recalculado (RN018).
 */
public record ConteudoRelatorio(Resumo resumo, List<ValorIndicador> indicadores, List<ResultadoGrupo> grupos,
        Optional<MovimentoAlunos> movimento, List<ResultadoPlano> planos, Optional<Ocupacao> ocupacao,
        Optional<Financeiro> financeiro, List<DesempenhoPersonal> personais, List<AlunoEmRisco> alunosEmRisco,
        List<CumprimentoMeta> metas) implements IObjetoValor {
    public ConteudoRelatorio {
        Objects.requireNonNull(resumo);
        indicadores = List.copyOf(indicadores);
        grupos = List.copyOf(grupos);
        Objects.requireNonNull(movimento);
        planos = List.copyOf(planos);
        Objects.requireNonNull(ocupacao);
        Objects.requireNonNull(financeiro);
        personais = List.copyOf(personais);
        alunosEmRisco = List.copyOf(alunosEmRisco);
        metas = List.copyOf(metas);
    }

    public Optional<ValorIndicador> buscarIndicador(IndicadorAcademia indicador) {
        return indicadores.stream().filter(valor -> valor.indicador() == indicador).findFirst();
    }

    /** Quantidades usadas para informar se há dados para o período e os filtros escolhidos. */
    public record Resumo(int alunosAtivos, int presencas, int pagamentos, int mensalidades, int planosTreino) {
        public boolean semDados(TipoRelatorio tipo) {
            return switch (tipo) {
            case RECEITA_INADIMPLENCIA -> pagamentos == 0 && mensalidades == 0;
            case FREQUENCIA_OCUPACAO -> presencas == 0;
            case DESEMPENHO_PERSONAIS, ADESAO_TREINOS -> planosTreino == 0;
            case METAS_ACADEMIA -> false;
            default -> alunosAtivos == 0 && presencas == 0 && pagamentos == 0;
            };
        }
    }

    public record ValorIndicador(IndicadorAcademia indicador, BigDecimal valor, Optional<BigDecimal> anterior,
            Optional<Variacao> variacao) {
    }

    public record ResultadoGrupo(String rotulo, LocalDate inicio, LocalDate fim, BigDecimal frequencia,
            BigDecimal receita, int presencas, int novasMatriculas) {
    }

    /** Movimento de alunos ativos entre o início e o fim do período. Renovações e trancamentos não são saídas. */
    public record MovimentoAlunos(int inicio, int novasMatriculas, int reativacoes, int renovacoes, int evasoes,
            int saldoTrancamentos, int fim) {
    }

    public record ResultadoPlano(String plano, String nome, int alunosAtivos, int novasMatriculas,
            BigDecimal taxaEvasao, BigDecimal receita, BigDecimal participacao) {
    }

    public enum ClassificacaoOcupacao {
        PICO, NORMAL, BAIXA, FECHADO
    }

    public record CelulaOcupacao(DiaSemana dia, LocalTime inicio, BigDecimal media, BigDecimal percentual,
            ClassificacaoOcupacao classificacao) {
    }

    public record OcupacaoAula(String aula, String nome, String modalidade, DiaSemana dia, LocalTime inicio,
            int inscritos, int capacidade, BigDecimal percentual, ClassificacaoOcupacao classificacao) {
    }

    public record FaixaEstacionamento(LocalTime inicio, BigDecimal percentual) {
    }

    public record OcupacaoEstacionamento(int vagas, BigDecimal percentualMedio, Optional<LocalTime> horarioPico,
            BigDecimal percentualPico, List<FaixaEstacionamento> faixas) {
        public OcupacaoEstacionamento {
            faixas = List.copyOf(faixas);
        }
    }

    public record Ocupacao(int capacidadeSala, BigDecimal frequenciaMedia, int presencas, List<CelulaOcupacao> sala,
            List<OcupacaoAula> aulas, OcupacaoEstacionamento estacionamento) {
        public Ocupacao {
            sala = List.copyOf(sala);
            aulas = List.copyOf(aulas);
        }
    }

    public record FaixaAtraso(String rotulo, BigDecimal valor, int alunos) {
    }

    public record MensalidadeAtrasada(String aluno, String nome, String plano, LocalDate vencimento, long diasAtraso,
            BigDecimal valor, Optional<String> cpf, Optional<String> telefone) {
    }

    public record Financeiro(BigDecimal receitaBruta, int pagamentos, BigDecimal estornos, int quantidadeEstornos,
            BigDecimal receitaLiquida, BigDecimal totalComVencimento, BigDecimal valorEmAberto,
            BigDecimal taxaInadimplencia, List<FaixaAtraso> faixasAtraso, List<MensalidadeAtrasada> atrasadas) {
        public Financeiro {
            faixasAtraso = List.copyOf(faixasAtraso);
            atrasadas = List.copyOf(atrasadas);
        }
    }

    public record DesempenhoPersonal(String personal, String nome, int alunosComPlano, int diasPrevistos,
            int diasComTreino, BigDecimal adesao, Optional<BigDecimal> variacaoAdesao, int metasAtingidas,
            int alunosEmRisco) {
    }

    public enum MotivoRisco {
        SEM_ENTRADA, QUEDA_FREQUENCIA
    }

    public record AlunoEmRisco(String aluno, String nome, String plano, Optional<String> personal,
            Optional<LocalDate> ultimaEntrada, long diasSemEntrar, BigDecimal frequenciaAnterior,
            BigDecimal frequenciaAtual, MotivoRisco motivo, Optional<BigDecimal> queda, Optional<String> cpf,
            Optional<String> telefone) {
    }

    public record CumprimentoMeta(String meta, IndicadorAcademia indicador, BigDecimal desejado, BigDecimal realizado,
            BigDecimal percentual, boolean atingida, LocalDate inicio, LocalDate fim) {
    }
}

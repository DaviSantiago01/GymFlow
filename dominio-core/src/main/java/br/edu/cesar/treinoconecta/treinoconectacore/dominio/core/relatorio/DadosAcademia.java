package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.SituacaoAdesao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.DiaSemana;

/**
 * Registros da operação da academia consultados para calcular os indicadores. Os identificadores são os valores
 * gravados no banco, e as datas e horários estão no fuso da academia.
 */
public record DadosAcademia(List<MatriculaDado> matriculas, List<TrancamentoDado> trancamentos,
        List<EntradaDado> entradas, List<MensalidadeDado> mensalidades, List<AulaDado> aulas,
        List<UsoEstacionamentoDado> estacionamento, List<PlanoTreinoDado> planosTreino, List<ExecucaoDado> execucoes,
        List<MetaAtingidaDado> metasAtingidas, List<AlunoDado> alunos, List<PersonalDado> personais,
        List<PlanoDado> planos) {
    public DadosAcademia {
        matriculas = List.copyOf(matriculas);
        trancamentos = List.copyOf(trancamentos);
        entradas = List.copyOf(entradas);
        mensalidades = List.copyOf(mensalidades);
        aulas = List.copyOf(aulas);
        estacionamento = List.copyOf(estacionamento);
        planosTreino = List.copyOf(planosTreino);
        execucoes = List.copyOf(execucoes);
        metasAtingidas = List.copyOf(metasAtingidas);
        alunos = List.copyOf(alunos);
        personais = List.copyOf(personais);
        planos = List.copyOf(planos);
    }

    /** O período coberto pela matrícula é [início, encerramento). */
    public record MatriculaDado(String identificador, String aluno, String plano, int periodicidadeMeses,
            LocalDate inicio, Optional<LocalDate> encerramento, SituacaoAdesao situacao) {
        public MatriculaDado {
            Objects.requireNonNull(aluno);
            Objects.requireNonNull(inicio);
            Objects.requireNonNull(encerramento);
            Objects.requireNonNull(situacao);
        }

        public boolean ativaEm(LocalDate data) {
            return !data.isBefore(inicio) && encerramento.map(data::isBefore).orElse(true);
        }
    }

    public record TrancamentoDado(String matricula, String aluno, LocalDate inicio, LocalDate fim) {
        public boolean cobre(LocalDate data) {
            return !data.isBefore(inicio) && !data.isAfter(fim);
        }
    }

    public record EntradaDado(String aluno, LocalDateTime dataHora, Instant registro) {
    }

    public record PagamentoDado(BigDecimal valor, LocalDate data, Optional<LocalDate> estorno,
            Instant registro, Optional<Instant> registroEstorno) {
    }

    public record MensalidadeDado(String identificador, String aluno, String plano, LocalDate vencimento,
            BigDecimal valor, List<PagamentoDado> pagamentos) {
        public MensalidadeDado {
            pagamentos = List.copyOf(pagamentos);
        }

        /** Indica se, na data informada, havia um pagamento confirmado e ainda não estornado. */
        public boolean pagaEm(LocalDate data) {
            for (var pagamento : pagamentos) {
                boolean pago = !pagamento.data().isAfter(data);
                boolean estornado = pagamento.estorno().map(estorno -> !estorno.isAfter(data)).orElse(false);
                if (pago && !estornado) {
                    return true;
                }
            }
            return false;
        }
    }

    public record AulaDado(String identificador, String nome, String modalidade, DiaSemana dia, LocalTime inicio,
            int capacidade, int inscritos) {
    }

    public record UsoEstacionamentoDado(LocalDateTime entrada, LocalDateTime saida) {
    }

    public record PlanoTreinoDado(String aluno, String personal, int frequenciaSemanal, LocalDate inicio,
            Optional<LocalDate> fim) {
    }

    public record ExecucaoDado(String aluno, LocalDate data) {
    }

    public record MetaAtingidaDado(String aluno, LocalDate data) {
    }

    public record AlunoDado(String identificador, String nome, Optional<String> cpf, Optional<String> telefone) {
    }

    public record PersonalDado(String identificador, String nome) {
    }

    public record PlanoDado(String identificador, String nome) {
    }
}

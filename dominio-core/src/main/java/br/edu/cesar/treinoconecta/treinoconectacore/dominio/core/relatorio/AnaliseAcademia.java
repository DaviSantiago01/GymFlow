package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.SituacaoAdesao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.PlanoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.DiaSemana;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.ConteudoRelatorio.AlunoEmRisco;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.ConteudoRelatorio.CelulaOcupacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.ConteudoRelatorio.ClassificacaoOcupacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.ConteudoRelatorio.DesempenhoPersonal;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.ConteudoRelatorio.FaixaAtraso;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.ConteudoRelatorio.FaixaEstacionamento;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.ConteudoRelatorio.Financeiro;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.ConteudoRelatorio.MensalidadeAtrasada;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.ConteudoRelatorio.MotivoRisco;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.ConteudoRelatorio.MovimentoAlunos;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.ConteudoRelatorio.Ocupacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.ConteudoRelatorio.OcupacaoAula;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.ConteudoRelatorio.OcupacaoEstacionamento;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.ConteudoRelatorio.ResultadoGrupo;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.ConteudoRelatorio.ResultadoPlano;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.ConteudoRelatorio.Resumo;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.DadosAcademia.AlunoDado;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.DadosAcademia.MatriculaDado;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.DadosAcademia.PlanoTreinoDado;

/**
 * Cálculo dos indicadores da academia a partir dos registros de operação. As datas são consideradas até o dia de
 * referência, que é o dia da geração do relatório.
 */
public final class AnaliseAcademia {
    private static final BigDecimal CEM = new BigDecimal("100");
    private static final BigDecimal SETE = new BigDecimal("7");

    private final DadosAcademia dados;
    private final LocalDate hoje;
    private final Map<String, List<MatriculaDado>> matriculasPorAluno = new LinkedHashMap<>();
    private final Map<String, List<DadosAcademia.TrancamentoDado>> trancamentosPorMatricula = new HashMap<>();
    private final Map<String, TreeSet<LocalDate>> presencasPorAluno = new HashMap<>();
    private final Map<String, AlunoDado> alunos = new HashMap<>();

    public AnaliseAcademia(DadosAcademia dados, LocalDate hoje) {
        this.dados = Objects.requireNonNull(dados);
        this.hoje = Objects.requireNonNull(hoje);
        var ordenadas = new ArrayList<>(dados.matriculas());
        ordenadas.sort(Comparator.comparing(MatriculaDado::inicio).thenComparing(m -> Long.valueOf(m.identificador())));
        for (var matricula : ordenadas) {
            matriculasPorAluno.computeIfAbsent(matricula.aluno(), chave -> new ArrayList<>()).add(matricula);
        }
        for (var trancamento : dados.trancamentos()) {
            trancamentosPorMatricula.computeIfAbsent(trancamento.matricula(), chave -> new ArrayList<>()).add(trancamento);
        }
        for (var entrada : dados.entradas()) {
            presencasPorAluno.computeIfAbsent(entrada.aluno(), chave -> new TreeSet<>()).add(entrada.dataHora().toLocalDate());
        }
        for (var aluno : dados.alunos()) {
            alunos.put(aluno.identificador(), aluno);
        }
    }

    /** Restringe os registros aos alunos e itens selecionados pelos filtros (RN001 e RN003). */
    public AnaliseAcademia filtrar(FiltrosRelatorio filtros) {
        Objects.requireNonNull(filtros);
        Optional<Set<String>> populacao = Optional.empty();
        var matriculas = dados.matriculas();
        var mensalidades = dados.mensalidades();
        var planosTreino = dados.planosTreino();
        if (filtros.plano().isPresent()) {
            var plano = filtros.plano().get().toString();
            matriculas = matriculas.stream().filter(m -> m.plano().equals(plano)).toList();
            mensalidades = mensalidades.stream().filter(m -> m.plano().equals(plano)).toList();
            populacao = Optional.of(alunosDe(matriculas.stream().map(MatriculaDado::aluno).toList()));
        }
        if (filtros.personal().isPresent()) {
            var personal = filtros.personal().get().toString();
            planosTreino = planosTreino.stream().filter(p -> p.personal().equals(personal)).toList();
            var doPersonal = alunosDe(planosTreino.stream().map(PlanoTreinoDado::aluno).toList());
            populacao = Optional.of(populacao.map(atual -> intersecao(atual, doPersonal)).orElse(doPersonal));
        }
        var aulas = dados.aulas();
        if (filtros.modalidade().isPresent()) {
            var modalidade = filtros.modalidade().get();
            aulas = aulas.stream().filter(a -> a.modalidade().equalsIgnoreCase(modalidade)).toList();
        }
        if (filtros.aula().isPresent()) {
            var aula = filtros.aula().get().toString();
            aulas = aulas.stream().filter(a -> a.identificador().equals(aula)).toList();
        }
        if (populacao.isEmpty()) {
            return new AnaliseAcademia(new DadosAcademia(matriculas, dados.trancamentos(), dados.entradas(), mensalidades,
                    aulas, dados.estacionamento(), planosTreino, dados.execucoes(), dados.metasAtingidas(),
                    dados.alunos(), dados.personais(), dados.planos()), hoje);
        }
        var alunosConsiderados = populacao.get();
        return new AnaliseAcademia(new DadosAcademia(
                matriculas.stream().filter(m -> alunosConsiderados.contains(m.aluno())).toList(),
                dados.trancamentos().stream().filter(t -> alunosConsiderados.contains(t.aluno())).toList(),
                dados.entradas().stream().filter(e -> alunosConsiderados.contains(e.aluno())).toList(),
                mensalidades.stream().filter(m -> alunosConsiderados.contains(m.aluno())).toList(), aulas,
                dados.estacionamento(), planosTreino.stream().filter(p -> alunosConsiderados.contains(p.aluno())).toList(),
                dados.execucoes().stream().filter(e -> alunosConsiderados.contains(e.aluno())).toList(),
                dados.metasAtingidas().stream().filter(m -> alunosConsiderados.contains(m.aluno())).toList(),
                dados.alunos(), dados.personais(), dados.planos()), hoje);
    }

    private static Set<String> alunosDe(List<String> lista) {
        return new HashSet<>(lista);
    }

    private static Set<String> intersecao(Set<String> primeiro, Set<String> segundo) {
        var resultado = new HashSet<>(primeiro);
        resultado.retainAll(segundo);
        return resultado;
    }

    // ---------------------------------------------------------------- matrículas

    private boolean trancada(MatriculaDado matricula, LocalDate data) {
        for (var trancamento : trancamentosPorMatricula.getOrDefault(matricula.identificador(), List.of())) {
            if (trancamento.cobre(data)) {
                return true;
            }
        }
        return false;
    }

    public boolean ativo(String aluno, LocalDate data) {
        for (var matricula : matriculasPorAluno.getOrDefault(aluno, List.of())) {
            if (matricula.ativaEm(data) && !trancada(matricula, data)) {
                return true;
            }
        }
        return false;
    }

    private boolean trancado(String aluno, LocalDate data) {
        for (var matricula : matriculasPorAluno.getOrDefault(aluno, List.of())) {
            if (matricula.ativaEm(data) && trancada(matricula, data)) {
                return true;
            }
        }
        return false;
    }

    public Set<String> ativosEm(LocalDate data) {
        var ativos = new HashSet<String>();
        for (var aluno : matriculasPorAluno.keySet()) {
            if (ativo(aluno, data)) {
                ativos.add(aluno);
            }
        }
        return ativos;
    }

    private int trancadosEm(LocalDate data) {
        int total = 0;
        for (var aluno : matriculasPorAluno.keySet()) {
            if (trancado(aluno, data)) {
                total++;
            }
        }
        return total;
    }

    public enum TipoInicio {
        NOVA, RENOVACAO, REATIVACAO
    }

    /**
     * RN005: a matrícula é nova quando o aluno não tem vínculo anterior. Quando a anterior foi encerrada por troca e a nova
     * começa dentro do prazo de tolerância, é renovação; nos demais casos, inclusive depois de um cancelamento, é
     * reativação.
     */
    public TipoInicio classificar(MatriculaDado matricula) {
        Objects.requireNonNull(matricula);
        MatriculaDado anterior = null;
        for (var outra : matriculasPorAluno.getOrDefault(matricula.aluno(), List.of())) {
            if (outra == matricula) {
                break;
            }
            anterior = outra;
        }
        if (anterior == null) {
            return TipoInicio.NOVA;
        }
        var fimAnterior = anterior.encerramento().orElse(matricula.inicio());
        long intervalo = ChronoUnit.DAYS.between(fimAnterior, matricula.inicio());
        if (anterior.situacao() == SituacaoAdesao.ENCERRADA && intervalo <= ParametrosRelatorio.TOLERANCIA_RENOVACAO_DIAS) {
            return TipoInicio.RENOVACAO;
        }
        return TipoInicio.REATIVACAO;
    }

    public List<MatriculaDado> iniciadas(PeriodoAnalise periodo, TipoInicio tipo) {
        var resultado = new ArrayList<MatriculaDado>();
        for (var lista : matriculasPorAluno.values()) {
            for (var matricula : lista) {
                if (periodo.contem(matricula.inicio()) && classificar(matricula) == tipo) {
                    resultado.add(matricula);
                }
            }
        }
        return resultado;
    }

    private boolean seguidaDeNovaMatricula(MatriculaDado matricula) {
        var fim = matricula.encerramento().orElseThrow();
        for (var outra : matriculasPorAluno.getOrDefault(matricula.aluno(), List.of())) {
            if (outra != matricula && !outra.inicio().isBefore(fim)
                    && ChronoUnit.DAYS.between(fim, outra.inicio()) <= ParametrosRelatorio.TOLERANCIA_RENOVACAO_DIAS) {
                return true;
            }
        }
        return false;
    }

    /** RN007: cancelamentos e encerramentos sem renovação. Trancamentos não encerram a matrícula. */
    public List<MatriculaDado> evasoes(PeriodoAnalise periodo) {
        var resultado = new ArrayList<MatriculaDado>();
        for (var lista : matriculasPorAluno.values()) {
            for (var matricula : lista) {
                if (matricula.encerramento().filter(periodo::contem).isEmpty()) {
                    continue;
                }
                if (matricula.situacao() == SituacaoAdesao.CANCELADA || !seguidaDeNovaMatricula(matricula)) {
                    resultado.add(matricula);
                }
            }
        }
        return resultado;
    }

    public BigDecimal taxaEvasao(PeriodoAnalise periodo) {
        return percentual(evasoes(periodo).size(), ativosEm(periodo.inicio()).size());
    }

    /**
     * RN008: cada fim de ciclo contratado dentro do período é um vencimento. A matrícula é renovada quando continua
     * ativa no ciclo seguinte ou quando o aluno faz nova matrícula até o fim da tolerância. Vencimentos cuja tolerância
     * ainda não terminou e que ainda não foram renovados ficam fora do cálculo.
     */
    public BigDecimal taxaRenovacao(PeriodoAnalise periodo) {
        int vencimentos = 0;
        int renovadas = 0;
        for (var lista : matriculasPorAluno.values()) {
            for (var matricula : lista) {
                for (int ciclo = 1;; ciclo++) {
                    var vencimento = matricula.inicio().plusMonths((long) ciclo * matricula.periodicidadeMeses());
                    if (vencimento.isAfter(periodo.fim())) {
                        break;
                    }
                    if (!matricula.ativaEm(vencimento.minusDays(1))) {
                        break;
                    }
                    if (vencimento.isBefore(periodo.inicio())) {
                        continue;
                    }
                    boolean renovada = matricula.ativaEm(vencimento) || seguidaDeNovaMatricula(matricula);
                    boolean prazoAberto = vencimento.plusDays(ParametrosRelatorio.TOLERANCIA_RENOVACAO_DIAS).isAfter(hoje);
                    if (renovada) {
                        vencimentos++;
                        renovadas++;
                    } else if (!prazoAberto) {
                        vencimentos++;
                    }
                }
            }
        }
        return percentual(renovadas, vencimentos);
    }

    public MovimentoAlunos movimento(PeriodoAnalise periodo) {
        int inicio = ativosEm(periodo.inicio()).size();
        int fim = ativosEm(periodo.fim()).size();
        int saldoTrancamentos = trancadosEm(periodo.fim()) - trancadosEm(periodo.inicio());
        return new MovimentoAlunos(inicio, iniciadas(periodo, TipoInicio.NOVA).size(),
                iniciadas(periodo, TipoInicio.REATIVACAO).size(), iniciadas(periodo, TipoInicio.RENOVACAO).size(),
                evasoes(periodo).size(), saldoTrancamentos, fim);
    }

    // ---------------------------------------------------------------- frequência

    private record Frequencia(long presencas, long diasAtivos) {
        BigDecimal porSemana() {
            if (diasAtivos == 0) {
                return BigDecimal.ZERO.setScale(1);
            }
            return BigDecimal.valueOf(presencas).multiply(SETE).divide(BigDecimal.valueOf(diasAtivos), 1,
                    RoundingMode.HALF_UP);
        }
    }

    /** RN006: considera só os dias em que cada aluno esteve ativo; várias entradas no mesmo dia são uma presença. */
    private Frequencia frequencia(String aluno, PeriodoAnalise periodo) {
        long presencas = 0;
        long dias = 0;
        Set<LocalDate> datas = presencasPorAluno.getOrDefault(aluno, new TreeSet<>());
        for (var data = periodo.inicio(); !data.isAfter(periodo.fim()) && !data.isAfter(hoje); data = data.plusDays(1)) {
            if (ativo(aluno, data)) {
                dias++;
                if (datas.contains(data)) {
                    presencas++;
                }
            }
        }
        return new Frequencia(presencas, dias);
    }

    private Frequencia frequenciaGeral(PeriodoAnalise periodo) {
        long presencas = 0;
        long dias = 0;
        for (var aluno : matriculasPorAluno.keySet()) {
            var frequencia = frequencia(aluno, periodo);
            presencas += frequencia.presencas();
            dias += frequencia.diasAtivos();
        }
        return new Frequencia(presencas, dias);
    }

    public BigDecimal frequenciaMedia(PeriodoAnalise periodo) {
        return frequenciaGeral(periodo).porSemana();
    }

    public int presencas(PeriodoAnalise periodo) {
        return (int) frequenciaGeral(periodo).presencas();
    }

    // ---------------------------------------------------------------- financeiro

    private record Receita(BigDecimal bruta, int pagamentos, BigDecimal estornos, int quantidadeEstornos) {
        BigDecimal liquida() {
            return bruta.subtract(estornos);
        }
    }

    /** RN009: pagamentos confirmados com data no período, menos os estornos feitos no período. */
    private Receita receita(PeriodoAnalise periodo) {
        var bruta = BigDecimal.ZERO;
        var estornos = BigDecimal.ZERO;
        int pagamentos = 0;
        int quantidadeEstornos = 0;
        for (var mensalidade : dados.mensalidades()) {
            for (var pagamento : mensalidade.pagamentos()) {
                if (periodo.contem(pagamento.data())) {
                    bruta = bruta.add(pagamento.valor());
                    pagamentos++;
                }
                if (pagamento.estorno().filter(periodo::contem).isPresent()) {
                    estornos = estornos.add(pagamento.valor());
                    quantidadeEstornos++;
                }
            }
        }
        return new Receita(dinheiro(bruta), pagamentos, dinheiro(estornos), quantidadeEstornos);
    }

    public BigDecimal receitaLiquida(PeriodoAnalise periodo) {
        return receita(periodo).liquida();
    }

    /**
     * RN010: mensalidades com vencimento no período que, no dia da geração, continuam sem pagamento depois da carência.
     */
    public Financeiro financeiro(PeriodoAnalise periodo) {
        var receita = receita(periodo);
        var total = BigDecimal.ZERO;
        var emAberto = BigDecimal.ZERO;
        var atrasadas = new ArrayList<MensalidadeAtrasada>();
        var faixas = new LinkedHashMap<String, FaixaAcumulada>();
        int primeiraFaixa = ParametrosRelatorio.CARENCIA_INADIMPLENCIA_DIAS + 1;
        faixas.put(primeiraFaixa + " a 15 dias", new FaixaAcumulada());
        faixas.put("16 a 30 dias", new FaixaAcumulada());
        faixas.put("Mais de 30 dias", new FaixaAcumulada());
        for (var mensalidade : dados.mensalidades()) {
            if (!periodo.contem(mensalidade.vencimento())) {
                continue;
            }
            total = total.add(mensalidade.valor());
            long atraso = ChronoUnit.DAYS.between(mensalidade.vencimento(), hoje);
            if (mensalidade.pagaEm(hoje) || atraso <= ParametrosRelatorio.CARENCIA_INADIMPLENCIA_DIAS) {
                continue;
            }
            emAberto = emAberto.add(mensalidade.valor());
            var aluno = alunos.get(mensalidade.aluno());
            atrasadas.add(new MensalidadeAtrasada(mensalidade.aluno(), nomeDoAluno(mensalidade.aluno()),
                    nomeDoPlano(mensalidade.plano()), mensalidade.vencimento(), atraso, dinheiro(mensalidade.valor()),
                    aluno == null ? Optional.empty() : aluno.cpf(), aluno == null ? Optional.empty() : aluno.telefone()));
            var faixa = Objects.requireNonNull(atraso <= 15 ? faixas.get(primeiraFaixa + " a 15 dias")
                    : atraso <= 30 ? faixas.get("16 a 30 dias") : faixas.get("Mais de 30 dias"));
            faixa.valor = faixa.valor.add(mensalidade.valor());
            faixa.alunos.add(mensalidade.aluno());
        }
        atrasadas.sort(Comparator.comparingLong(MensalidadeAtrasada::diasAtraso).reversed());
        var resumoFaixas = new ArrayList<FaixaAtraso>();
        faixas.forEach((rotulo, faixa) -> resumoFaixas.add(new FaixaAtraso(rotulo, dinheiro(faixa.valor), faixa.alunos.size())));
        return new Financeiro(receita.bruta(), receita.pagamentos(), receita.estornos(), receita.quantidadeEstornos(),
                dinheiro(receita.liquida()), dinheiro(total), dinheiro(emAberto), percentual(emAberto, total),
                resumoFaixas, atrasadas);
    }

    private static final class FaixaAcumulada {
        private BigDecimal valor = BigDecimal.ZERO;
        private final Set<String> alunos = new HashSet<>();
    }

    public BigDecimal taxaInadimplencia(PeriodoAnalise periodo) {
        return financeiro(periodo).taxaInadimplencia();
    }

    // ---------------------------------------------------------------- ocupação

    private static ClassificacaoOcupacao classificarOcupacao(BigDecimal percentual) {
        if (percentual.compareTo(ParametrosRelatorio.OCUPACAO_PICO_PERCENTUAL) > 0) {
            return ClassificacaoOcupacao.PICO;
        }
        if (percentual.compareTo(ParametrosRelatorio.OCUPACAO_BAIXA_PERCENTUAL) < 0) {
            return ClassificacaoOcupacao.BAIXA;
        }
        return ClassificacaoOcupacao.NORMAL;
    }

    /** RN012: ocupação média de cada faixa de horário, das aulas coletivas e do estacionamento. */
    public Ocupacao ocupacao(PeriodoAnalise periodo) {
        var datasPorDia = new LinkedHashMap<DiaSemana, List<LocalDate>>();
        for (var dia : DiaSemana.values()) {
            datasPorDia.put(dia, new ArrayList<>());
        }
        for (var data = periodo.inicio(); !data.isAfter(periodo.fim()) && !data.isAfter(hoje); data = data.plusDays(1)) {
            datasPorDia.computeIfAbsent(DiaSemana.values()[data.getDayOfWeek().ordinal()], chave -> new ArrayList<>()).add(data);
        }
        var contagem = new HashMap<String, Integer>();
        for (var entrada : dados.entradas()) {
            var data = entrada.dataHora().toLocalDate();
            if (!periodo.contem(data)) {
                continue;
            }
            int hora = entrada.dataHora().getHour();
            int inicioFaixa = hora - (hora - ParametrosRelatorio.ABERTURA.getHour()) % ParametrosRelatorio.DURACAO_FAIXA_HORAS;
            var chave = data.getDayOfWeek().ordinal() + "|" + inicioFaixa;
            contagem.merge(chave, 1, Integer::sum);
        }
        var celulas = new ArrayList<CelulaOcupacao>();
        var capacidade = BigDecimal.valueOf(ParametrosRelatorio.CAPACIDADE_SALA);
        for (var dia : DiaSemana.values()) {
            var datas = datasPorDia.getOrDefault(dia, List.of());
            for (var faixa = ParametrosRelatorio.ABERTURA; faixa.isBefore(ParametrosRelatorio.FECHAMENTO);
                    faixa = faixa.plusHours(ParametrosRelatorio.DURACAO_FAIXA_HORAS)) {
                var diaDaSemana = DayOfWeek.values()[dia.ordinal()];
                if (!ParametrosRelatorio.abertaNaFaixa(diaDaSemana, faixa) || datas.isEmpty()) {
                    celulas.add(new CelulaOcupacao(dia, faixa, BigDecimal.ZERO, BigDecimal.ZERO,
                            ClassificacaoOcupacao.FECHADO));
                    continue;
                }
                int total = contagem.getOrDefault(dia.ordinal() + "|" + faixa.getHour(), 0);
                var media = BigDecimal.valueOf(total).divide(BigDecimal.valueOf(datas.size()), 1, RoundingMode.HALF_UP);
                var taxa = media.multiply(CEM).divide(capacidade, 0, RoundingMode.HALF_UP);
                celulas.add(new CelulaOcupacao(dia, faixa, media, taxa, classificarOcupacao(taxa)));
            }
        }
        var aulas = new ArrayList<OcupacaoAula>();
        for (var aula : dados.aulas()) {
            var taxa = BigDecimal.valueOf(aula.inscritos()).multiply(CEM)
                    .divide(BigDecimal.valueOf(aula.capacidade()), 0, RoundingMode.HALF_UP);
            aulas.add(new OcupacaoAula(aula.identificador(), aula.nome(), aula.modalidade(), aula.dia(), aula.inicio(),
                    aula.inscritos(), aula.capacidade(), taxa, classificarOcupacao(taxa)));
        }
        aulas.sort(Comparator.comparing(OcupacaoAula::percentual).reversed());
        var frequencia = frequenciaGeral(periodo);
        return new Ocupacao(ParametrosRelatorio.CAPACIDADE_SALA, frequencia.porSemana(), (int) frequencia.presencas(),
                celulas, aulas, estacionamento(periodo));
    }

    private OcupacaoEstacionamento estacionamento(PeriodoAnalise periodo) {
        var faixas = new ArrayList<FaixaEstacionamento>();
        var vagas = BigDecimal.valueOf(ParametrosRelatorio.VAGAS_ESTACIONAMENTO);
        long dias = 0;
        for (var data = periodo.inicio(); !data.isAfter(periodo.fim()) && !data.isAfter(hoje); data = data.plusDays(1)) {
            dias++;
        }
        var soma = BigDecimal.ZERO;
        Optional<LocalTime> pico = Optional.empty();
        var maior = BigDecimal.ZERO;
        for (var hora = ParametrosRelatorio.ABERTURA; hora.isBefore(ParametrosRelatorio.FECHAMENTO); hora = hora.plusHours(1)) {
            long ocupadas = 0;
            for (var uso : dados.estacionamento()) {
                var data = uso.entrada().toLocalDate();
                if (!periodo.contem(data)) {
                    continue;
                }
                var inicioHora = data.atTime(hora);
                if (uso.entrada().isBefore(inicioHora.plusHours(1)) && uso.saida().isAfter(inicioHora)) {
                    ocupadas++;
                }
            }
            var taxa = dias == 0 ? BigDecimal.ZERO
                    : BigDecimal.valueOf(ocupadas).multiply(CEM).divide(vagas.multiply(BigDecimal.valueOf(dias)), 0,
                            RoundingMode.HALF_UP);
            faixas.add(new FaixaEstacionamento(hora, taxa));
            soma = soma.add(taxa);
            if (taxa.compareTo(maior) > 0) {
                maior = taxa;
                pico = Optional.of(hora);
            }
        }
        var media = faixas.isEmpty() ? BigDecimal.ZERO
                : soma.divide(BigDecimal.valueOf(faixas.size()), 0, RoundingMode.HALF_UP);
        return new OcupacaoEstacionamento(ParametrosRelatorio.VAGAS_ESTACIONAMENTO, media, pico, maior, faixas);
    }

    // ---------------------------------------------------------------- personais

    /** RN013: alunos com plano ativo, adesão (dias com treino ÷ dias previstos) e metas atingidas por personal. */
    public List<DesempenhoPersonal> personais(PeriodoAnalise periodo, List<AlunoEmRisco> emRisco) {
        var resultado = new ArrayList<DesempenhoPersonal>();
        for (var personal : dados.personais()) {
            var atual = adesao(personal.identificador(), periodo);
            if (atual.alunos().isEmpty()) {
                continue;
            }
            var anterior = adesao(personal.identificador(), periodo.anterior());
            Optional<BigDecimal> variacao = anterior.previstos() == 0 ? Optional.empty()
                    : Optional.of(atual.taxa().subtract(anterior.taxa()));
            int metas = (int) dados.metasAtingidas().stream()
                    .filter(meta -> atual.alunos().contains(meta.aluno()) && periodo.contem(meta.data())).count();
            int risco = (int) emRisco.stream().filter(aluno -> aluno.personal().filter(personal.nome()::equals).isPresent())
                    .count();
            resultado.add(new DesempenhoPersonal(personal.identificador(), personal.nome(), atual.alunos().size(),
                    atual.previstos(), atual.realizados(), atual.taxa(), variacao, metas, risco));
        }
        resultado.sort(Comparator.comparing(DesempenhoPersonal::adesao).reversed());
        return resultado;
    }

    private record Adesao(Set<String> alunos, int previstos, int realizados) {
        BigDecimal taxa() {
            return percentual(Math.min(realizados, previstos), previstos);
        }
    }

    private Adesao adesao(String personal, PeriodoAnalise periodo) {
        var alunosDoPersonal = new HashSet<String>();
        double previstos = 0;
        int realizados = 0;
        var limite = periodo.fim().isAfter(hoje) ? hoje : periodo.fim();
        for (var plano : dados.planosTreino()) {
            if (!plano.personal().equals(personal) || limite.isBefore(periodo.inicio())) {
                continue;
            }
            var fimPlano = plano.fim().map(fim -> fim.minusDays(1)).orElse(limite);
            var sobreposicao = periodo.intersecao(plano.inicio(), fimPlano.isAfter(limite) ? limite : fimPlano);
            if (sobreposicao.isEmpty()) {
                continue;
            }
            var intervalo = sobreposicao.get();
            alunosDoPersonal.add(plano.aluno());
            previstos += intervalo.dias() * plano.frequenciaSemanal() / 7.0;
            realizados += (int) dados.execucoes().stream()
                    .filter(execucao -> execucao.aluno().equals(plano.aluno()) && intervalo.contem(execucao.data()))
                    .map(DadosAcademia.ExecucaoDado::data).distinct().count();
        }
        return new Adesao(alunosDoPersonal, (int) Math.round(previstos), realizados);
    }

    // ---------------------------------------------------------------- risco de evasão

    /**
     * RN014: alunos ativos no fim do período sem entrada há mais dias que o limite ou com queda de frequência acima do
     * percentual definido em relação ao período anterior.
     */
    public List<AlunoEmRisco> alunosEmRisco(PeriodoAnalise periodo) {
        var resultado = new ArrayList<AlunoEmRisco>();
        var referencia = periodo.fim();
        for (var aluno : ativosEm(referencia)) {
            var datas = presencasPorAluno.getOrDefault(aluno, new TreeSet<>());
            var ultima = Optional.ofNullable(datas.floor(referencia));
            var desde = ultima.orElseGet(() -> primeiroDiaAtivo(aluno));
            long diasSemEntrar = ChronoUnit.DAYS.between(desde, referencia);
            var atual = frequencia(aluno, periodo).porSemana();
            var anterior = frequencia(aluno, periodo.anterior()).porSemana();
            Optional<BigDecimal> queda = anterior.signum() == 0 ? Optional.empty()
                    : Optional.of(anterior.subtract(atual).multiply(CEM).divide(anterior, 0, RoundingMode.HALF_UP));
            boolean semEntrada = diasSemEntrar > ParametrosRelatorio.DIAS_SEM_ENTRADA_RISCO;
            boolean comQueda = queda.filter(valor -> valor.compareTo(ParametrosRelatorio.QUEDA_FREQUENCIA_RISCO_PERCENTUAL) > 0)
                    .isPresent();
            if (!semEntrada && !comQueda) {
                continue;
            }
            var dadosAluno = alunos.get(aluno);
            resultado.add(new AlunoEmRisco(aluno, nomeDoAluno(aluno), nomeDoPlano(planoAtual(aluno, referencia)),
                    personalAtual(aluno, referencia), ultima, diasSemEntrar, anterior, atual,
                    semEntrada ? MotivoRisco.SEM_ENTRADA : MotivoRisco.QUEDA_FREQUENCIA, queda,
                    dadosAluno == null ? Optional.empty() : dadosAluno.cpf(),
                    dadosAluno == null ? Optional.empty() : dadosAluno.telefone()));
        }
        resultado.sort(Comparator.comparingLong(AlunoEmRisco::diasSemEntrar).reversed().thenComparing(AlunoEmRisco::nome));
        return resultado;
    }

    private LocalDate primeiroDiaAtivo(String aluno) {
        return matriculasPorAluno.getOrDefault(aluno, List.of()).stream().map(MatriculaDado::inicio)
                .min(Comparator.naturalOrder()).orElse(hoje);
    }

    private String planoAtual(String aluno, LocalDate data) {
        for (var matricula : matriculasPorAluno.getOrDefault(aluno, List.of())) {
            if (matricula.ativaEm(data)) {
                return matricula.plano();
            }
        }
        return "";
    }

    private Optional<String> personalAtual(String aluno, LocalDate data) {
        PlanoTreinoDado atual = null;
        for (var plano : dados.planosTreino()) {
            boolean vigente = !plano.inicio().isAfter(data) && plano.fim().map(data::isBefore).orElse(true);
            if (plano.aluno().equals(aluno) && vigente) {
                atual = plano;
            }
        }
        if (atual == null) {
            return Optional.empty();
        }
        var personal = atual.personal();
        return dados.personais().stream().filter(p -> p.identificador().equals(personal)).map(DadosAcademia.PersonalDado::nome)
                .findFirst();
    }

    // ---------------------------------------------------------------- resultados

    public BigDecimal valor(IndicadorAcademia indicador, PeriodoAnalise periodo) {
        Objects.requireNonNull(indicador);
        Objects.requireNonNull(periodo);
        return switch (indicador) {
        case ALUNOS_ATIVOS -> BigDecimal.valueOf(ativosEm(periodo.fim()).size());
        case NOVAS_MATRICULAS -> BigDecimal.valueOf(iniciadas(periodo, TipoInicio.NOVA).size());
        case REATIVACOES -> BigDecimal.valueOf(iniciadas(periodo, TipoInicio.REATIVACAO).size());
        case FREQUENCIA_MEDIA -> frequenciaMedia(periodo);
        case TAXA_EVASAO -> taxaEvasao(periodo);
        case TAXA_RENOVACAO -> taxaRenovacao(periodo);
        case RECEITA_LIQUIDA -> dinheiro(receitaLiquida(periodo));
        case TAXA_INADIMPLENCIA -> taxaInadimplencia(periodo);
        case ALUNOS_EM_RISCO -> BigDecimal.valueOf(alunosEmRisco(periodo).size());
        };
    }

    /** Indica se há algum registro da operação no período, para decidir se a comparação tem base. */
    public boolean possuiDados(PeriodoAnalise periodo) {
        var resumo = resumo(periodo);
        return resumo.alunosAtivos() > 0 || resumo.presencas() > 0 || resumo.pagamentos() > 0;
    }

    public Resumo resumo(PeriodoAnalise periodo) {
        int pagamentos = 0;
        int mensalidades = 0;
        for (var mensalidade : dados.mensalidades()) {
            if (periodo.contem(mensalidade.vencimento())) {
                mensalidades++;
            }
            for (var pagamento : mensalidade.pagamentos()) {
                if (periodo.contem(pagamento.data())) {
                    pagamentos++;
                }
            }
        }
        int planos = (int) dados.planosTreino().stream()
                .filter(plano -> periodo.intersecao(plano.inicio(), plano.fim().orElse(periodo.fim())).isPresent()).count();
        return new Resumo(ativosEm(periodo.fim()).size(), presencas(periodo), pagamentos, mensalidades, planos);
    }

    public List<ResultadoGrupo> grupos(PeriodoAnalise periodo, Agrupamento agrupamento) {
        var resultado = new ArrayList<ResultadoGrupo>();
        for (var grupo : agrupamento.dividir(periodo)) {
            var intervalo = grupo.periodo();
            var frequencia = frequenciaGeral(intervalo);
            resultado.add(new ResultadoGrupo(grupo.rotulo(), intervalo.inicio(), intervalo.fim(), frequencia.porSemana(),
                    dinheiro(receitaLiquida(intervalo)), (int) frequencia.presencas(),
                    iniciadas(intervalo, TipoInicio.NOVA).size()));
        }
        return resultado;
    }

    public List<ResultadoPlano> resultadosPorPlano(PeriodoAnalise periodo) {
        var receitaTotal = receitaLiquida(periodo);
        var resultado = new ArrayList<ResultadoPlano>();
        for (var plano : dados.planos()) {
            var filtro = new FiltrosRelatorio(Optional.of(new PlanoIdentificador(plano.identificador())), Optional.empty(),
                    Optional.empty(), Optional.empty());
            var analise = filtrar(filtro);
            int ativos = analise.ativosEm(periodo.fim()).size();
            var receita = analise.receitaLiquida(periodo);
            if (ativos == 0 && receita.signum() == 0) {
                continue;
            }
            resultado.add(new ResultadoPlano(plano.identificador(), plano.nome(), ativos,
                    analise.iniciadas(periodo, TipoInicio.NOVA).size(), analise.taxaEvasao(periodo), dinheiro(receita),
                    percentual(receita, receitaTotal)));
        }
        resultado.sort(Comparator.comparing(ResultadoPlano::alunosAtivos).reversed());
        return resultado;
    }

    private String nomeDoAluno(String aluno) {
        var dadosAluno = alunos.get(aluno);
        return dadosAluno == null ? aluno : dadosAluno.nome();
    }

    private String nomeDoPlano(String plano) {
        return dados.planos().stream().filter(p -> p.identificador().equals(plano)).map(DadosAcademia.PlanoDado::nome)
                .findFirst().orElse(plano);
    }

    static BigDecimal percentual(long parte, long total) {
        return percentual(BigDecimal.valueOf(parte), BigDecimal.valueOf(total));
    }

    static BigDecimal percentual(BigDecimal parte, BigDecimal total) {
        if (total.signum() == 0) {
            return BigDecimal.ZERO.setScale(1);
        }
        return parte.multiply(CEM).divide(total, 1, RoundingMode.HALF_UP);
    }

    private static BigDecimal dinheiro(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    public LocalDate getHoje() { return hoje; }
}

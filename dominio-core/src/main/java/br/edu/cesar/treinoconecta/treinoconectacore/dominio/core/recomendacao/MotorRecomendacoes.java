package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aulacoletiva.AulaColetiva;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.IndicadorFisico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.execucaodeexercicio.ExecucaoDeExercicio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.ExercicioIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.DiaSemana;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreino;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.ParametrosRelatorio;

/**
 * Aplica os critérios de recomendação (RN007 a RN010 e recomendações de aulas e planos) aos dados do aluno. Itens
 * incompatíveis com as restrições, o objetivo e o nível do aluno são descartados antes da geração (RN004 e RN005).
 */
public final class MotorRecomendacoes {
    private static final Locale BRASIL = Locale.of("pt", "BR");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DIA_MES = DateTimeFormatter.ofPattern("dd/MM");
    private static final BigDecimal CEM = new BigDecimal("100");

    private final ContextoRecomendacao contexto;
    private final AlunoIdentificador aluno;
    private final Instant agora;

    public MotorRecomendacoes(ContextoRecomendacao contexto, Instant agora) {
        this.contexto = Objects.requireNonNull(contexto);
        this.aluno = contexto.perfil().getAluno();
        this.agora = Objects.requireNonNull(agora);
    }

    public List<Recomendacao> gerar() {
        var resultado = new ArrayList<Recomendacao>();
        reavaliacao().ifPresent(resultado::add);
        reengajamento().ifPresent(resultado::add);
        resultado.addAll(substituicoes());
        resultado.addAll(revisoesDePlano());
        resultado.addAll(aulas());
        oferta().ifPresent(resultado::add);
        return resultado;
    }

    private Recomendacao criar(CriterioRecomendacao criterio, String item, String titulo, String descricao,
            String justificativa, List<Evidencia> evidencias, ReferenciasRecomendacao referencias,
            Optional<SugestaoSubstituicao> sugestao) {
        return Recomendacao.gerar(aluno, criterio, item, titulo, descricao, justificativa, evidencias, referencias,
                sugestao, agora, contexto.hoje());
    }

    // ------------------------------------------------------------------ RN009

    /** RN009: sem avaliação física ou com a última mais antiga que o intervalo definido pela academia. */
    Optional<Recomendacao> reavaliacao() {
        var hoje = contexto.hoje();
        var ultima = contexto.avaliacoes().stream().map(avaliacao -> avaliacao.getData()).max(Comparator.naturalOrder());
        if (ultima.isEmpty()) {
            return Optional.of(criar(CriterioRecomendacao.REAVALIACAO_FISICA, "AVALIACAO", "Agende sua avaliação física",
                    "Você ainda não tem avaliação física registrada. Ela orienta seu treino e o acompanhamento das suas metas.",
                    "Nenhuma avaliação física registrada", List.of(), ReferenciasRecomendacao.nenhuma(), Optional.empty()));
        }
        long dias = ChronoUnit.DAYS.between(ultima.get(), hoje);
        if (dias <= ParametrosRecomendacao.INTERVALO_REAVALIACAO_DIAS) {
            return Optional.empty();
        }
        return Optional.of(criar(CriterioRecomendacao.REAVALIACAO_FISICA, "AVALIACAO", "Agende sua reavaliação física",
                "Sua última avaliação foi em " + ultima.get().format(DATA) + ", há " + dias
                        + " dias. A academia recomenda uma nova avaliação a cada "
                        + ParametrosRecomendacao.INTERVALO_REAVALIACAO_DIAS + " dias.",
                "Última avaliação física há " + dias + " dias, acima do intervalo de "
                        + ParametrosRecomendacao.INTERVALO_REAVALIACAO_DIAS + " dias",
                List.of(), ReferenciasRecomendacao.nenhuma(), Optional.empty()));
    }

    // ------------------------------------------------------------------ RN008

    /**
     * RN008: frequência abaixo do percentual mínimo da frequência prevista no plano durante semanas consecutivas.
     * Sugere a faixa de horário menos ocupada dentro da disponibilidade do aluno ou, sem alternativa, o contato do
     * personal.
     */
    Optional<Recomendacao> reengajamento() {
        if (contexto.planoAtivo().isEmpty()) {
            return Optional.empty();
        }
        var plano = contexto.planoAtivo().get();
        int previstas = plano.getFrequenciaSemanal();
        var minimo = BigDecimal.valueOf(previstas).multiply(ParametrosRecomendacao.PERCENTUAL_FREQUENCIA_MINIMA)
                .divide(CEM, 1, RoundingMode.HALF_UP);
        var inicioSemanaAtual = contexto.hoje().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        var datas = new HashSet<LocalDate>();
        for (var entrada : contexto.entradasDoAluno()) {
            datas.add(entrada.toLocalDate());
        }
        var evidencias = new ArrayList<Evidencia>();
        var contagens = new ArrayList<Integer>();
        int exibidas = ParametrosRecomendacao.SEMANAS_EXIBIDAS_NA_JUSTIFICATIVA;
        for (int semana = exibidas; semana >= 1; semana--) {
            var inicio = inicioSemanaAtual.minusWeeks(semana);
            int treinos = 0;
            for (int dia = 0; dia < 7; dia++) {
                if (datas.contains(inicio.plusDays(dia))) {
                    treinos++;
                }
            }
            contagens.add(treinos);
            boolean abaixo = BigDecimal.valueOf(treinos).compareTo(minimo) < 0;
            boolean recente = semana <= ParametrosRecomendacao.SEMANAS_FREQUENCIA_BAIXA;
            evidencias.add(new Evidencia(inicio.format(DIA_MES), BigDecimal.valueOf(treinos), abaixo && recente));
        }
        var recentes = contagens.subList(contagens.size() - ParametrosRecomendacao.SEMANAS_FREQUENCIA_BAIXA, contagens.size());
        if (recentes.stream().anyMatch(treinos -> BigDecimal.valueOf(treinos).compareTo(minimo) >= 0)) {
            return Optional.empty();
        }
        int semanas = ParametrosRecomendacao.SEMANAS_FREQUENCIA_BAIXA;
        String justificativa;
        if (recentes.stream().distinct().count() == 1) {
            int treinos = recentes.get(0);
            justificativa = treinos == 0 ? "Nenhuma entrada registrada nas últimas " + semanas + " semanas, e o plano prevê "
                    + previstas + " treinos por semana" : "Frequência de " + treinos + (treinos == 1 ? " treino" : " treinos") + " por semana nas últimas "
                    + semanas + " semanas, abaixo dos " + previstas + " previstos";
        } else {
            justificativa = "Frequência abaixo de " + ParametrosRecomendacao.PERCENTUAL_FREQUENCIA_MINIMA.toPlainString()
                    + "% do previsto (" + previstas + " treinos por semana) nas últimas " + semanas + " semanas seguidas";
        }
        var vezes = descreverTreinos(recentes);
        var resumo = (vezes.equals("nenhuma vez") ? "Você não treinou " : "Você treinou ") + vezes + " nas últimas " + semanas
                + " semanas, e seu plano prevê " + previstas + ".";
        var sugestao = horarioAlternativo(plano);
        if (sugestao.isPresent()) {
            var horario = sugestao.get();
            return Optional.of(criar(CriterioRecomendacao.REENGAJAMENTO, "HORARIO:" + horario.sugerida().getHour(),
                    "Que tal treinar a partir das " + hora(horario.sugerida()) + "?",
                    resumo + " Nas " + listarDias(horario.dias()) + ", a sala fica bem mais vazia das "
                            + hora(horario.sugerida()) + " às " + hora(horario.sugerida().plusHours(2)) + ": em média "
                            + horario.ocupacaoSugerida().toPlainString() + "% da capacidade, contra "
                            + horario.ocupacaoAtual().toPlainString() + "% das " + hora(horario.atual()) + " às "
                            + hora(horario.atual().plusHours(2)) + ", quando você costuma vir.",
                    justificativa, evidencias, ReferenciasRecomendacao.doPersonal(Optional.of(plano.getPersonal())),
                    Optional.empty()));
        }
        return Optional.of(criar(CriterioRecomendacao.REENGAJAMENTO, "CONTATO_PERSONAL",
                "Que tal conversar com seu personal sobre a sua rotina?",
                resumo + " Seu personal pode ajustar os dias e o volume do treino para caber na sua semana.",
                justificativa, evidencias, ReferenciasRecomendacao.doPersonal(Optional.of(plano.getPersonal())),
                Optional.empty()));
    }

    private static String descreverTreinos(List<Integer> contagens) {
        if (contagens.stream().distinct().count() == 1) {
            int treinos = contagens.get(0);
            if (treinos == 0) {
                return "nenhuma vez";
            }
            return treinos + (treinos == 1 ? " vez" : " vezes") + " por semana";
        }
        return contagens.stream().map(String::valueOf).reduce((a, b) -> a + ", " + b).orElse("") + " vezes por semana";
    }

    private record HorarioAlternativo(Set<DiaSemana> dias, LocalTime atual, BigDecimal ocupacaoAtual,
            LocalTime sugerida, BigDecimal ocupacaoSugerida) {
    }

    private Optional<HorarioAlternativo> horarioAlternativo(PlanoDeTreino plano) {
        var disponibilidade = contexto.perfil().getDisponibilidade();
        if (disponibilidade.isEmpty() || contexto.entradasDaAcademia().isEmpty()) {
            return Optional.empty();
        }
        var dias = EnumSet.noneOf(DiaSemana.class);
        for (var treino : plano.getTreinos()) {
            if (disponibilidade.get().dias().contains(treino.dia())) {
                dias.add(treino.dia());
            }
        }
        if (dias.isEmpty()) {
            dias.addAll(disponibilidade.get().dias());
        }
        var ocupacao = ocupacaoPorFaixa();
        var faixaHabitual = faixaHabitual().orElse(faixaDe(disponibilidade.get().inicio()));
        var atual = media(ocupacao, dias, faixaHabitual);
        LocalTime melhor = null;
        var menor = atual;
        for (var faixa = ParametrosRelatorio.ABERTURA; faixa.isBefore(ParametrosRelatorio.FECHAMENTO); faixa = faixa.plusHours(2)) {
            if (faixa.isBefore(disponibilidade.get().inicio()) || faixa.plusHours(2).isAfter(disponibilidade.get().fim())
                    || faixa.equals(faixaHabitual)) {
                continue;
            }
            var media = media(ocupacao, dias, faixa);
            if (media.compareTo(menor.subtract(BigDecimal.TEN)) < 0 && (melhor == null || media.compareTo(menor) < 0)) {
                menor = media;
                melhor = faixa;
            }
        }
        if (melhor == null) {
            return Optional.empty();
        }
        return Optional.of(new HorarioAlternativo(dias, faixaHabitual, atual, melhor, menor));
    }

    /** Ocupação média da sala, em percentual da capacidade, por dia da semana e faixa de duas horas. */
    private Map<String, BigDecimal> ocupacaoPorFaixa() {
        var contagem = new HashMap<String, Integer>();
        var datasPorDia = new HashMap<DiaSemana, Set<LocalDate>>();
        for (var entrada : contexto.entradasDaAcademia()) {
            var dia = diaDaSemana(entrada.toLocalDate());
            datasPorDia.computeIfAbsent(dia, chave -> new HashSet<>()).add(entrada.toLocalDate());
            contagem.merge(dia + "|" + faixaDe(entrada.toLocalTime()).getHour(), 1, Integer::sum);
        }
        var resultado = new HashMap<String, BigDecimal>();
        var capacidade = BigDecimal.valueOf(ParametrosRelatorio.CAPACIDADE_SALA);
        contagem.forEach((chave, total) -> {
            var dia = DiaSemana.valueOf(chave.substring(0, chave.indexOf('|')));
            int ocorrencias = datasPorDia.getOrDefault(dia, Set.of()).size();
            if (ocorrencias > 0) {
                resultado.put(chave, BigDecimal.valueOf(total).multiply(CEM)
                        .divide(capacidade.multiply(BigDecimal.valueOf(ocorrencias)), 0, RoundingMode.HALF_UP));
            }
        });
        return resultado;
    }

    private static BigDecimal media(Map<String, BigDecimal> ocupacao, Set<DiaSemana> dias, LocalTime faixa) {
        var soma = BigDecimal.ZERO;
        for (var dia : dias) {
            soma = soma.add(ocupacao.getOrDefault(dia + "|" + faixa.getHour(), BigDecimal.ZERO));
        }
        return soma.divide(BigDecimal.valueOf(dias.size()), 0, RoundingMode.HALF_UP);
    }

    private Optional<LocalTime> faixaHabitual() {
        var contagem = new HashMap<LocalTime, Integer>();
        for (var entrada : contexto.entradasDoAluno()) {
            contagem.merge(faixaDe(entrada.toLocalTime()), 1, Integer::sum);
        }
        return contagem.entrySet().stream().max(Map.Entry.<LocalTime, Integer>comparingByValue()
                .thenComparing(Map.Entry.comparingByKey())).map(Map.Entry::getKey);
    }

    private static LocalTime faixaDe(LocalTime horario) {
        int abertura = ParametrosRelatorio.ABERTURA.getHour();
        int hora = Math.max(horario.getHour(), abertura);
        return LocalTime.of(hora - (hora - abertura) % ParametrosRelatorio.DURACAO_FAIXA_HORAS, 0);
    }

    // ------------------------------------------------------------------ RN007

    /** RN007: exercício sem aumento de carga nem de repetições na quantidade de execuções definida pela academia. */
    List<Recomendacao> substituicoes() {
        if (contexto.planoAtivo().isEmpty()) {
            return List.of();
        }
        var plano = contexto.planoAtivo().get();
        var noPlano = new HashSet<ExercicioIdentificador>();
        plano.getTreinos().forEach(treino -> treino.series().forEach(serie -> noPlano.add(serie.exercicio())));
        var resultado = new ArrayList<Recomendacao>();
        int limite = ParametrosRecomendacao.EXECUCOES_ESTAGNACAO;
        for (var treino : plano.getTreinos()) {
            for (var serie : treino.series()) {
                var execucoes = new ArrayList<>(contexto.execucoes().getOrDefault(serie.exercicio(), List.of()));
                execucoes.sort(Comparator.comparing(ExecucaoDeExercicio::getData));
                if (execucoes.size() < limite) {
                    continue;
                }
                var ultimas = execucoes.subList(execucoes.size() - limite, execucoes.size());
                if (!estagnado(ultimas)) {
                    continue;
                }
                var atual = contexto.classificacao(serie.exercicio());
                if (atual.isEmpty()) {
                    continue;
                }
                var substituto = contexto.catalogo().stream()
                        .filter(item -> item.grupoMuscular() == atual.get().grupoMuscular())
                        .filter(item -> !noPlano.contains(item.exercicio()))
                        .filter(item -> item.incompatibilidadeCom(contexto.perfil()).isEmpty())
                        .min(Comparator.comparing(ExercicioClassificado::nome));
                if (substituto.isEmpty()) {
                    continue;
                }
                var sugestao = new SugestaoSubstituicao(serie.exercicio(), substituto.get().exercicio(), serie.quantidade(),
                        serie.repeticoesMinimas(), serie.repeticoesMaximas(), serie.carga(), serie.intervaloSegundos());
                var evidencias = new ArrayList<Evidencia>();
                for (var execucao : ultimas) {
                    var data = execucao.getData().atZone(ParametrosAcademia.FUSO).toLocalDate();
                    evidencias.add(new Evidencia(data.format(DATA) + " · " + execucao.getSeries() + "x"
                            + execucao.getRepeticoes(), execucao.getCarga(), false));
                }
                resultado.add(criar(CriterioRecomendacao.SUBSTITUICAO_EXERCICIO, "EXERCICIO:" + serie.exercicio(),
                        "Substituir " + minusculo(atual.get().nome()),
                        treino.nome() + ": " + minusculo(atual.get().nome()) + " → " + minusculo(substituto.get().nome())
                                + ". Sem evolução de carga ou repetições em " + limite + " treinos seguidos.",
                        "A carga e as repetições não aumentaram em " + limite
                                + " execuções seguidas. O limite definido pela academia é de " + limite + " execuções.",
                        evidencias, ReferenciasRecomendacao.doPlanoDeTreino(plano.getPersonal(),
                                plano.getIdentificador().orElseThrow()),
                        Optional.of(sugestao)));
            }
        }
        return resultado;
    }

    /** Nenhuma execução depois da primeira superou a carga ou as repetições dela. */
    static boolean estagnado(List<ExecucaoDeExercicio> execucoes) {
        var base = execucoes.get(0);
        for (var execucao : execucoes.subList(1, execucoes.size())) {
            if (execucao.getCarga().compareTo(base.getCarga()) > 0 || execucao.getRepeticoes() > base.getRepeticoes()) {
                return false;
            }
        }
        return true;
    }

    // ------------------------------------------------------------------ RN010

    /** RN010: indicador da avaliação física com tendência contrária à meta ativa em avaliações consecutivas. */
    List<Recomendacao> revisoesDePlano() {
        if (contexto.planoAtivo().isEmpty()) {
            return List.of();
        }
        var plano = contexto.planoAtivo().get();
        var resultado = new ArrayList<Recomendacao>();
        var avaliacoes = new ArrayList<>(contexto.avaliacoes());
        avaliacoes.sort(Comparator.comparing(avaliacao -> avaliacao.getData()));
        int necessarias = ParametrosRecomendacao.AVALIACOES_TENDENCIA + 1;
        for (var meta : contexto.metasAtivas()) {
            var indicador = meta.getIndicador();
            var pontos = avaliacoes.stream().filter(avaliacao -> avaliacao.existeValor(indicador)).toList();
            if (pontos.size() < necessarias) {
                continue;
            }
            var ultimos = pontos.subList(pontos.size() - necessarias, pontos.size());
            boolean reduzir = meta.getValorDesejado().compareTo(meta.getValorInicial()) < 0;
            boolean contraria = true;
            for (int indice = 1; indice < ultimos.size(); indice++) {
                int comparacao = ultimos.get(indice).buscarValor(indicador)
                        .compareTo(ultimos.get(indice - 1).buscarValor(indicador));
                contraria &= reduzir ? comparacao > 0 : comparacao < 0;
            }
            if (!contraria) {
                continue;
            }
            var evidencias = new ArrayList<Evidencia>();
            var valores = new ArrayList<String>();
            for (var avaliacao : ultimos) {
                var valor = avaliacao.buscarValor(indicador);
                evidencias.add(new Evidencia(avaliacao.getData().format(DATA), valor, true));
                valores.add(numero(valor));
            }
            var nome = nomeIndicador(indicador);
            resultado.add(criar(CriterioRecomendacao.REVISAO_PLANO, "META:" + meta.getIdentificador().orElseThrow(),
                    "Revisar o plano de treino",
                    nome + (reduzir ? " subiu" : " caiu") + " em " + ParametrosRecomendacao.AVALIACOES_TENDENCIA
                            + " avaliações seguidas; a meta é chegar a " + numero(meta.getValorDesejado()) + " "
                            + unidade(indicador) + " até " + meta.getFim().format(DATA) + ".",
                    "Tendência contrária à meta de " + nome.toLowerCase(BRASIL) + ": " + String.join(" → ", valores) + " "
                            + unidade(indicador) + " nas últimas avaliações",
                    evidencias,
                    ReferenciasRecomendacao.doPlanoDeTreino(plano.getPersonal(), plano.getIdentificador().orElseThrow()),
                    Optional.empty()));
        }
        return resultado;
    }

    // ------------------------------------------------------------------ RN004 a RN006

    /** Aulas coletivas compatíveis com o aluno, com vaga e sem conflito com as aulas em que ele já está inscrito. */
    List<Recomendacao> aulas() {
        var compativeis = new ArrayList<AulaColetiva>();
        for (var aula : contexto.aulas()) {
            if (aula.isAtiva() && aula.incompatibilidadeCom(contexto.perfil(), contexto.inscricoes()).isEmpty()) {
                compativeis.add(aula);
            }
        }
        compativeis.sort(Comparator.comparingInt(AulaColetiva::vagas).reversed().thenComparing(AulaColetiva::getNome));
        var resultado = new ArrayList<Recomendacao>();
        var perfil = contexto.perfil();
        for (var aula : compativeis.subList(0, Math.min(compativeis.size(), ParametrosRecomendacao.MAXIMO_AULAS_SUGERIDAS))) {
            var restricoes = perfil.getRestricoes().stream()
                    .map(restricao -> "a restrição no seu " + restricao.regiao().getDescricao().toLowerCase(BRASIL)).toList();
            var compatibilidade = restricoes.isEmpty() ? "seu objetivo e sua disponibilidade"
                    : String.join(", ", restricoes) + " e sua disponibilidade";
            int vagas = aula.vagas();
            resultado.add(criar(CriterioRecomendacao.AULA_COMPATIVEL, "AULA:" + aula.getIdentificador().orElseThrow(),
                    aula.getNome() + " " + diaNoPlural(aula.getDia()) + ", " + hora(aula.getInicio()),
                    "Aula de " + aula.getImpacto().getDescricao().toLowerCase(BRASIL) + ", compatível com "
                            + compatibilidade + ". " + (vagas == 1 ? "Resta 1 vaga." : "Restam " + vagas + " vagas."),
                    "Aula de nível " + aula.getNivel().getDescricao().toLowerCase(BRASIL) + " adequada ao objetivo de "
                            + perfil.getObjetivo().map(o -> o.getDescricao().toLowerCase(BRASIL)).orElse("treino")
                            + ", sem contraindicação para as suas restrições, dentro da sua disponibilidade e com "
                            + vagas + (vagas == 1 ? " vaga" : " vagas"),
                    List.of(), ReferenciasRecomendacao.daAula(aula.getIdentificador().orElseThrow()), Optional.empty()));
        }
        return resultado;
    }

    // ------------------------------------------------------------------ planos e serviços

    /** Oferta de plano mais longo com mensalidade equivalente menor, para quem já está há alguns meses na academia. */
    Optional<Recomendacao> oferta() {
        if (contexto.planoContratado().isEmpty() || contexto.inicioAdesao().isEmpty()) {
            return Optional.empty();
        }
        var atual = contexto.planoContratado().get();
        long meses = ChronoUnit.MONTHS.between(contexto.inicioAdesao().get(), contexto.hoje());
        if (meses < ParametrosRecomendacao.MESES_PARA_OFERTA) {
            return Optional.empty();
        }
        var mensalAtual = mensal(atual.getValor(), atual.getDuracaoMeses());
        var melhor = contexto.planosAtivos().stream()
                .filter(plano -> plano.getDuracaoMeses() > atual.getDuracaoMeses())
                .filter(plano -> mensal(plano.getValor(), plano.getDuracaoMeses()).compareTo(mensalAtual) < 0)
                .min(Comparator.comparing(plano -> mensal(plano.getValor(), plano.getDuracaoMeses())));
        if (melhor.isEmpty()) {
            return Optional.empty();
        }
        var plano = melhor.get();
        var mensalNovo = mensal(plano.getValor(), plano.getDuracaoMeses());
        var desconto = BigDecimal.ONE.subtract(mensalNovo.divide(mensalAtual, 4, RoundingMode.HALF_UP)).multiply(CEM)
                .setScale(0, RoundingMode.HALF_UP);
        return Optional.of(criar(CriterioRecomendacao.OFERTA_PLANO, "PLANO:" + plano.getIdentificador().orElseThrow(),
                "Plano " + minusculo(plano.getNome()) + " com " + desconto.toPlainString() + "% de desconto",
                "Você está no plano " + minusculo(atual.getNome()) + " há " + meses + " meses. No plano "
                        + minusculo(plano.getNome()) + ", a mensalidade equivalente cai de " + moeda(mensalAtual) + " para "
                        + moeda(mensalNovo) + ".",
                "Aluno há " + meses + " meses no plano " + minusculo(atual.getNome()) + "; o plano "
                        + minusculo(plano.getNome()) + " custa " + desconto.toPlainString() + "% menos por mês",
                List.of(), ReferenciasRecomendacao.doPlano(plano.getIdentificador().orElseThrow()), Optional.empty()));
    }

    private static BigDecimal mensal(BigDecimal valor, int meses) {
        return valor.divide(BigDecimal.valueOf(meses), 2, RoundingMode.HALF_UP);
    }

    // ------------------------------------------------------------------ formatação

    private static DiaSemana diaDaSemana(LocalDate data) {
        return DiaSemana.values()[data.getDayOfWeek().ordinal()];
    }

    static String hora(LocalTime horario) {
        return horario.getMinute() == 0 ? horario.getHour() + "h" : horario.getHour() + "h" + "%02d".formatted(horario.getMinute());
    }

    static String diaNoPlural(DiaSemana dia) {
        return (dia == DiaSemana.SABADO || dia == DiaSemana.DOMINGO ? "aos " : "às ") + plural(dia);
    }

    private static String plural(DiaSemana dia) {
        return switch (dia) {
        case SEGUNDA -> "segundas";
        case TERCA -> "terças";
        case QUARTA -> "quartas";
        case QUINTA -> "quintas";
        case SEXTA -> "sextas";
        case SABADO -> "sábados";
        case DOMINGO -> "domingos";
        };
    }

    private static String listarDias(Set<DiaSemana> dias) {
        var nomes = new ArrayList<String>();
        for (var dia : new LinkedHashSet<>(EnumSet.copyOf(dias))) {
            nomes.add(plural(dia));
        }
        if (nomes.size() == 1) {
            return nomes.get(0);
        }
        return String.join(", ", nomes.subList(0, nomes.size() - 1)) + " e " + nomes.get(nomes.size() - 1);
    }

    private static String minusculo(String texto) {
        return texto.isEmpty() ? texto : texto.substring(0, 1).toLowerCase(BRASIL) + texto.substring(1);
    }

    private static String numero(BigDecimal valor) {
        var formato = NumberFormat.getNumberInstance(BRASIL);
        formato.setMaximumFractionDigits(1);
        return formato.format(valor);
    }

    private static String moeda(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(BRASIL).format(valor).replace(' ', ' ');
    }

    static String nomeIndicador(IndicadorFisico indicador) {
        return switch (indicador) {
        case PESO -> "Peso";
        case IMC -> "IMC";
        case CINTURA -> "Cintura";
        case ABDOMEN -> "Abdômen";
        case QUADRIL -> "Quadril";
        case PEITORAL -> "Peitoral";
        case BRACO -> "Braço";
        case COXA -> "Coxa";
        case PANTURRILHA -> "Panturrilha";
        };
    }

    private static String unidade(IndicadorFisico indicador) {
        return switch (indicador) {
        case PESO -> "kg";
        case IMC -> "kg/m²";
        default -> "cm";
        };
    }

}

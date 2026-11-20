package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.registroacesso;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.registroacesso.spi.IRegistroAcessoAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroacesso.RegistroAcesso;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroacesso.RegistroAcessoAutorizacoes;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroacesso.RegistroAcessoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroacesso.RegistroAcessoServico;
import dev.sauloaraujo.d5.framework.aplicacao.impl.AgregadoAplicacaoServicoAbstrato;

/** Consultas da recepção: presentes, histórico e frequência (F006, RN008 a RN012). */
public final class RegistroAcessoAplicacaoServico extends AgregadoAplicacaoServicoAbstrato<RegistroAcessoIdentificador,
        RegistroAcesso, RegistroAcessoAutorizacoes, RegistroAcessoProjecao, RegistroAcessoConsulta> {
    /** Período máximo de uma consulta de frequência, para limitar a quantidade de registros devolvidos. */
    public static final int DIAS_MAXIMOS_FREQUENCIA = 366;
    public static final String PERIODO_LONGO = "Consulte a frequência em períodos de até " + DIAS_MAXIMOS_FREQUENCIA + " dias";

    private final IRegistroAcessoAplicacaoRepositorio repositorio;
    private final RegistroAcessoServico servico;
    private final Clock relogio;

    public RegistroAcessoAplicacaoServico(IRegistroAcessoAplicacaoRepositorio repositorio, RegistroAcessoServico servico,
            Clock relogio) {
        super(repositorio, servico);
        this.repositorio = Objects.requireNonNull(repositorio);
        this.servico = Objects.requireNonNull(servico);
        this.relogio = Objects.requireNonNull(relogio);
    }

    public PainelPresenca presentes() {
        validarBusca();
        var agora = Instant.now(relogio);
        var parametros = servico.getParametros();
        var registros = repositorio.presentes().stream().map(item -> permanencia(item, agora)).toList();
        return new PainelPresenca(parametros.capacidadeMaxima(), registros.size(),
                parametros.permanenciaMaxima().toMinutes(), parametros.abertura(), parametros.fechamento(), registros);
    }

    /** RN011 e RN012: registros e frequência do aluno entre as datas, inclusive, no fuso da academia. */
    public FrequenciaAluno frequencia(AlunoIdentificador aluno, Optional<LocalDate> inicioOpcional,
            Optional<LocalDate> fimOpcional) {
        Objects.requireNonNull(aluno);
        validarBusca();
        var fim = fimOpcional.orElseGet(servico::hoje);
        var inicio = inicioOpcional.orElse(fim.minusDays(29));
        Validate.isTrue(!inicio.isAfter(fim), RegistroAcessoConsulta.PERIODO_INVERTIDO);
        long dias = ChronoUnit.DAYS.between(inicio, fim) + 1;
        Validate.isTrue(dias <= DIAS_MAXIMOS_FREQUENCIA, PERIODO_LONGO);
        var agora = Instant.now(relogio);
        var registros = repositorio.doAluno(aluno, inicio.atStartOfDay(ParametrosAcademia.FUSO).toInstant(),
                fim.plusDays(1).atStartOfDay(ParametrosAcademia.FUSO).toInstant()).stream()
                .map(item -> permanencia(item, agora)).toList();
        var diasComparecidos = new HashSet<LocalDate>();
        long minutos = 0;
        for (var item : registros) {
            diasComparecidos.add(item.registro().entrada().atZone(ParametrosAcademia.FUSO).toLocalDate());
            minutos += item.minutos();
        }
        long media = registros.isEmpty() ? 0 : minutos / registros.size();
        return new FrequenciaAluno(aluno.toString(), inicio, fim, dias, diasComparecidos.size(), registros.size(), minutos,
                media, registros);
    }

    public boolean presente(AlunoIdentificador aluno) {
        Objects.requireNonNull(aluno);
        return repositorio.presentes().stream().anyMatch(item -> item.aluno().equals(aluno.toString()));
    }

    private PermanenciaRegistro permanencia(RegistroAcessoProjecao registro, Instant agora) {
        var fim = Optional.ofNullable(registro.saida()).orElse(agora);
        var duracao = fim.isBefore(registro.entrada()) ? Duration.ZERO : Duration.between(registro.entrada(), fim);
        boolean excedeu = registro.verificar() || duracao.compareTo(servico.getParametros().permanenciaMaxima()) > 0;
        return new PermanenciaRegistro(registro, duracao.toMinutes(), excedeu);
    }

    private void validarBusca() {
        servico.validarConsulta();
    }
}

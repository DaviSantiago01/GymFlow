package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import org.jspecify.annotations.Nullable;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.IAlunoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.spi.IPerfilAlunoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.DiaSemana;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.CategoriaRecomendacao;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoServicoAbstrato;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;

public final class PerfilAlunoServico extends AgregadoServicoAbstrato<PerfilAlunoIdentificador, PerfilAluno, PerfilAlunoAutorizacoes> {
    public static final String IDENTIFICADOR_EXISTENTE_MODELO = "Já existe perfil para o aluno %s";
    public static final String IDENTIFICADOR_INEXISTENTE_MODELO = "Não existe perfil para o aluno %s";
    public static final String ALUNO_INEXISTENTE_MODELO = "Não existe aluno com o identificador %s";
    public static final String OBJETIVO_INVALIDO = "Escolha um objetivo válido: hipertrofia, emagrecimento ou condicionamento";
    public static final String NIVEL_INVALIDO = "Escolha um nível de experiência válido: iniciante, intermediário ou avançado";
    public static final String DISPONIBILIDADE_INVALIDA = "A disponibilidade semanal deve ter entre 1 e 7 dias";
    public static final String DIA_INVALIDO = "Dia da semana inválido";
    public static final String HORARIO_INVALIDO = "Informe um horário inicial anterior ao horário final";
    public static final String REGIAO_INVALIDA = "Escolha a região da restrição física";
    public static final String DESCRICAO_RESTRICAO_INVALIDA = "Descreva a restrição física em até 250 caracteres";
    public static final String RESTRICAO_REPETIDA_MODELO = "Já existe uma restrição registrada para %s";
    public static final String RESTRICAO_INEXISTENTE = "Não há restrição registrada nessa região";
    public static final String CATEGORIA_OBRIGATORIA_MODELO = "As recomendações de %s não podem ser desativadas";
    public static final String CATEGORIA_INVALIDA = "Categoria de recomendação inválida";

    private final IPerfilAlunoRepositorio repositorio;
    private final IAlunoRepositorio alunos;
    private final Clock relogio;

    public PerfilAlunoServico(ISegurancaServico seguranca, IPerfilAlunoRepositorio repositorio, IAlunoRepositorio alunos,
            Clock relogio) {
        super(seguranca, repositorio, IDENTIFICADOR_EXISTENTE_MODELO, IDENTIFICADOR_INEXISTENTE_MODELO);
        this.repositorio = Objects.requireNonNull(repositorio);
        this.alunos = Objects.requireNonNull(alunos);
        this.relogio = Objects.requireNonNull(relogio);
    }

    /** O aluno consulta e edita o próprio perfil; o personal consulta o perfil dos alunos para analisar sugestões. */
    @Override
    public PerfilAlunoAutorizacoes criarAutorizacoes(Optional<PerfilAlunoIdentificador> identificador) {
        Objects.requireNonNull(identificador);
        boolean aluno = getSegurancaServico().possuiAutorizacao(Autorizacao.USUARIO.name());
        boolean personal = getSegurancaServico().possuiAutorizacao(Autorizacao.PERSONAL.name());
        boolean proprio = aluno && identificador.map(id -> id.toString().equals(alunoAutenticado().toString())).orElse(true);
        return new PerfilAlunoAutorizacoes(proprio || personal, proprio);
    }

    /** Perfil gravado do aluno ou, se ele ainda não preencheu, um perfil vazio. */
    public PerfilAluno consultar(AlunoIdentificador aluno) {
        Objects.requireNonNull(aluno);
        var identificador = new PerfilAlunoIdentificador(aluno.toString());
        validarAutorizacao(Optional.of(identificador), PerfilAlunoAutorizacoes::getBuscar);
        validarExistencia(alunos::existePorIdentificador, aluno, ALUNO_INEXISTENTE_MODELO);
        return repositorio.existePorIdentificador(identificador) ? repositorio.buscarPorIdentificador(identificador)
                : PerfilAluno.vazio(aluno, relogio.instant());
    }

    public PerfilAluno meuPerfil() {
        return consultar(alunoAutenticado());
    }

    public PerfilAluno salvarDadosDeTreino(String objetivo, String nivel, Collection<String> dias, @Nullable LocalTime inicio,
            @Nullable LocalTime fim) {
        Validate.notNull(dias, DISPONIBILIDADE_INVALIDA);
        if (inicio == null || fim == null) {
            throw new IllegalArgumentException(HORARIO_INVALIDO);
        }
        var diasSemana = EnumSet.noneOf(DiaSemana.class);
        for (var dia : dias) {
            diasSemana.add(diaDaSemana(dia));
        }
        var disponibilidade = new Disponibilidade(diasSemana, inicio, fim);
        var perfil = editavel();
        return gravar(perfil.atualizarDadosDeTreino(ObjetivoAluno.de(objetivo), NivelExperiencia.de(nivel), disponibilidade,
                relogio.instant()));
    }

    public PerfilAluno registrarRestricao(String regiao, String descricao) {
        var perfil = editavel();
        return gravar(perfil.registrarRestricao(RegiaoCorporal.de(regiao), descricao, hoje(), relogio.instant()));
    }

    public PerfilAluno removerRestricao(String regiao) {
        var perfil = editavel();
        return gravar(perfil.removerRestricao(RegiaoCorporal.de(regiao), relogio.instant()));
    }

    public PerfilAluno salvarPreferencias(Collection<String> categoriasDesativadas) {
        Objects.requireNonNull(categoriasDesativadas);
        var desativadas = EnumSet.noneOf(CategoriaRecomendacao.class);
        for (var categoria : categoriasDesativadas) {
            desativadas.add(CategoriaRecomendacao.de(categoria));
        }
        var perfil = editavel();
        return gravar(perfil.definirPreferencias(desativadas, relogio.instant()));
    }

    private PerfilAluno editavel() {
        var aluno = alunoAutenticado();
        validarAutorizacao(Optional.of(new PerfilAlunoIdentificador(aluno.toString())), PerfilAlunoAutorizacoes::getEditar);
        return consultar(aluno);
    }

    private PerfilAluno gravar(PerfilAluno perfil) {
        if (repositorio.existePorIdentificador(perfil.getIdentificador().orElseThrow())) {
            repositorio.alterar(perfil);
            return perfil;
        }
        return repositorio.incluir(perfil);
    }

    private static DiaSemana diaDaSemana(String valor) {
        Validate.notBlank(valor, DIA_INVALIDO);
        for (var dia : DiaSemana.values()) {
            if (dia.name().equalsIgnoreCase(valor.strip())) {
                return dia;
            }
        }
        throw new IllegalArgumentException(DIA_INVALIDO);
    }

    private AlunoIdentificador alunoAutenticado() {
        return alunos.buscarPorUsuario(getSegurancaServico().getUsuario()).getIdentificador().orElseThrow();
    }

    private LocalDate hoje() {
        return relogio.instant().atZone(ParametrosAcademia.FUSO).toLocalDate();
    }
}

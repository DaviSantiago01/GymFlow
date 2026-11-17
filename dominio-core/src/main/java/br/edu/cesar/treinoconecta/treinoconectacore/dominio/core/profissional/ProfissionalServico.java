package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.spi.IProfissionalRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.vinculopersonal.spi.IVinculoPersonalAlunoRepositorio;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoServicoAbstrato;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;

public final class ProfissionalServico
        extends AgregadoServicoAbstrato<ProfissionalIdentificador, Profissional, ProfissionalAutorizacoes> {
    public static final String IDENTIFICADOR_EXISTENTE_MODELO = "Já existe um profissional com o identificador %s";
    public static final String IDENTIFICADOR_INEXISTENTE_MODELO = "Não existe profissional com o identificador %s";
    public static final String NOME_OBRIGATORIO = "Informe o nome do profissional com 2 a " + Profissional.TAMANHO_NOME
            + " caracteres";
    public static final String CONTATO_OBRIGATORIO = "Informe um contato com até " + Profissional.TAMANHO_CONTATO
            + " caracteres";
    public static final String CLASSIFICACAO_OBRIGATORIA = "Informe ao menos uma classificação: professor, personal trainer ou ambos";
    public static final String CPF_DUPLICADO = "Já existe outro profissional cadastrado com este CPF";
    public static final String CREF_DUPLICADO = "Já existe outro profissional cadastrado com este CREF";
    public static final String ESPECIALIDADE_DUPLICADA = "Esta especialidade já está associada ao profissional";
    public static final String ESPECIALIDADE_INEXISTENTE = "Esta especialidade não está associada ao profissional";
    public static final String DISPONIBILIDADE_INCOMPLETA = "Informe o dia, o horário inicial e o horário final";
    public static final String HORARIO_INVERTIDO = "O horário inicial deve ser anterior ao horário final";
    public static final String DISPONIBILIDADE_SOBREPOSTA = "O período se sobrepõe a outra disponibilidade do profissional no mesmo dia";
    public static final String DISPONIBILIDADE_INEXISTENTE = "A disponibilidade informada não pertence ao profissional";
    public static final String CARGA_EXCEDIDA = "A carga horária semanal ultrapassaria o limite de "
            + ParametrosProfissionais.CARGA_HORARIA_SEMANAL_MAXIMA.toHours() + " horas";
    public static final String PROFISSIONAL_INATIVO = "Profissionais inativos não podem receber novas associações";
    public static final String PROFISSIONAL_JA_ATIVO = "O profissional já está ativo";
    public static final String VINCULOS_IMPEDEM_INATIVACAO = "Encerre os vínculos ativos com alunos antes de inativar o profissional";
    public static final String VINCULOS_IMPEDEM_CLASSIFICACAO = "Encerre os vínculos ativos com alunos antes de remover a classificação de personal trainer";

    private final IProfissionalRepositorio repositorio;
    private final IVinculoPersonalAlunoRepositorio vinculos;

    public ProfissionalServico(ISegurancaServico seguranca, IProfissionalRepositorio repositorio,
            IVinculoPersonalAlunoRepositorio vinculos) {
        super(seguranca, repositorio, IDENTIFICADOR_EXISTENTE_MODELO, IDENTIFICADOR_INEXISTENTE_MODELO);
        this.repositorio = Objects.requireNonNull(repositorio);
        this.vinculos = Objects.requireNonNull(vinculos);
    }

    @Override
    public ProfissionalAutorizacoes criarAutorizacoes(Optional<ProfissionalIdentificador> identificador) {
        Objects.requireNonNull(identificador);
        boolean administrador = getSegurancaServico().possuiAutorizacao(Autorizacao.ADMINISTRADOR.name());
        return new ProfissionalAutorizacoes(administrador, administrador);
    }

    public Profissional cadastrar(String nome, Cpf cpf, Cref cref, String contato,
            Set<ClassificacaoProfissional> classificacoes) {
        validarAutorizacao(Optional.empty(), ProfissionalAutorizacoes::getCadastrar);
        var profissional = Profissional.cadastrar(nome, cpf, cref, contato, classificacoes);
        validarDocumentosUnicos(profissional, Optional.empty());
        return repositorio.incluir(profissional);
    }

    public Profissional editar(ProfissionalIdentificador identificador, String nome, Cpf cpf, Cref cref, String contato,
            Set<ClassificacaoProfissional> classificacoes) {
        validarAcesso(identificador, ProfissionalAutorizacoes::getEditar);
        var editado = repositorio.bloquear(identificador).editar(nome, cpf, cref, contato, classificacoes,
                vinculos.existeAtivoDoProfissional(identificador));
        validarDocumentosUnicos(editado, Optional.of(identificador));
        return salvar(editado);
    }

    public Profissional adicionarEspecialidade(ProfissionalIdentificador identificador, Especialidade especialidade) {
        validarAcesso(identificador, ProfissionalAutorizacoes::getGerenciarEspecialidades);
        return salvar(repositorio.bloquear(identificador).adicionarEspecialidade(especialidade));
    }

    public Profissional removerEspecialidade(ProfissionalIdentificador identificador, Especialidade especialidade) {
        validarAcesso(identificador, ProfissionalAutorizacoes::getGerenciarEspecialidades);
        return salvar(repositorio.bloquear(identificador).removerEspecialidade(especialidade));
    }

    public Profissional adicionarDisponibilidade(ProfissionalIdentificador identificador, DayOfWeek dia, LocalTime inicio,
            LocalTime fim) {
        validarAcesso(identificador, ProfissionalAutorizacoes::getGerenciarDisponibilidades);
        return salvar(repositorio.bloquear(identificador).adicionarDisponibilidade(dia, inicio, fim));
    }

    public Profissional alterarDisponibilidade(ProfissionalIdentificador identificador, long disponibilidade, DayOfWeek dia,
            LocalTime inicio, LocalTime fim) {
        validarAcesso(identificador, ProfissionalAutorizacoes::getGerenciarDisponibilidades);
        return salvar(repositorio.bloquear(identificador).alterarDisponibilidade(disponibilidade, dia, inicio, fim));
    }

    public Profissional removerDisponibilidade(ProfissionalIdentificador identificador, long disponibilidade) {
        validarAcesso(identificador, ProfissionalAutorizacoes::getGerenciarDisponibilidades);
        return salvar(repositorio.bloquear(identificador).removerDisponibilidade(disponibilidade));
    }

    /** Não há exclusão definitiva: o profissional é inativado e o histórico permanece. */
    public Profissional inativar(ProfissionalIdentificador identificador) {
        validarAcesso(identificador, ProfissionalAutorizacoes::getInativar);
        var profissional = repositorio.bloquear(identificador);
        return salvar(profissional.inativar(vinculos.existeAtivoDoProfissional(identificador)));
    }

    public Profissional reativar(ProfissionalIdentificador identificador) {
        validarAcesso(identificador, ProfissionalAutorizacoes::getInativar);
        return salvar(repositorio.bloquear(identificador).reativar());
    }

    private void validarAcesso(ProfissionalIdentificador identificador,
            java.util.function.Predicate<ProfissionalAutorizacoes> autorizacao) {
        Objects.requireNonNull(identificador);
        validarAutorizacao(Optional.of(identificador), autorizacao);
        validarExistencia(identificador);
    }

    private Profissional salvar(Profissional profissional) {
        repositorio.alterar(profissional);
        return repositorio.buscarPorIdentificador(profissional.getIdentificador().orElseThrow());
    }

    private void validarDocumentosUnicos(Profissional profissional, Optional<ProfissionalIdentificador> ignorado) {
        Validate.validState(!repositorio.existePorCpf(profissional.getCpf(), ignorado), CPF_DUPLICADO);
        Validate.validState(!repositorio.existePorCref(profissional.getCref(), ignorado), CREF_DUPLICADO);
    }
}

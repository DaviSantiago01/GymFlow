package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.vinculopersonal;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.IAlunoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.ProfissionalIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.spi.IProfissionalRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.vinculopersonal.spi.IVinculoPersonalAlunoRepositorio;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoServicoAbstrato;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;

public final class VinculoPersonalAlunoServico extends
        AgregadoServicoAbstrato<VinculoPersonalAlunoIdentificador, VinculoPersonalAluno, VinculoPersonalAlunoAutorizacoes> {
    public static final String IDENTIFICADOR_EXISTENTE_MODELO = "Já existe um vínculo com o identificador %s";
    public static final String IDENTIFICADOR_INEXISTENTE_MODELO = "Não existe vínculo com o identificador %s";
    public static final String ALUNO_INEXISTENTE_MODELO = "Não existe aluno com o identificador %s";
    public static final String PROFISSIONAL_INEXISTENTE_MODELO = "Não existe profissional com o identificador %s";
    public static final String PROFISSIONAL_INATIVO = "Profissionais inativos não podem receber novos vínculos";
    public static final String SOMENTE_PERSONAL_TRAINER = "Somente profissionais classificados como personal trainer podem ser vinculados a alunos";
    public static final String VINCULO_DUPLICADO = "Já existe um vínculo ativo entre este personal trainer e o aluno";
    public static final String VINCULO_NAO_ATIVO = "Somente um vínculo ativo pode ser encerrado";

    private final IVinculoPersonalAlunoRepositorio repositorio;
    private final IProfissionalRepositorio profissionais;
    private final IAlunoRepositorio alunos;
    private final Clock relogio;

    public VinculoPersonalAlunoServico(ISegurancaServico seguranca, IVinculoPersonalAlunoRepositorio repositorio,
            IProfissionalRepositorio profissionais, IAlunoRepositorio alunos, Clock relogio) {
        super(seguranca, repositorio, IDENTIFICADOR_EXISTENTE_MODELO, IDENTIFICADOR_INEXISTENTE_MODELO);
        this.repositorio = Objects.requireNonNull(repositorio);
        this.profissionais = Objects.requireNonNull(profissionais);
        this.alunos = Objects.requireNonNull(alunos);
        this.relogio = Objects.requireNonNull(relogio);
    }

    @Override
    public VinculoPersonalAlunoAutorizacoes criarAutorizacoes(Optional<VinculoPersonalAlunoIdentificador> identificador) {
        Objects.requireNonNull(identificador);
        boolean administrador = getSegurancaServico().possuiAutorizacao(Autorizacao.ADMINISTRADOR.name());
        return new VinculoPersonalAlunoAutorizacoes(administrador, administrador, administrador);
    }

    /** O profissional é bloqueado para que a inativação e um novo vínculo não ocorram ao mesmo tempo. */
    public VinculoPersonalAluno vincular(ProfissionalIdentificador profissional, AlunoIdentificador aluno) {
        Objects.requireNonNull(profissional);
        Objects.requireNonNull(aluno);
        validarAutorizacao(Optional.empty(), VinculoPersonalAlunoAutorizacoes::getVincular);
        validarExistencia(profissionais::existePorIdentificador, profissional, PROFISSIONAL_INEXISTENTE_MODELO);
        validarExistencia(alunos::existePorIdentificador, aluno, ALUNO_INEXISTENTE_MODELO);
        var novo = VinculoPersonalAluno.vincular(profissionais.bloquear(profissional), aluno, hoje());
        Validate.validState(!repositorio.existeAtivo(profissional, aluno), VINCULO_DUPLICADO);
        return repositorio.incluir(novo);
    }

    public VinculoPersonalAluno encerrar(VinculoPersonalAlunoIdentificador identificador) {
        Objects.requireNonNull(identificador);
        validarAutorizacao(Optional.of(identificador), VinculoPersonalAlunoAutorizacoes::getEncerrar);
        validarExistencia(identificador);
        var encerrado = repositorio.bloquear(identificador).encerrar(hoje());
        repositorio.alterar(encerrado);
        return encerrado;
    }

    private LocalDate hoje() {
        return relogio.instant().atZone(ParametrosAcademia.FUSO).toLocalDate();
    }
}

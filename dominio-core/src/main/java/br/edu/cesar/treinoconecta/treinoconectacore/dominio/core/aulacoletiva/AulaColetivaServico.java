package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aulacoletiva;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.IAlunoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aulacoletiva.spi.IAulaColetivaRepositorio;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoServicoAbstrato;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;

public final class AulaColetivaServico
        extends AgregadoServicoAbstrato<AulaColetivaIdentificador, AulaColetiva, AulaColetivaAutorizacoes> {
    public static final String IDENTIFICADOR_EXISTENTE_MODELO = "Já existe uma aula coletiva com o identificador %s";
    public static final String IDENTIFICADOR_INEXISTENTE_MODELO = "Não existe aula coletiva com o identificador %s";
    public static final String SEM_VAGAS = "Esta aula não tem mais vagas";
    public static final String JA_INSCRITO = "Você já está inscrito nesta aula";
    public static final String AULA_INATIVA = "Esta aula não está mais na grade da academia";
    public static final String CONFLITO_MODELO = "Você já está inscrito em %s no mesmo horário";

    private final IAulaColetivaRepositorio repositorio;
    private final IAlunoRepositorio alunos;

    public AulaColetivaServico(ISegurancaServico seguranca, IAulaColetivaRepositorio repositorio, IAlunoRepositorio alunos) {
        super(seguranca, repositorio, IDENTIFICADOR_EXISTENTE_MODELO, IDENTIFICADOR_INEXISTENTE_MODELO);
        this.repositorio = Objects.requireNonNull(repositorio);
        this.alunos = Objects.requireNonNull(alunos);
    }

    @Override
    public AulaColetivaAutorizacoes criarAutorizacoes(Optional<AulaColetivaIdentificador> identificador) {
        Objects.requireNonNull(identificador);
        boolean aluno = getSegurancaServico().possuiAutorizacao(Autorizacao.USUARIO.name());
        boolean equipe = getSegurancaServico().possuiAutorizacao(Autorizacao.ADMINISTRADOR.name())
                || getSegurancaServico().possuiAutorizacao(Autorizacao.PERSONAL.name());
        return new AulaColetivaAutorizacoes(aluno || equipe, aluno);
    }

    /** RN006: a inscrição exige vaga e não pode conflitar com outra aula em que o aluno já está inscrito. */
    public AulaColetiva inscrever(AulaColetivaIdentificador identificador) {
        Objects.requireNonNull(identificador);
        validarAutorizacao(Optional.of(identificador), AulaColetivaAutorizacoes::getInscrever);
        validarExistencia(identificador);
        var aluno = alunoAutenticado();
        var aula = repositorio.bloquear(identificador);
        for (var inscrita : repositorio.buscarInscricoesDoAluno(aluno)) {
            Validate.validState(!aula.conflitaCom(inscrita), CONFLITO_MODELO, inscrita.getNome());
        }
        var atualizada = aula.inscrever(aluno);
        repositorio.alterar(atualizada);
        return atualizada;
    }

    public List<AulaColetiva> listar() {
        validarAutorizacao(Optional.empty(), AulaColetivaAutorizacoes::getBuscar);
        return repositorio.buscarAtivas();
    }

    public List<AulaColetiva> minhasInscricoes() {
        validarAutorizacao(Optional.empty(), AulaColetivaAutorizacoes::getInscrever);
        return repositorio.buscarInscricoesDoAluno(alunoAutenticado());
    }

    private AlunoIdentificador alunoAutenticado() {
        return alunos.buscarPorUsuario(getSegurancaServico().getUsuario()).getIdentificador().orElseThrow();
    }
}

package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.vinculopersonal;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.ProfissionalIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.vinculopersonal.VinculoPersonalAluno;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.vinculopersonal.VinculoPersonalAlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.vinculopersonal.VinculoPersonalAlunoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.vinculopersonal.spi.IVinculoPersonalAlunoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.IAlunoRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.profissional.IProfissionalRepositorioJpa;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoRepositorioAbstrato;

public final class VinculoPersonalAlunoRepositorio extends AgregadoRepositorioAbstrato<VinculoPersonalAlunoIdentificador,
        VinculoPersonalAluno, Long, VinculoPersonalAlunoJpa> implements IVinculoPersonalAlunoRepositorio {
    private final IVinculoPersonalAlunoRepositorioJpa repositorioJpa;
    private final IProfissionalRepositorioJpa profissionais;
    private final IAlunoRepositorioJpa alunos;

    public VinculoPersonalAlunoRepositorio(IVinculoPersonalAlunoRepositorioJpa repositorioJpa,
            IProfissionalRepositorioJpa profissionais, IAlunoRepositorioJpa alunos) {
        super(repositorioJpa, VinculoPersonalAlunoServico.IDENTIFICADOR_INEXISTENTE_MODELO);
        this.repositorioJpa = Objects.requireNonNull(repositorioJpa);
        this.profissionais = Objects.requireNonNull(profissionais);
        this.alunos = Objects.requireNonNull(alunos);
    }

    @Override
    protected Long paraIdentificadorJpa(VinculoPersonalAlunoIdentificador identificador) {
        return Long.valueOf(identificador.toString());
    }

    @Override
    protected VinculoPersonalAlunoJpa paraJpa(VinculoPersonalAluno agregado) {
        var identificador = agregado.getIdentificador().map(this::paraIdentificadorJpa).orElse(null);
        return new VinculoPersonalAlunoJpa(identificador,
                profissionais.getReferenceById(Long.valueOf(agregado.getProfissional().toString())),
                alunos.getReferenceById(Long.valueOf(agregado.getAluno().toString())), agregado.getInicio(),
                agregado.getEncerramento().orElse(null), agregado.getSituacao());
    }

    @Override
    protected VinculoPersonalAluno paraAgregado(VinculoPersonalAlunoJpa entidadeJpa) {
        return new VinculoPersonalAluno(
                Optional.of(new VinculoPersonalAlunoIdentificador(entidadeJpa.getIdentificador().toString())),
                new ProfissionalIdentificador(entidadeJpa.getProfissional().getIdentificador().toString()),
                new AlunoIdentificador(entidadeJpa.getAluno().getIdentificador().toString()), entidadeJpa.getInicio(),
                Optional.ofNullable(entidadeJpa.getEncerramento()), entidadeJpa.getSituacao());
    }

    /** O encerramento é gravado de imediato, antes de um eventual novo vínculo entre o mesmo par. */
    @Override
    public void alterar(VinculoPersonalAluno agregado) {
        Objects.requireNonNull(agregado);
        repositorioJpa.saveAndFlush(paraJpa(agregado));
    }

    @Override
    public VinculoPersonalAluno bloquear(VinculoPersonalAlunoIdentificador identificador) {
        Objects.requireNonNull(identificador);
        return paraAgregado(repositorioJpa.bloquear(paraIdentificadorJpa(identificador)).orElseThrow(
                () -> new NoSuchElementException(VinculoPersonalAlunoServico.IDENTIFICADOR_INEXISTENTE_MODELO.formatted(identificador))));
    }

    @Override
    public boolean existeAtivo(ProfissionalIdentificador profissional, AlunoIdentificador aluno) {
        Objects.requireNonNull(profissional);
        Objects.requireNonNull(aluno);
        return repositorioJpa.existeAtivo(Long.valueOf(profissional.toString()), Long.valueOf(aluno.toString()));
    }

    @Override
    public boolean existeAtivoDoProfissional(ProfissionalIdentificador profissional) {
        Objects.requireNonNull(profissional);
        return repositorioJpa.existeAtivoDoProfissional(Long.valueOf(profissional.toString()));
    }
}

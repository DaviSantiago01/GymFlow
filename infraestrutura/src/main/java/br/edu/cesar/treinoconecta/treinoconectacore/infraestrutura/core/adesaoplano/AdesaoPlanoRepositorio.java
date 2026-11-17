package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.adesaoplano;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.AdesaoPlano;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.AdesaoPlanoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.AdesaoPlanoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.spi.IAdesaoPlanoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.PlanoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.IAlunoRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.plano.IPlanoRepositorioJpa;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoRepositorioAbstrato;

public final class AdesaoPlanoRepositorio
        extends AgregadoRepositorioAbstrato<AdesaoPlanoIdentificador, AdesaoPlano, Long, AdesaoPlanoJpa>
        implements IAdesaoPlanoRepositorio {
    private final IAdesaoPlanoRepositorioJpa repositorioJpa;
    private final IAlunoRepositorioJpa alunos;
    private final IPlanoRepositorioJpa planos;

    public AdesaoPlanoRepositorio(IAdesaoPlanoRepositorioJpa repositorioJpa, IAlunoRepositorioJpa alunos,
            IPlanoRepositorioJpa planos) {
        super(repositorioJpa, AdesaoPlanoServico.IDENTIFICADOR_INEXISTENTE_MODELO);
        this.repositorioJpa = Objects.requireNonNull(repositorioJpa);
        this.alunos = Objects.requireNonNull(alunos);
        this.planos = Objects.requireNonNull(planos);
    }

    @Override
    protected Long paraIdentificadorJpa(AdesaoPlanoIdentificador identificador) {
        return Long.valueOf(identificador.toString());
    }

    @Override
    protected AdesaoPlanoJpa paraJpa(AdesaoPlano agregado) {
        var identificador = agregado.getIdentificador().map(this::paraIdentificadorJpa).orElse(null);
        return new AdesaoPlanoJpa(identificador, alunos.getReferenceById(Long.valueOf(agregado.getAluno().toString())),
                planos.getReferenceById(Long.valueOf(agregado.getPlano().toString())), agregado.getPeriodicidadeMeses(),
                agregado.getInicio(), agregado.getEncerramento().orElse(null), agregado.getSituacao());
    }

    @Override
    protected AdesaoPlano paraAgregado(AdesaoPlanoJpa entidadeJpa) {
        return new AdesaoPlano(Optional.of(new AdesaoPlanoIdentificador(entidadeJpa.getIdentificador().toString())),
                new AlunoIdentificador(entidadeJpa.getAluno().getIdentificador().toString()),
                new PlanoIdentificador(entidadeJpa.getPlano().getIdentificador().toString()),
                entidadeJpa.getPeriodicidadeMeses(), entidadeJpa.getInicio(),
                Optional.ofNullable(entidadeJpa.getEncerramento()), entidadeJpa.getSituacao());
    }

    /** Na troca, o encerramento é gravado antes da inclusão da nova adesão, respeitando a unicidade do aluno ativo. */
    @Override
    public void alterar(AdesaoPlano agregado) {
        Objects.requireNonNull(agregado);
        repositorioJpa.saveAndFlush(paraJpa(agregado));
    }

    @Override
    public AdesaoPlano bloquear(AdesaoPlanoIdentificador identificador) {
        Objects.requireNonNull(identificador);
        return paraAgregado(repositorioJpa.bloquear(paraIdentificadorJpa(identificador)).orElseThrow(
                () -> new NoSuchElementException(AdesaoPlanoServico.IDENTIFICADOR_INEXISTENTE_MODELO.formatted(identificador))));
    }

    @Override
    public Optional<AdesaoPlano> buscarAtivaDoAluno(AlunoIdentificador aluno) {
        Objects.requireNonNull(aluno);
        return repositorioJpa.buscarAtivaDoAluno(Long.valueOf(aluno.toString())).map(this::paraAgregado);
    }

    @Override
    public List<AdesaoPlano> buscarAtivas() {
        var ativas = new ArrayList<AdesaoPlano>();
        for (var entidade : repositorioJpa.buscarAtivas()) {
            ativas.add(paraAgregado(entidade));
        }
        return ativas;
    }
}

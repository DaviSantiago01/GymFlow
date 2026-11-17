package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.profissional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.ClassificacaoProfissional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.Cpf;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.Cref;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.DisponibilidadeProfissional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.Especialidade;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.Profissional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.ProfissionalIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.ProfissionalServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.spi.IProfissionalRepositorio;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoRepositorioAbstrato;

public final class ProfissionalRepositorio
        extends AgregadoRepositorioAbstrato<ProfissionalIdentificador, Profissional, Long, ProfissionalJpa>
        implements IProfissionalRepositorio {
    private final IProfissionalRepositorioJpa repositorioJpa;

    public ProfissionalRepositorio(IProfissionalRepositorioJpa repositorioJpa) {
        super(repositorioJpa, ProfissionalServico.IDENTIFICADOR_INEXISTENTE_MODELO);
        this.repositorioJpa = Objects.requireNonNull(repositorioJpa);
    }

    @Override
    protected Long paraIdentificadorJpa(ProfissionalIdentificador identificador) {
        return Long.valueOf(identificador.toString());
    }

    @Override
    protected ProfissionalJpa paraJpa(Profissional agregado) {
        var identificador = agregado.getIdentificador().map(this::paraIdentificadorJpa).orElse(null);
        var especialidades = new HashSet<EspecialidadeJpa>();
        for (var especialidade : agregado.getEspecialidades()) {
            especialidades.add(new EspecialidadeJpa(especialidade.chave(), especialidade.getNome()));
        }
        var disponibilidades = new ArrayList<DisponibilidadeProfissionalJpa>();
        for (var disponibilidade : agregado.getDisponibilidades()) {
            disponibilidades.add(new DisponibilidadeProfissionalJpa(disponibilidade.getIdentificador().orElse(null),
                    disponibilidade.getDia(), disponibilidade.getInicio(), disponibilidade.getFim()));
        }
        return new ProfissionalJpa(identificador, agregado.getNome(), agregado.getCpf().toString(),
                agregado.getCref().toString(), agregado.getContato(), agregado.professor(), agregado.personalTrainer(),
                agregado.getSituacao(), especialidades, disponibilidades);
    }

    @Override
    protected Profissional paraAgregado(ProfissionalJpa entidadeJpa) {
        var classificacoes = EnumSet.noneOf(ClassificacaoProfissional.class);
        if (entidadeJpa.isProfessor()) {
            classificacoes.add(ClassificacaoProfissional.PROFESSOR);
        }
        if (entidadeJpa.isPersonalTrainer()) {
            classificacoes.add(ClassificacaoProfissional.PERSONAL_TRAINER);
        }
        var especialidades = entidadeJpa.getEspecialidades().stream().map(item -> new Especialidade(item.getNome()))
                .sorted(Comparator.comparing(Especialidade::chave)).toList();
        var disponibilidades = entidadeJpa.getDisponibilidades().stream()
                .map(item -> new DisponibilidadeProfissional(Optional.ofNullable(item.getIdentificador()), item.getDia(),
                        item.getInicio(), item.getFim()))
                .sorted(Comparator.comparing(DisponibilidadeProfissional::getDia)
                        .thenComparing(DisponibilidadeProfissional::getInicio))
                .toList();
        return new Profissional(Optional.of(new ProfissionalIdentificador(entidadeJpa.getIdentificador().toString())),
                entidadeJpa.getNome(), new Cpf(entidadeJpa.getCpf()), new Cref(entidadeJpa.getCref()),
                entidadeJpa.getContato(), classificacoes, especialidades, disponibilidades, entidadeJpa.getSituacao());
    }

    /** Grava de imediato para que a releitura devolva as disponibilidades com os identificadores gerados. */
    @Override
    public void alterar(Profissional agregado) {
        Objects.requireNonNull(agregado);
        repositorioJpa.saveAndFlush(paraJpa(agregado));
    }

    @Override
    public Profissional bloquear(ProfissionalIdentificador identificador) {
        Objects.requireNonNull(identificador);
        return paraAgregado(repositorioJpa.bloquear(paraIdentificadorJpa(identificador)).orElseThrow(
                () -> new NoSuchElementException(ProfissionalServico.IDENTIFICADOR_INEXISTENTE_MODELO.formatted(identificador))));
    }

    @Override
    public boolean existePorCpf(Cpf cpf, Optional<ProfissionalIdentificador> identificadorIgnorado) {
        Objects.requireNonNull(cpf);
        return repositorioJpa.existePorCpf(cpf.toString(), identificadorIgnorado.map(this::paraIdentificadorJpa).orElse(null));
    }

    @Override
    public boolean existePorCref(Cref cref, Optional<ProfissionalIdentificador> identificadorIgnorado) {
        Objects.requireNonNull(cref);
        return repositorioJpa.existePorCref(cref.toString(), identificadorIgnorado.map(this::paraIdentificadorJpa).orElse(null));
    }
}

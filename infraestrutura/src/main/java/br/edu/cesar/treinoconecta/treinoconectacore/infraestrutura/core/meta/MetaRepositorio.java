package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.meta;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.AvaliacaoFisicaIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.IndicadorFisico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta.Meta;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta.MetaIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta.MetaServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta.spi.IMetaRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.PersonalIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.IAlunoRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.avaliacaofisica.AvaliacaoFisicaJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.avaliacaofisica.IAvaliacaoFisicaRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.personal.IPersonalRepositorioJpa;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoRepositorioAbstrato;

public final class MetaRepositorio extends AgregadoRepositorioAbstrato<MetaIdentificador, Meta, Long, MetaJpa>
        implements IMetaRepositorio {
    private final IMetaRepositorioJpa repositorioJpa;
    private final IAlunoRepositorioJpa alunos;
    private final IPersonalRepositorioJpa personais;
    private final IAvaliacaoFisicaRepositorioJpa avaliacoes;

    public MetaRepositorio(IMetaRepositorioJpa repositorioJpa, IAlunoRepositorioJpa alunos, IPersonalRepositorioJpa personais,
            IAvaliacaoFisicaRepositorioJpa avaliacoes) {
        super(repositorioJpa, MetaServico.IDENTIFICADOR_INEXISTENTE_MODELO);
        this.repositorioJpa = Objects.requireNonNull(repositorioJpa);
        this.alunos = Objects.requireNonNull(alunos);
        this.personais = Objects.requireNonNull(personais);
        this.avaliacoes = Objects.requireNonNull(avaliacoes);
    }

    @Override
    protected Long paraIdentificadorJpa(MetaIdentificador identificador) {
        return Long.valueOf(identificador.toString());
    }

    @Override
    protected MetaJpa paraJpa(Meta agregado) {
        var identificador = agregado.getIdentificador().map(this::paraIdentificadorJpa).orElse(null);
        var aluno = alunos.getReferenceById(Long.valueOf(agregado.getAluno().toString()));
        var personal = personais.getReferenceById(Long.valueOf(agregado.getPersonal().toString()));
        return new MetaJpa(identificador, aluno, personal, agregado.getIndicador(), agregado.getValorInicial(),
                agregado.getValorDesejado(), agregado.getInicio(), agregado.getFim(),
                referenciaDaAvaliacao(agregado.getAvaliacaoReferencia()), agregado.getSituacao(),
                agregado.getDataAtingida().orElse(null));
    }

    private AvaliacaoFisicaJpa referenciaDaAvaliacao(AvaliacaoFisicaIdentificador avaliacao) {
        return avaliacoes.getReferenceById(Long.valueOf(avaliacao.toString()));
    }

    @Override
    protected Meta paraAgregado(MetaJpa entidadeJpa) {
        return new Meta(Optional.of(new MetaIdentificador(entidadeJpa.getIdentificador().toString())),
                new AlunoIdentificador(entidadeJpa.getAluno().getIdentificador().toString()),
                new PersonalIdentificador(entidadeJpa.getPersonal().getIdentificador().toString()), entidadeJpa.getIndicador(),
                entidadeJpa.getValorInicial(), entidadeJpa.getValorDesejado(), entidadeJpa.getInicio(), entidadeJpa.getFim(),
                new AvaliacaoFisicaIdentificador(entidadeJpa.getAvaliacaoReferencia().getIdentificador().toString()),
                entidadeJpa.getSituacao(), Optional.ofNullable(entidadeJpa.getDataAtingida()));
    }

    @Override
    public Meta bloquear(MetaIdentificador identificador) {
        Objects.requireNonNull(identificador);
        var meta = repositorioJpa.bloquear(paraIdentificadorJpa(identificador)).orElseThrow(() -> new NoSuchElementException(
                MetaServico.IDENTIFICADOR_INEXISTENTE_MODELO.formatted(identificador)));
        return paraAgregado(meta);
    }

    @Override
    public boolean existeAtivaNoPeriodo(AlunoIdentificador aluno, IndicadorFisico indicador, LocalDate inicio, LocalDate fim,
            Optional<MetaIdentificador> identificadorIgnorado) {
        Objects.requireNonNull(aluno);
        Objects.requireNonNull(identificadorIgnorado);
        var ignorada = identificadorIgnorado.map(this::paraIdentificadorJpa).orElse(null);
        return repositorioJpa.existeAtivaNoPeriodo(Long.valueOf(aluno.toString()), indicador, inicio, fim, ignorada);
    }

    @Override
    public List<Meta> buscarAtivasPorAluno(AlunoIdentificador aluno) {
        Objects.requireNonNull(aluno);
        var ativas = new ArrayList<Meta>();
        for (var entidade : repositorioJpa.buscarAtivasPorAluno(Long.valueOf(aluno.toString()))) {
            ativas.add(paraAgregado(entidade));
        }
        return ativas;
    }

    @Override
    public boolean existePorAvaliacaoReferencia(AvaliacaoFisicaIdentificador avaliacao) {
        Objects.requireNonNull(avaliacao);
        return repositorioJpa.existePorAvaliacaoReferencia(Long.valueOf(avaliacao.toString()));
    }

    @Override
    public void transferirReferencia(AvaliacaoFisicaIdentificador antiga, AvaliacaoFisicaIdentificador nova) {
        Objects.requireNonNull(antiga);
        Objects.requireNonNull(nova);
        repositorioJpa.transferirReferencia(referenciaDaAvaliacao(antiga), referenciaDaAvaliacao(nova));
    }
}

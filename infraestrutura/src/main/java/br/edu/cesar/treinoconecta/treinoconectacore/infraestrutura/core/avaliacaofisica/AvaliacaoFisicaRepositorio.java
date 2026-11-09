package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.avaliacaofisica;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.AvaliacaoFisica;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.AvaliacaoFisicaIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.AvaliacaoFisicaServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.MedidaCorporal;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.spi.IAvaliacaoFisicaRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.PersonalIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.IAlunoRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.personal.IPersonalRepositorioJpa;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoRepositorioAbstrato;

public final class AvaliacaoFisicaRepositorio
        extends AgregadoRepositorioAbstrato<AvaliacaoFisicaIdentificador, AvaliacaoFisica, Long, AvaliacaoFisicaJpa>
        implements IAvaliacaoFisicaRepositorio {
    private final IAvaliacaoFisicaRepositorioJpa repositorioJpa;
    private final IAlunoRepositorioJpa alunos;
    private final IPersonalRepositorioJpa personais;

    public AvaliacaoFisicaRepositorio(IAvaliacaoFisicaRepositorioJpa repositorioJpa, IAlunoRepositorioJpa alunos,
            IPersonalRepositorioJpa personais) {
        super(repositorioJpa, AvaliacaoFisicaServico.IDENTIFICADOR_INEXISTENTE_MODELO);
        this.repositorioJpa = Objects.requireNonNull(repositorioJpa);
        this.alunos = Objects.requireNonNull(alunos);
        this.personais = Objects.requireNonNull(personais);
    }

    @Override
    protected Long paraIdentificadorJpa(AvaliacaoFisicaIdentificador identificador) {
        return Long.valueOf(identificador.toString());
    }

    @Override
    protected AvaliacaoFisicaJpa paraJpa(AvaliacaoFisica agregado) {
        var identificador = agregado.getIdentificador().map(this::paraIdentificadorJpa).orElse(null);
        var aluno = alunos.getReferenceById(Long.valueOf(agregado.getAluno().toString()));
        var personal = personais.getReferenceById(Long.valueOf(agregado.getPersonal().toString()));
        var anterior = agregado.getAvaliacaoAnterior().map(this::paraIdentificadorJpa).map(repositorioJpa::getReferenceById)
                .orElse(null);
        var medidas = new ArrayList<MedidaCorporalJpa>();
        for (var medida : agregado.getMedidas()) {
            medidas.add(new MedidaCorporalJpa(medida.indicador(), medida.valor()));
        }
        return new AvaliacaoFisicaJpa(identificador, aluno, personal, agregado.getData(), agregado.getPeso(),
                agregado.getAltura(), agregado.getImc(), agregado.getSituacao(), agregado.getVersao(), anterior,
                agregado.getRegistro(), medidas);
    }

    @Override
    protected AvaliacaoFisica paraAgregado(AvaliacaoFisicaJpa entidadeJpa) {
        var identificador = new AvaliacaoFisicaIdentificador(entidadeJpa.getIdentificador().toString());
        var aluno = new AlunoIdentificador(entidadeJpa.getAluno().getIdentificador().toString());
        var personal = new PersonalIdentificador(entidadeJpa.getPersonal().getIdentificador().toString());
        Optional<AvaliacaoFisicaIdentificador> anterior = Optional.empty();
        if (entidadeJpa.getAvaliacaoAnterior() != null) {
            anterior = Optional.of(new AvaliacaoFisicaIdentificador(entidadeJpa.getAvaliacaoAnterior().getIdentificador().toString()));
        }
        var medidas = new ArrayList<MedidaCorporal>();
        for (var medida : entidadeJpa.getMedidas()) {
            medidas.add(new MedidaCorporal(medida.getIndicador(), medida.getValor()));
        }
        return new AvaliacaoFisica(Optional.of(identificador), aluno, personal, entidadeJpa.getData(), entidadeJpa.getPeso(),
                entidadeJpa.getAltura(), medidas, entidadeJpa.getSituacao(), entidadeJpa.getVersao(), anterior,
                entidadeJpa.getRegistro());
    }

    /** Grava na hora para liberar a data da avaliação antes de incluir a versão corrigida. */
    @Override
    public void alterar(AvaliacaoFisica agregado) {
        Objects.requireNonNull(agregado);
        var identificador = paraIdentificadorJpa(agregado.getIdentificador().orElseThrow());
        var avaliacao = repositorioJpa.findById(identificador).orElseThrow(() -> inexistente(identificador));
        avaliacao.atualizarSituacao(agregado.getSituacao());
        repositorioJpa.saveAndFlush(avaliacao);
    }

    @Override
    public AvaliacaoFisica bloquear(AvaliacaoFisicaIdentificador identificador) {
        Objects.requireNonNull(identificador);
        var codigo = paraIdentificadorJpa(identificador);
        return paraAgregado(repositorioJpa.bloquear(codigo).orElseThrow(() -> inexistente(codigo)));
    }

    @Override
    public List<AvaliacaoFisica> buscarValidasPorAluno(AlunoIdentificador aluno) {
        Objects.requireNonNull(aluno);
        var encontradas = new ArrayList<AvaliacaoFisica>();
        for (var entidade : repositorioJpa.buscarValidasPorAluno(Long.valueOf(aluno.toString()))) {
            encontradas.add(paraAgregado(entidade));
        }
        return encontradas;
    }

    private static NoSuchElementException inexistente(Long identificador) {
        return new NoSuchElementException(AvaliacaoFisicaServico.IDENTIFICADOR_INEXISTENTE_MODELO.formatted(identificador));
    }
}

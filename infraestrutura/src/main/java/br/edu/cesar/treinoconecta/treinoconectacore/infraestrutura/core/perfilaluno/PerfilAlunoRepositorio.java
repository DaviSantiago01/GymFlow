package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.perfilaluno;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;
import java.util.stream.Collectors;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.Disponibilidade;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.PerfilAluno;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.PerfilAlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.PerfilAlunoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.RestricaoFisica;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.spi.IPerfilAlunoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.DiaSemana;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.CategoriaRecomendacao;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoRepositorioAbstrato;

public final class PerfilAlunoRepositorio
        extends AgregadoRepositorioAbstrato<PerfilAlunoIdentificador, PerfilAluno, Long, PerfilAlunoJpa>
        implements IPerfilAlunoRepositorio {
    public PerfilAlunoRepositorio(IPerfilAlunoRepositorioJpa repositorioJpa) {
        super(repositorioJpa, PerfilAlunoServico.IDENTIFICADOR_INEXISTENTE_MODELO);
    }

    @Override
    protected Long paraIdentificadorJpa(PerfilAlunoIdentificador identificador) {
        return Long.valueOf(identificador.toString());
    }

    @Override
    protected PerfilAlunoJpa paraJpa(PerfilAluno agregado) {
        var restricoes = new ArrayList<RestricaoFisicaJpa>();
        for (var restricao : agregado.getRestricoes()) {
            restricoes.add(new RestricaoFisicaJpa(restricao.regiao(), restricao.descricao(), restricao.registro()));
        }
        var disponibilidade = agregado.getDisponibilidade();
        var dias = disponibilidade.map(valor -> EnumSet.copyOf(valor.dias()).stream().map(Enum::name)
                .collect(Collectors.joining(","))).orElse(null);
        var categorias = EnumSet.noneOf(CategoriaRecomendacao.class);
        categorias.addAll(agregado.getCategoriasDesativadas());
        return new PerfilAlunoJpa(Long.valueOf(agregado.getAluno().toString()), agregado.getObjetivo().orElse(null),
                agregado.getNivel().orElse(null), dias, disponibilidade.map(Disponibilidade::inicio).orElse(null),
                disponibilidade.map(Disponibilidade::fim).orElse(null),
                categorias.stream().map(Enum::name).collect(Collectors.joining(",")), agregado.getAtualizacao(),
                restricoes);
    }

    @Override
    protected PerfilAluno paraAgregado(PerfilAlunoJpa entidade) {
        Optional<Disponibilidade> disponibilidade = Optional.empty();
        var dias = entidade.getDias();
        var inicio = entidade.getHoraInicio();
        var fim = entidade.getHoraFim();
        if (dias != null && !dias.isBlank() && inicio != null && fim != null) {
            var conjunto = EnumSet.noneOf(DiaSemana.class);
            Arrays.stream(dias.split(",")).map(DiaSemana::valueOf).forEach(conjunto::add);
            disponibilidade = Optional.of(new Disponibilidade(conjunto, inicio, fim));
        }
        var categorias = EnumSet.noneOf(CategoriaRecomendacao.class);
        Arrays.stream(entidade.getCategoriasDesativadas().split(",")).filter(valor -> !valor.isBlank())
                .map(CategoriaRecomendacao::valueOf).forEach(categorias::add);
        var restricoes = new ArrayList<RestricaoFisica>();
        for (var restricao : entidade.getRestricoes()) {
            restricoes.add(new RestricaoFisica(restricao.getRegiao(), restricao.getDescricao(), restricao.getRegistro()));
        }
        return new PerfilAluno(new AlunoIdentificador(entidade.getAluno().toString()),
                Optional.ofNullable(entidade.getObjetivo()), Optional.ofNullable(entidade.getNivel()), disponibilidade,
                restricoes, categorias, entidade.getAtualizacao());
    }
}

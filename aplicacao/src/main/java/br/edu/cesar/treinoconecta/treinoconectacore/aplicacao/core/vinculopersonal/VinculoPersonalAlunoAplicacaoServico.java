package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.vinculopersonal;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.vinculopersonal.spi.IVinculoPersonalAlunoAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.ProfissionalIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.vinculopersonal.VinculoPersonalAluno;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.vinculopersonal.VinculoPersonalAlunoAutorizacoes;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.vinculopersonal.VinculoPersonalAlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.vinculopersonal.VinculoPersonalAlunoServico;
import dev.sauloaraujo.d5.framework.aplicacao.impl.AgregadoAplicacaoServicoAbstrato;
import dev.sauloaraujo.d5.framework.aplicacao.impl.Consulta;

public final class VinculoPersonalAlunoAplicacaoServico extends AgregadoAplicacaoServicoAbstrato<
        VinculoPersonalAlunoIdentificador, VinculoPersonalAluno, VinculoPersonalAlunoAutorizacoes,
        VinculoPersonalAlunoProjecao, VinculoPersonalAlunoConsulta> {
    private final IVinculoPersonalAlunoAplicacaoRepositorio repositorio;

    public VinculoPersonalAlunoAplicacaoServico(IVinculoPersonalAlunoAplicacaoRepositorio repositorio,
            VinculoPersonalAlunoServico servico) {
        super(repositorio, servico);
        this.repositorio = Objects.requireNonNull(repositorio);
    }

    /** Ativos primeiro; os encerrados permanecem como histórico. */
    public List<VinculoPersonalAlunoProjecao> listarDoProfissional(ProfissionalIdentificador profissional) {
        var consulta = new VinculoPersonalAlunoConsulta(Optional.of(VinculoPersonalAlunoOrdenacao.INICIO), Optional.of(true),
                Optional.empty(), Optional.of(Consulta.TAMANHO_PAGINA_MAXIMO), Optional.of(profissional.toString()),
                Optional.empty(), Optional.empty());
        return pesquisar(consulta).getProjecoes();
    }

    public VinculoPersonalAlunoProjecao projetar(VinculoPersonalAlunoIdentificador identificador) {
        return repositorio.projetar(identificador);
    }
}

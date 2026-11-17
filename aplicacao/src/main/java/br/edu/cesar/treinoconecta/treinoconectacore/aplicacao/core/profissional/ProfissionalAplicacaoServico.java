package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.profissional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.AlunoConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.AlunoOrdenacao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.AlunoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.spi.IAlunoAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.profissional.spi.IProfissionalAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.vinculopersonal.VinculoPersonalAlunoAplicacaoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.ParametrosProfissionais;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.Profissional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.ProfissionalAutorizacoes;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.ProfissionalIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.ProfissionalServico;
import dev.sauloaraujo.d5.framework.aplicacao.impl.AgregadoAplicacaoServicoAbstrato;
import dev.sauloaraujo.d5.framework.aplicacao.impl.Consulta;

public final class ProfissionalAplicacaoServico extends AgregadoAplicacaoServicoAbstrato<ProfissionalIdentificador,
        Profissional, ProfissionalAutorizacoes, ProfissionalProjecao, ProfissionalConsulta> {
    private final ProfissionalServico servico;
    private final VinculoPersonalAlunoAplicacaoServico vinculos;
    private final IAlunoAplicacaoRepositorio alunos;

    public ProfissionalAplicacaoServico(IProfissionalAplicacaoRepositorio repositorio, ProfissionalServico servico,
            VinculoPersonalAlunoAplicacaoServico vinculos, IAlunoAplicacaoRepositorio alunos) {
        super(repositorio, servico);
        this.servico = Objects.requireNonNull(servico);
        this.vinculos = Objects.requireNonNull(vinculos);
        this.alunos = Objects.requireNonNull(alunos);
    }

    public ProfissionalDetalhe detalhe(ProfissionalIdentificador identificador) {
        var profissional = servico.buscar(identificador);
        List<AlunoProjecao> opcoes = List.of();
        if (profissional.ativo() && profissional.personalTrainer()) {
            var consulta = new AlunoConsulta(Optional.of(AlunoOrdenacao.NOME), Optional.empty(), Optional.empty(),
                    Optional.of(Consulta.TAMANHO_PAGINA_MAXIMO), Optional.empty());
            opcoes = alunos.pesquisar(consulta).getProjecoes();
        }
        return new ProfissionalDetalhe(profissional, profissional.getCpf().formatado(),
                profissional.cargaHorariaSemanal().toMinutes(),
                ParametrosProfissionais.CARGA_HORARIA_SEMANAL_MAXIMA.toMinutes(), vinculos.listarDoProfissional(identificador),
                opcoes);
    }
}

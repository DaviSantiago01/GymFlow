package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.planodetreino;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.AlunoAplicacaoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.AlunoConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.AlunoOrdenacao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.exercicio.ExercicioAplicacaoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.exercicio.ExercicioConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.exercicio.ExercicioOrdenacao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.planodetreino.spi.IPlanoDeTreinoAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.DiaSemana;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.ObjetivoTreino;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreino;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreinoAutorizacoes;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreinoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreinoServico;
import dev.sauloaraujo.d5.framework.aplicacao.impl.AgregadoAplicacaoServicoAbstrato;
import dev.sauloaraujo.d5.framework.aplicacao.impl.Consulta;

public final class PlanoDeTreinoAplicacaoServico extends
        AgregadoAplicacaoServicoAbstrato<PlanoDeTreinoIdentificador, PlanoDeTreino, PlanoDeTreinoAutorizacoes, PlanoDeTreinoProjecao, PlanoDeTreinoConsulta> {
    private final IPlanoDeTreinoAplicacaoRepositorio repositorio;
    private final PlanoDeTreinoServico servico;
    private final AlunoAplicacaoServico alunos;
    private final ExercicioAplicacaoServico exercicios;

    public PlanoDeTreinoAplicacaoServico(IPlanoDeTreinoAplicacaoRepositorio repositorio, PlanoDeTreinoServico servico,
            AlunoAplicacaoServico alunos, ExercicioAplicacaoServico exercicios) {
        super(repositorio, servico);
        this.repositorio = Objects.requireNonNull(repositorio);
        this.servico = Objects.requireNonNull(servico);
        this.alunos = Objects.requireNonNull(alunos);
        this.exercicios = Objects.requireNonNull(exercicios);
    }

    public PlanoDeTreinoDetalhe detalhe(PlanoDeTreinoIdentificador identificador) {
        var plano = servico.buscar(identificador);
        return repositorio.detalhe(identificador, servico.podeGerenciar(plano));
    }

    public PlanoDeTreinoFormulario formulario() {
        var consultaAlunos = new AlunoConsulta(Optional.of(AlunoOrdenacao.NOME), Optional.empty(), Optional.empty(),
                Optional.of(Consulta.TAMANHO_PAGINA_MAXIMO), Optional.empty());
        var consultaExercicios = new ExercicioConsulta(Optional.of(ExercicioOrdenacao.NOME), Optional.empty(),
                Optional.empty(), Optional.of(Consulta.TAMANHO_PAGINA_MAXIMO), Optional.empty());
        return new PlanoDeTreinoFormulario(alunos.pesquisar(consultaAlunos).getProjecoes(),
                exercicios.pesquisar(consultaExercicios).getProjecoes(), faixasDosObjetivos(), List.of(DiaSemana.values()),
                ParametrosAcademia.INTERVALO_MINIMO_SEGUNDOS, ParametrosAcademia.INTERVALO_MAXIMO_SEGUNDOS);
    }

    private static List<FaixaObjetivo> faixasDosObjetivos() {
        var faixas = new ArrayList<FaixaObjetivo>();
        for (var objetivo : ObjetivoTreino.values()) {
            faixas.add(new FaixaObjetivo(objetivo, objetivo.getSeriesMinimas(), objetivo.getSeriesMaximas(),
                    objetivo.getRepeticoesMinimas(), objetivo.getRepeticoesMaximas()));
        }
        return faixas;
    }
}

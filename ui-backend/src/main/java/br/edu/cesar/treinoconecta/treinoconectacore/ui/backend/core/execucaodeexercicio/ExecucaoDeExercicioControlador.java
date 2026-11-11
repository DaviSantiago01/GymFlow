package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.execucaodeexercicio;

import java.util.Objects;
import java.util.Optional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.execucaodeexercicio.EvolucaoExercicio;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.execucaodeexercicio.ExecucaoDeExercicioAplicacaoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.execucaodeexercicio.ExecucaoDeExercicioConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.execucaodeexercicio.ExecucaoDeExercicioProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.execucaodeexercicio.ExecucaoRegistrada;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.planodetreino.PlanoDeTreinoDetalhe;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.ExercicioIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.execucaodeexercicio.ExecucaoDeExercicio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.execucaodeexercicio.ExecucaoDeExercicioAutorizacoes;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.execucaodeexercicio.ExecucaoDeExercicioIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.execucaodeexercicio.ExecucaoDeExercicioServico;
import dev.sauloaraujo.d5.framework.ui.AgregadoControladorAbstrato;
import dev.sauloaraujo.d5.framework.ui.Backend;

@RestController
@RequestMapping(Backend.PREFIXO + "core/execucao-de-exercicio")
public class ExecucaoDeExercicioControlador extends
        AgregadoControladorAbstrato<ExecucaoDeExercicioIdentificador, ExecucaoDeExercicio, ExecucaoDeExercicioAutorizacoes, ExecucaoDeExercicioProjecao, ExecucaoDeExercicioConsulta> {
    private final ExecucaoDeExercicioServico servico;
    private final ExecucaoDeExercicioAplicacaoServico consultas;

    public ExecucaoDeExercicioControlador(ExecucaoDeExercicioServico servico,
            ExecucaoDeExercicioAplicacaoServico consultas) {
        super(servico, consultas);
        this.servico = Objects.requireNonNull(servico);
        this.consultas = Objects.requireNonNull(consultas);
    }

    @Override
    public PlanoDeTreinoDetalhe inclusao() {
        return consultas.planoAtivo();
    }

    @PostMapping("registrar")
    public ExecucaoRegistrada registrar(@RequestBody ExecucaoRequisicao requisicao) {
        var execucao = servico.registrar(requisicao.plano(), requisicao.dia(), requisicao.exercicio(), requisicao.series(),
                requisicao.repeticoes(), requisicao.carga());
        return consultas.resultado(execucao);
    }

    @GetMapping("evolucao")
    public EvolucaoExercicio evolucao(@RequestParam Optional<AlunoIdentificador> aluno,
            @RequestParam Optional<ExercicioIdentificador> exercicio) {
        return consultas.evolucao(aluno, exercicio);
    }
}

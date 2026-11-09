package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.exercicio;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.exercicio.ExercicioAplicacaoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.exercicio.ExercicioConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.exercicio.ExercicioProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.Exercicio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.ExercicioIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.ExercicioServico;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;
import dev.sauloaraujo.d5.framework.ui.AgregadoControladorAbstrato;
import dev.sauloaraujo.d5.framework.ui.Backend;

@RestController
@RequestMapping(Backend.PREFIXO + "core/exercicio")
public class ExercicioControlador extends
        AgregadoControladorAbstrato<ExercicioIdentificador, Exercicio, AgregadoAutorizacoes<Exercicio>, ExercicioProjecao, ExercicioConsulta> {

    public ExercicioControlador(ExercicioServico servico, ExercicioAplicacaoServico servicoAplicacao) {
        super(servico, servicoAplicacao);
    }
}

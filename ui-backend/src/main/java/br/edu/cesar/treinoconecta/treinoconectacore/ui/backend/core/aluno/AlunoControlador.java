package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.aluno;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.AlunoAplicacaoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.AlunoConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.AlunoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.Aluno;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoServico;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;
import dev.sauloaraujo.d5.framework.ui.AgregadoControladorAbstrato;
import dev.sauloaraujo.d5.framework.ui.Backend;

@RestController
@RequestMapping(Backend.PREFIXO + "core/aluno")
public class AlunoControlador extends
        AgregadoControladorAbstrato<AlunoIdentificador, Aluno, AgregadoAutorizacoes<Aluno>, AlunoProjecao, AlunoConsulta> {

    public AlunoControlador(AlunoServico servico, AlunoAplicacaoServico servicoAplicacao) {
        super(servico, servicoAplicacao);
    }
}

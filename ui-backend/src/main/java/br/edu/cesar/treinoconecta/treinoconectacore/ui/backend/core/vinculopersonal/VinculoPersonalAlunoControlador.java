package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.vinculopersonal;

import java.util.Objects;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.vinculopersonal.VinculoPersonalAlunoAplicacaoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.vinculopersonal.VinculoPersonalAlunoConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.vinculopersonal.VinculoPersonalAlunoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.vinculopersonal.VinculoPersonalAluno;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.vinculopersonal.VinculoPersonalAlunoAutorizacoes;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.vinculopersonal.VinculoPersonalAlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.vinculopersonal.VinculoPersonalAlunoServico;
import dev.sauloaraujo.d5.framework.ui.AgregadoControladorAbstrato;
import dev.sauloaraujo.d5.framework.ui.Backend;

@RestController
@RequestMapping(Backend.PREFIXO + "core/vinculo-personal-aluno")
public class VinculoPersonalAlunoControlador extends AgregadoControladorAbstrato<VinculoPersonalAlunoIdentificador,
        VinculoPersonalAluno, VinculoPersonalAlunoAutorizacoes, VinculoPersonalAlunoProjecao, VinculoPersonalAlunoConsulta> {
    private final VinculoPersonalAlunoServico servico;
    private final VinculoPersonalAlunoAplicacaoServico consultas;

    public VinculoPersonalAlunoControlador(VinculoPersonalAlunoServico servico,
            VinculoPersonalAlunoAplicacaoServico consultas) {
        super(servico, consultas);
        this.servico = Objects.requireNonNull(servico);
        this.consultas = Objects.requireNonNull(consultas);
    }

    @PostMapping("vincular")
    public VinculoPersonalAlunoProjecao vincular(@RequestBody VinculoRequisicao requisicao) {
        var vinculo = servico.vincular(requisicao.profissional(), requisicao.aluno());
        return consultas.projetar(vinculo.getIdentificador().orElseThrow());
    }

    @PostMapping("{identificador}/encerrar")
    public VinculoPersonalAlunoProjecao encerrar(@PathVariable VinculoPersonalAlunoIdentificador identificador) {
        servico.encerrar(identificador);
        return consultas.projetar(identificador);
    }
}

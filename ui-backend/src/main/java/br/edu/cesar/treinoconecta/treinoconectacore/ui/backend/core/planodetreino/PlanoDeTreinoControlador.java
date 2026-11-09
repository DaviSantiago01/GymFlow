package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.planodetreino;

import java.util.Objects;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.planodetreino.PlanoDeTreinoAplicacaoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.planodetreino.PlanoDeTreinoConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.planodetreino.PlanoDeTreinoDetalhe;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.planodetreino.PlanoDeTreinoFormulario;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.planodetreino.PlanoDeTreinoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreino;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreinoAutorizacoes;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreinoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreinoServico;
import dev.sauloaraujo.d5.framework.ui.AgregadoControladorAbstrato;
import dev.sauloaraujo.d5.framework.ui.Backend;

@RestController
@RequestMapping(Backend.PREFIXO + "core/plano-de-treino")
public class PlanoDeTreinoControlador extends
        AgregadoControladorAbstrato<PlanoDeTreinoIdentificador, PlanoDeTreino, PlanoDeTreinoAutorizacoes, PlanoDeTreinoProjecao, PlanoDeTreinoConsulta> {
    private final PlanoDeTreinoServico servico;
    private final PlanoDeTreinoAplicacaoServico consultas;

    public PlanoDeTreinoControlador(PlanoDeTreinoServico servico, PlanoDeTreinoAplicacaoServico consultas) {
        super(servico, consultas);
        this.servico = Objects.requireNonNull(servico);
        this.consultas = Objects.requireNonNull(consultas);
    }

    @Override
    public PlanoDeTreinoFormulario inclusao() {
        return consultas.formulario();
    }

    @GetMapping("{identificador}/visualizacao")
    public PlanoDeTreinoDetalhe visualizacao(@PathVariable PlanoDeTreinoIdentificador identificador) {
        return consultas.detalhe(identificador);
    }

    @PostMapping("criar")
    public PlanoDeTreinoDetalhe criar(@RequestBody PlanoDeTreinoRequisicao requisicao) {
        var plano = servico.criar(requisicao.aluno(), requisicao.nome(), requisicao.frequenciaSemanal(), requisicao.treinos());
        return consultas.detalhe(plano.getIdentificador().orElseThrow());
    }

    @PostMapping("{identificador}/ativar")
    public PlanoDeTreinoDetalhe ativar(@PathVariable PlanoDeTreinoIdentificador identificador) {
        servico.ativar(identificador);
        return consultas.detalhe(identificador);
    }

    @PostMapping("{identificador}/substituir")
    public PlanoDeTreinoDetalhe substituir(@PathVariable PlanoDeTreinoIdentificador identificador) {
        servico.substituir(identificador);
        return consultas.detalhe(identificador);
    }

    @PostMapping("{identificador}/revisar")
    public PlanoDeTreinoDetalhe revisar(@PathVariable PlanoDeTreinoIdentificador identificador,
            @RequestBody RevisaoPlanoRequisicao requisicao) {
        var plano = servico.revisar(identificador, requisicao.nome(), requisicao.frequenciaSemanal(), requisicao.treinos());
        return consultas.detalhe(plano.getIdentificador().orElseThrow());
    }

    @PostMapping("{identificador}/encerrar")
    public PlanoDeTreinoDetalhe encerrar(@PathVariable PlanoDeTreinoIdentificador identificador) {
        servico.encerrar(identificador);
        return consultas.detalhe(identificador);
    }
}

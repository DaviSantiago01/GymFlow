package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.adesaoplano;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.adesaoplano.AdesaoPlanoAplicacaoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.adesaoplano.AdesaoPlanoConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.adesaoplano.AdesaoPlanoFormulario;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.adesaoplano.AdesaoPlanoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.adesaoplano.MeuPlano;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.mensalidade.MensalidadeAplicacaoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.plano.PlanoAplicacaoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.AdesaoPlano;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.AdesaoPlanoAutorizacoes;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.AdesaoPlanoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.AdesaoPlanoServico;
import dev.sauloaraujo.d5.framework.ui.AgregadoControladorAbstrato;
import dev.sauloaraujo.d5.framework.ui.Backend;

@RestController
@RequestMapping(Backend.PREFIXO + "core/adesao-plano")
public class AdesaoPlanoControlador extends AgregadoControladorAbstrato<AdesaoPlanoIdentificador, AdesaoPlano,
        AdesaoPlanoAutorizacoes, AdesaoPlanoProjecao, AdesaoPlanoConsulta> {
    private final AdesaoPlanoServico servico;
    private final AdesaoPlanoAplicacaoServico consultas;
    private final MensalidadeAplicacaoServico mensalidades;
    private final PlanoAplicacaoServico planos;

    public AdesaoPlanoControlador(AdesaoPlanoServico servico, AdesaoPlanoAplicacaoServico consultas,
            MensalidadeAplicacaoServico mensalidades, PlanoAplicacaoServico planos) {
        super(servico, consultas);
        this.servico = Objects.requireNonNull(servico);
        this.consultas = Objects.requireNonNull(consultas);
        this.mensalidades = Objects.requireNonNull(mensalidades);
        this.planos = Objects.requireNonNull(planos);
    }

    @Override
    public AdesaoPlanoFormulario inclusao() {
        return consultas.formulario();
    }

    @GetMapping("{identificador}/visualizacao")
    public AdesaoVisualizacao visualizacao(@PathVariable AdesaoPlanoIdentificador identificador) {
        var adesao = consultas.projetar(identificador);
        var gerenciavel = servico.criarAutorizacoes(Optional.of(identificador)).getTrocar();
        return new AdesaoVisualizacao(adesao, mensalidades.listarDaAdesao(identificador),
                gerenciavel ? planos.listarAtivos() : List.of(), consultas.cronograma(identificador));
    }

    @GetMapping("meu-plano")
    public MeuPlano meuPlano() {
        return consultas.meuPlano();
    }

    @PostMapping("aderir")
    public AdesaoPlanoProjecao aderir(@RequestBody AdesaoRequisicao requisicao) {
        var adesao = servico.aderir(requisicao.aluno(), requisicao.plano(),
                Optional.ofNullable(requisicao.quantidadePeriodos()));
        return consultas.projetar(adesao.getIdentificador().orElseThrow());
    }

    @PostMapping("{identificador}/trocar")
    public AdesaoPlanoProjecao trocar(@PathVariable AdesaoPlanoIdentificador identificador,
            @RequestBody TrocaRequisicao requisicao) {
        var nova = servico.trocar(identificador, requisicao.plano(),
                Optional.ofNullable(requisicao.quantidadePeriodos()));
        return consultas.projetar(nova.getIdentificador().orElseThrow());
    }

    @PostMapping("{identificador}/cancelar")
    public AdesaoPlanoProjecao cancelar(@PathVariable AdesaoPlanoIdentificador identificador) {
        servico.cancelar(identificador);
        return consultas.projetar(identificador);
    }
}

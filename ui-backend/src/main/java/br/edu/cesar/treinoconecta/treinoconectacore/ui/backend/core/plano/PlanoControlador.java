package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.plano;

import java.math.BigDecimal;
import java.util.Objects;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.plano.PlanoAplicacaoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.plano.PlanoConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.plano.PlanoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.Plano;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.PlanoAutorizacoes;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.PlanoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.PlanoServico;
import dev.sauloaraujo.d5.framework.ui.AgregadoControladorAbstrato;
import dev.sauloaraujo.d5.framework.ui.Backend;

/** As rotas genéricas de inclusão, alteração e exclusão do D5 permanecem bloqueadas pelas autorizações. */
@RestController
@RequestMapping(Backend.PREFIXO + "core/plano")
public class PlanoControlador
        extends AgregadoControladorAbstrato<PlanoIdentificador, Plano, PlanoAutorizacoes, PlanoProjecao, PlanoConsulta> {
    private final PlanoServico servico;
    private final PlanoAplicacaoServico consultas;

    public PlanoControlador(PlanoServico servico, PlanoAplicacaoServico consultas) {
        super(servico, consultas);
        this.servico = Objects.requireNonNull(servico);
        this.consultas = Objects.requireNonNull(consultas);
    }

    @GetMapping("{identificador}/visualizacao")
    public PlanoProjecao visualizacao(@PathVariable PlanoIdentificador identificador) {
        return consultas.projetar(identificador);
    }

    @PostMapping("cadastrar")
    public PlanoProjecao cadastrar(@RequestBody PlanoRequisicao requisicao) {
        var plano = servico.cadastrar(nome(requisicao), valor(requisicao), duracao(requisicao));
        return consultas.projetar(plano.getIdentificador().orElseThrow());
    }

    @PostMapping("{identificador}/editar")
    public PlanoProjecao editar(@PathVariable PlanoIdentificador identificador, @RequestBody PlanoRequisicao requisicao) {
        servico.editar(identificador, nome(requisicao), valor(requisicao), duracao(requisicao));
        return consultas.projetar(identificador);
    }

    @PostMapping("{identificador}/inativar")
    public PlanoProjecao inativar(@PathVariable PlanoIdentificador identificador) {
        servico.inativar(identificador);
        return consultas.projetar(identificador);
    }

    /** Campos ausentes chegam ao domínio como valores inválidos, que respondem com a mensagem da regra. */
    private static String nome(PlanoRequisicao requisicao) {
        return Objects.requireNonNullElse(requisicao.nome(), "");
    }

    private static BigDecimal valor(PlanoRequisicao requisicao) {
        return Objects.requireNonNullElse(requisicao.valor(), BigDecimal.ZERO);
    }

    private static int duracao(PlanoRequisicao requisicao) {
        return Objects.requireNonNullElse(requisicao.duracaoMeses(), 0);
    }
}

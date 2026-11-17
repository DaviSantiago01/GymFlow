package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.mensalidade;

import java.util.Map;
import java.util.Objects;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.mensalidade.MensalidadeAplicacaoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.mensalidade.MensalidadeConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.mensalidade.MensalidadeDetalhe;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.mensalidade.MensalidadeProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.AdesaoPlanoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.Mensalidade;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.MensalidadeAutorizacoes;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.MensalidadeIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.MensalidadeServico;
import dev.sauloaraujo.d5.framework.ui.AgregadoControladorAbstrato;
import dev.sauloaraujo.d5.framework.ui.Backend;

@RestController
@RequestMapping(Backend.PREFIXO + "core/mensalidade")
public class MensalidadeControlador extends AgregadoControladorAbstrato<MensalidadeIdentificador, Mensalidade,
        MensalidadeAutorizacoes, MensalidadeProjecao, MensalidadeConsulta> {
    private final MensalidadeServico servico;
    private final MensalidadeAplicacaoServico consultas;
    private final AdesaoPlanoServico adesoes;

    public MensalidadeControlador(MensalidadeServico servico, MensalidadeAplicacaoServico consultas,
            AdesaoPlanoServico adesoes) {
        super(servico, consultas);
        this.servico = Objects.requireNonNull(servico);
        this.consultas = Objects.requireNonNull(consultas);
        this.adesoes = Objects.requireNonNull(adesoes);
    }

    @GetMapping("{identificador}/visualizacao")
    public MensalidadeDetalhe visualizacao(@PathVariable MensalidadeIdentificador identificador) {
        return consultas.detalhe(identificador);
    }

    /** Gera, sem duplicar, as mensalidades dos períodos já iniciados de todas as adesões ativas. */
    @PostMapping("gerar")
    public Map<String, Integer> gerar() {
        return Map.of("geradas", adesoes.gerarMensalidades());
    }

    @PostMapping("{identificador}/pagar")
    public MensalidadeDetalhe pagar(@PathVariable MensalidadeIdentificador identificador,
            @RequestBody PagamentoRequisicao requisicao) {
        servico.registrarPagamento(identificador, requisicao.forma());
        return consultas.detalhe(identificador);
    }

    @PostMapping("{identificador}/pagamentos/{pagamento}/estornar")
    public MensalidadeDetalhe estornar(@PathVariable MensalidadeIdentificador identificador, @PathVariable long pagamento,
            @RequestBody EstornoRequisicao requisicao) {
        servico.estornarPagamento(identificador, pagamento, Objects.requireNonNullElse(requisicao.motivo(), ""));
        return consultas.detalhe(identificador);
    }
}

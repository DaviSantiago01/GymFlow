package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.meta;

import java.util.Objects;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.meta.MetaAplicacaoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.meta.MetaConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.meta.MetaFormulario;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.meta.MetaProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta.Meta;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta.MetaAutorizacoes;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta.MetaIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta.MetaServico;
import dev.sauloaraujo.d5.framework.ui.AgregadoControladorAbstrato;
import dev.sauloaraujo.d5.framework.ui.Backend;

@RestController
@RequestMapping(Backend.PREFIXO + "core/meta")
public class MetaControlador
        extends AgregadoControladorAbstrato<MetaIdentificador, Meta, MetaAutorizacoes, MetaProjecao, MetaConsulta> {
    private final MetaServico servico;
    private final MetaAplicacaoServico consultas;

    public MetaControlador(MetaServico servico, MetaAplicacaoServico consultas) {
        super(servico, consultas);
        this.servico = Objects.requireNonNull(servico);
        this.consultas = Objects.requireNonNull(consultas);
    }

    @Override
    public MetaFormulario inclusao() {
        return consultas.formulario();
    }

    @PostMapping("criar")
    public MetaProjecao criar(@RequestBody MetaRequisicao requisicao) {
        var meta = servico.criar(requisicao.aluno(), requisicao.indicador(), requisicao.valorDesejado(), requisicao.inicio(),
                requisicao.fim());
        return consultas.projetar(meta.getIdentificador().orElseThrow());
    }

    @PostMapping("{identificador}/ajustar")
    public MetaProjecao ajustar(@PathVariable MetaIdentificador identificador, @RequestBody AjusteMetaRequisicao requisicao) {
        servico.ajustar(identificador, requisicao.valorDesejado(), requisicao.inicio(), requisicao.fim());
        return consultas.projetar(identificador);
    }

    @PostMapping("{identificador}/substituir")
    public MetaProjecao substituir(@PathVariable MetaIdentificador identificador, @RequestBody AjusteMetaRequisicao requisicao) {
        var meta = servico.substituir(identificador, requisicao.valorDesejado(), requisicao.inicio(), requisicao.fim());
        return consultas.projetar(meta.getIdentificador().orElseThrow());
    }

    @PostMapping("{identificador}/encerrar")
    public MetaProjecao encerrar(@PathVariable MetaIdentificador identificador) {
        servico.encerrar(identificador);
        return consultas.projetar(identificador);
    }
}

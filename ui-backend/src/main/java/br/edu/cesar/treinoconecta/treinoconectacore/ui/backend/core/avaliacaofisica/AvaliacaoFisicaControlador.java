package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.avaliacaofisica;

import java.util.Objects;
import java.util.Optional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaofisica.AvaliacaoFisicaAplicacaoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaofisica.AvaliacaoFisicaConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaofisica.AvaliacaoFisicaDetalhe;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaofisica.AvaliacaoFisicaFormulario;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaofisica.AvaliacaoFisicaProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaofisica.ComparacaoAvaliacoes;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaofisica.EvolucaoIndicador;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaofisica.SituacaoAtualAluno;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.AvaliacaoFisica;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.AvaliacaoFisicaAutorizacoes;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.AvaliacaoFisicaIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.AvaliacaoFisicaServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.IndicadorFisico;
import dev.sauloaraujo.d5.framework.ui.AgregadoControladorAbstrato;
import dev.sauloaraujo.d5.framework.ui.Backend;
import dev.sauloaraujo.d5.framework.ui.IAgregadoRecurso;

@RestController
@RequestMapping(Backend.PREFIXO + "core/avaliacao-fisica")
public class AvaliacaoFisicaControlador extends
        AgregadoControladorAbstrato<AvaliacaoFisicaIdentificador, AvaliacaoFisica, AvaliacaoFisicaAutorizacoes, AvaliacaoFisicaProjecao, AvaliacaoFisicaConsulta> {
    private final AvaliacaoFisicaServico servico;
    private final AvaliacaoFisicaAplicacaoServico consultas;

    public AvaliacaoFisicaControlador(AvaliacaoFisicaServico servico, AvaliacaoFisicaAplicacaoServico consultas) {
        super(servico, consultas);
        this.servico = Objects.requireNonNull(servico);
        this.consultas = Objects.requireNonNull(consultas);
    }

    @Override
    public AvaliacaoFisicaFormulario inclusao() {
        return consultas.formulario();
    }

    /** A rota de alteração recebe também os dados do formulário, em uma única operação. */
    @Override
    protected IAgregadoRecurso<AvaliacaoFisica, AvaliacaoFisicaAutorizacoes> alteracao(AvaliacaoFisica agregado,
            AvaliacaoFisicaAutorizacoes autorizacoes) {
        return new AvaliacaoFisicaAlteracaoRecurso(agregado, autorizacoes, consultas.formulario());
    }

    @PostMapping("registrar")
    public AvaliacaoFisicaDetalhe registrar(@RequestBody AvaliacaoFisicaRequisicao requisicao) {
        var avaliacao = servico.registrar(requisicao.aluno(), requisicao.data(), requisicao.peso(), requisicao.altura(),
                requisicao.medidas());
        return consultas.detalhe(avaliacao.getIdentificador().orElseThrow());
    }

    @GetMapping("{identificador}/visualizacao")
    public AvaliacaoFisicaDetalhe visualizacao(@PathVariable AvaliacaoFisicaIdentificador identificador) {
        return consultas.detalhe(identificador);
    }

    @PostMapping("{identificador}/corrigir")
    public AvaliacaoFisicaDetalhe corrigir(@PathVariable AvaliacaoFisicaIdentificador identificador,
            @RequestBody CorrecaoAvaliacaoRequisicao requisicao) {
        var avaliacao = servico.corrigir(identificador, requisicao.data(), requisicao.peso(), requisicao.altura(),
                requisicao.medidas());
        return consultas.detalhe(avaliacao.getIdentificador().orElseThrow());
    }

    @PostMapping("{identificador}/remover")
    public void remover(@PathVariable AvaliacaoFisicaIdentificador identificador) {
        servico.remover(identificador);
    }

    @GetMapping("{identificador}/comparacao")
    public ComparacaoAvaliacoes comparacao(@PathVariable AvaliacaoFisicaIdentificador identificador,
            @RequestParam Optional<AvaliacaoFisicaIdentificador> referencia) {
        return consultas.comparacao(identificador, referencia);
    }

    @GetMapping("evolucao")
    public EvolucaoIndicador evolucao(@RequestParam Optional<AlunoIdentificador> aluno,
            @RequestParam Optional<IndicadorFisico> indicador) {
        return consultas.evolucao(aluno, indicador);
    }

    @GetMapping("situacao")
    public SituacaoAtualAluno situacao(@RequestParam Optional<AlunoIdentificador> aluno) {
        return consultas.situacao(aluno);
    }
}

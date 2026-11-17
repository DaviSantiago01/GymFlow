package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.mensalidade;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.mensalidade.spi.IMensalidadeAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.AdesaoPlanoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.Mensalidade;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.MensalidadeAutorizacoes;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.MensalidadeIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.MensalidadeServico;
import dev.sauloaraujo.d5.framework.aplicacao.IConsultaResultado;
import dev.sauloaraujo.d5.framework.aplicacao.impl.AgregadoAplicacaoServicoAbstrato;
import dev.sauloaraujo.d5.framework.aplicacao.impl.Consulta;
import dev.sauloaraujo.d5.framework.aplicacao.impl.ConsultaResultado;

public final class MensalidadeAplicacaoServico extends AgregadoAplicacaoServicoAbstrato<MensalidadeIdentificador, Mensalidade,
        MensalidadeAutorizacoes, MensalidadeProjecao, MensalidadeConsulta> {
    private final IMensalidadeAplicacaoRepositorio repositorio;
    private final MensalidadeServico servico;

    public MensalidadeAplicacaoServico(IMensalidadeAplicacaoRepositorio repositorio, MensalidadeServico servico) {
        super(repositorio, servico);
        this.repositorio = Objects.requireNonNull(repositorio);
        this.servico = Objects.requireNonNull(servico);
    }

    @Override
    public IConsultaResultado<MensalidadeProjecao> pesquisar(MensalidadeConsulta consulta) {
        var resultado = super.pesquisar(consulta);
        var hoje = servico.hoje();
        var projecoes = new ArrayList<MensalidadeProjecao>();
        for (var projecao : resultado.getProjecoes()) {
            projecoes.add(projecao.calcularEm(hoje));
        }
        return new ConsultaResultado<>(projecoes, resultado.getTotal());
    }

    public MensalidadeDetalhe detalhe(MensalidadeIdentificador identificador) {
        var mensalidade = servico.buscar(identificador);
        return new MensalidadeDetalhe(repositorio.projetar(identificador).calcularEm(servico.hoje()),
                mensalidade.getPagamentos());
    }

    public List<MensalidadeProjecao> listarDaAdesao(AdesaoPlanoIdentificador adesao) {
        var consulta = new MensalidadeConsulta(Optional.of(MensalidadeOrdenacao.VENCIMENTO), Optional.of(true),
                Optional.empty(), Optional.of(Consulta.TAMANHO_PAGINA_MAXIMO), Optional.empty(),
                Optional.of(adesao.toString()), Optional.empty());
        return pesquisar(consulta).getProjecoes();
    }
}

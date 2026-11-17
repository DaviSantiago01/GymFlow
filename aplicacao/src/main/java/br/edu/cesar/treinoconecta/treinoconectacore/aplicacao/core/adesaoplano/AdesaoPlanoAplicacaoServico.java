package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.adesaoplano;

import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.adesaoplano.spi.IAdesaoPlanoAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.AlunoConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.AlunoOrdenacao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.spi.IAlunoAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.mensalidade.MensalidadeAplicacaoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.mensalidade.MensalidadeConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.mensalidade.MensalidadeOrdenacao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.plano.PlanoAplicacaoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.AdesaoPlano;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.AdesaoPlanoAutorizacoes;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.AdesaoPlanoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.AdesaoPlanoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.MensalidadeServico;
import dev.sauloaraujo.d5.framework.aplicacao.impl.AgregadoAplicacaoServicoAbstrato;
import dev.sauloaraujo.d5.framework.aplicacao.impl.Consulta;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
import dev.sauloaraujo.d5.framework.dominio.spi.NaoAutorizadoExcecao;

public final class AdesaoPlanoAplicacaoServico extends AgregadoAplicacaoServicoAbstrato<AdesaoPlanoIdentificador, AdesaoPlano,
        AdesaoPlanoAutorizacoes, AdesaoPlanoProjecao, AdesaoPlanoConsulta> {
    private final IAdesaoPlanoAplicacaoRepositorio repositorio;
    private final AdesaoPlanoServico servico;
    private final IAlunoAplicacaoRepositorio alunos;
    private final PlanoAplicacaoServico planos;
    private final MensalidadeAplicacaoServico mensalidades;
    private final MensalidadeServico servicoMensalidade;
    private final ISegurancaServico seguranca;

    public AdesaoPlanoAplicacaoServico(IAdesaoPlanoAplicacaoRepositorio repositorio, AdesaoPlanoServico servico,
            IAlunoAplicacaoRepositorio alunos, PlanoAplicacaoServico planos, MensalidadeAplicacaoServico mensalidades,
            MensalidadeServico servicoMensalidade, ISegurancaServico seguranca) {
        super(repositorio, servico);
        this.repositorio = Objects.requireNonNull(repositorio);
        this.servico = Objects.requireNonNull(servico);
        this.alunos = Objects.requireNonNull(alunos);
        this.planos = Objects.requireNonNull(planos);
        this.mensalidades = Objects.requireNonNull(mensalidades);
        this.servicoMensalidade = Objects.requireNonNull(servicoMensalidade);
        this.seguranca = Objects.requireNonNull(seguranca);
    }

    public AdesaoPlanoProjecao projetar(AdesaoPlanoIdentificador identificador) {
        servico.buscar(identificador);
        return repositorio.projetar(identificador);
    }

    /** A lista de alunos é lida diretamente do repositório: a permissão é a de aderir, do administrador. */
    public AdesaoPlanoFormulario formulario() {
        if (!servico.criarAutorizacoes(Optional.empty()).getAderir()) {
            throw new NaoAutorizadoExcecao("Ação não autorizada");
        }
        var consulta = new AlunoConsulta(Optional.of(AlunoOrdenacao.NOME), Optional.empty(), Optional.empty(),
                Optional.of(Consulta.TAMANHO_PAGINA_MAXIMO), Optional.empty());
        return new AdesaoPlanoFormulario(alunos.pesquisar(consulta).getProjecoes(), planos.listarAtivos());
    }

    public MeuPlano meuPlano() {
        if (!seguranca.possuiAutorizacao(Autorizacao.USUARIO.name())) {
            throw new NaoAutorizadoExcecao("Acesso exclusivo de alunos");
        }
        var consulta = new MensalidadeConsulta(Optional.of(MensalidadeOrdenacao.VENCIMENTO), Optional.of(true),
                Optional.empty(), Optional.of(Consulta.TAMANHO_PAGINA_MAXIMO), Optional.empty(), Optional.empty(),
                Optional.empty());
        return new MeuPlano(repositorio.projetarAtivaDoUsuario(seguranca.getUsuario()),
                mensalidades.pesquisar(consulta).getProjecoes(), servicoMensalidade.hoje());
    }
}

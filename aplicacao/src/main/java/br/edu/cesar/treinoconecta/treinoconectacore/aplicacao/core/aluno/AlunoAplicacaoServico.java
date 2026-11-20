package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.spi.IAlunoAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.spi.IAlunoGestaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.registroacesso.RegistroAcessoAplicacaoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.spi.IAdesaoPlanoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.Aluno;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoAutorizacoes;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.SituacaoAluno;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.IAlunoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.spi.IMensalidadeRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.spi.IPlanoRepositorio;
import dev.sauloaraujo.d5.framework.aplicacao.impl.AgregadoAplicacaoServicoAbstrato;

public final class AlunoAplicacaoServico extends
        AgregadoAplicacaoServicoAbstrato<AlunoIdentificador, Aluno, AlunoAutorizacoes, AlunoProjecao, AlunoConsulta> {
    private final AlunoServico servico;
    private final IAlunoRepositorio alunos;
    private final IAlunoGestaoRepositorio gestao;
    private final IAdesaoPlanoRepositorio adesoes;
    private final IPlanoRepositorio planos;
    private final IMensalidadeRepositorio mensalidades;
    private final RegistroAcessoAplicacaoServico acessos;
    private final Clock relogio;

    public AlunoAplicacaoServico(IAlunoAplicacaoRepositorio repositorio, AlunoServico servico, IAlunoRepositorio alunos,
            IAlunoGestaoRepositorio gestao, IAdesaoPlanoRepositorio adesoes, IPlanoRepositorio planos,
            IMensalidadeRepositorio mensalidades, RegistroAcessoAplicacaoServico acessos, Clock relogio) {
        super(repositorio, servico);
        this.servico = Objects.requireNonNull(servico);
        this.alunos = Objects.requireNonNull(alunos);
        this.gestao = Objects.requireNonNull(gestao);
        this.adesoes = Objects.requireNonNull(adesoes);
        this.planos = Objects.requireNonNull(planos);
        this.mensalidades = Objects.requireNonNull(mensalidades);
        this.acessos = Objects.requireNonNull(acessos);
        this.relogio = Objects.requireNonNull(relogio);
    }

    /** Lista da administração (F005), com a situação calculada e filtro opcional por situação. */
    public List<AlunoGestaoProjecao> pesquisarGestao(Optional<String> busca, Optional<SituacaoAluno> situacao) {
        servico.validarGestao();
        var trecho = busca.map(String::strip).filter(texto -> !texto.isEmpty())
                .map(texto -> texto.matches("[\\d.\\-\\s]+") ? texto.replaceAll("[.\\-\\s]", "") : texto)
                .map(texto -> texto.toLowerCase(Locale.ROOT));
        return gestao.pesquisar(trecho, hoje()).stream()
                .filter(item -> situacao.map(valor -> valor == item.situacao()).orElse(true)).toList();
    }

    public AlunoFicha ficha(AlunoIdentificador identificador) {
        Objects.requireNonNull(identificador);
        servico.validarGestao();
        if (!alunos.existePorIdentificador(identificador)) {
            throw new NoSuchElementException(AlunoServico.IDENTIFICADOR_INEXISTENTE_MODELO.formatted(identificador));
        }
        var aluno = alunos.buscarPorIdentificador(identificador);
        var hoje = hoje();
        var matricula = matricula(identificador, hoje);
        boolean atrasada = mensalidades.existeAtrasadaDoAluno(identificador, hoje);
        var situacao = SituacaoAluno.calcular(aluno.isAtivo(), aluno.bloqueado(), matricula.isPresent(), atrasada);
        var responsavel = aluno.getResponsavel()
                .map(item -> new AlunoFicha.Responsavel(item.nome(), item.cpf().formatado(), item.telefone())).orElse(null);
        Integer idade = aluno.getNascimento().map(data -> Period.between(data, hoje).getYears()).orElse(null);
        return new AlunoFicha(identificador.toString(), aluno.getUsuario(), aluno.getNome(),
                aluno.getCpf().map(cpf -> cpf.formatado()).orElse(null), aluno.getNascimento().orElse(null), idade,
                aluno.menorDeIdadeEm(hoje), aluno.getEmail().map(Object::toString).orElse(null), aluno.getTelefone(),
                aluno.getEndereco(), responsavel, aluno.isAtivo(),
                aluno.getBloqueio().map(item -> item.motivo()).orElse(null),
                aluno.getBloqueio().map(item -> item.inicio()).orElse(null), aluno.getCpf().isPresent(), situacao,
                atrasada, matricula.orElse(null), acessos.presente(identificador), gestao.historico(identificador),
                acessos.frequencia(identificador, Optional.empty(), Optional.of(hoje)));
    }

    /** RN008: a validade é o último dia do período de cobrança em curso da adesão ativa. */
    private Optional<MatriculaResumo> matricula(AlunoIdentificador aluno, LocalDate hoje) {
        return adesoes.buscarAtivaDoAluno(aluno).map(adesao -> {
            var periodos = adesao.periodosIniciadosAte(hoje);
            var validade = periodos.isEmpty() ? adesao.periodo(0).fim() : periodos.get(periodos.size() - 1).fim();
            var plano = planos.buscarPorIdentificador(adesao.getPlano());
            return new MatriculaResumo(adesao.getIdentificador().orElseThrow().toString(), plano.getNome(),
                    adesao.getInicio(), validade);
        });
    }

    private LocalDate hoje() {
        return LocalDate.now(relogio.withZone(ParametrosAcademia.FUSO));
    }
}

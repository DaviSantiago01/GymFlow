package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.desafio;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.IAlunoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.desafio.spi.IDesafioRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.SituacaoProfissional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.spi.IProfissionalRepositorio;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoServicoAbstrato;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;

/** Regras de criação, participação e avaliações de desafios. */
public final class DesafioServico extends AgregadoServicoAbstrato<DesafioIdentificador, Desafio, DesafioAutorizacoes> {
    private final IDesafioRepositorio desafios;
    private final IAlunoRepositorio alunos;
    private final IProfissionalRepositorio profissionais;
    private final Clock relogio;

    public DesafioServico(ISegurancaServico seguranca, IDesafioRepositorio desafios, IAlunoRepositorio alunos,
            IProfissionalRepositorio profissionais, Clock relogio) {
        super(seguranca, desafios, "Desafio %s já existe", "Desafio %s não encontrado");
        this.desafios = Objects.requireNonNull(desafios);
        this.alunos = Objects.requireNonNull(alunos);
        this.profissionais = Objects.requireNonNull(profissionais);
        this.relogio = Objects.requireNonNull(relogio);
    }

    @Override public DesafioAutorizacoes criarAutorizacoes(Optional<DesafioIdentificador> identificador) {
        boolean gestor = gestor();
        return new DesafioAutorizacoes(gestor || getSegurancaServico().possuiAutorizacao(Autorizacao.USUARIO.name()),
                gestor, gestor, gestor);
    }

    public Desafio criar(DadosDesafio dados) {
        validarAutorizacao(Optional.empty(), DesafioAutorizacoes::getCriar);
        return desafios.incluir(Desafio.criar(dados));
    }

    public void inscrever(DesafioIdentificador id, String indicador, java.math.BigDecimal meta) {
        var aluno = alunos.buscarPorUsuario(getSegurancaServico().getUsuario());
        Validate.validState(aluno.isAtivo(), "Aluno inativo não pode participar.");
        var alunoId = aluno.getIdentificador().orElseThrow();
        var desafio = desafios.bloquear(id);
        var hoje = java.time.LocalDate.now(relogio);
        Validate.validState(desafio.aceitaInscricao(hoje), "O prazo de inscrição do desafio foi encerrado.");
        Validate.isTrue(desafio.indicador().name().equals(indicador), "O indicador da meta deve corresponder ao desafio.");
        VigenciaDesafio.validarMeta(meta);
        Validate.validState(!desafios.existeInscricao(id, alunoId), "Aluno já inscrito neste desafio.");
        Validate.validState(desafios.existeAvaliacaoInicial(id, alunoId), "Registre a avaliação inicial antes da inscrição.");
        desafios.inscrever(desafio, alunoId, desafio.indicador(), meta,
                desafios.buscarAvaliacaoInicial(id, alunoId).orElseThrow().identificador().orElseThrow(), OffsetDateTime.now(relogio));
    }

    public void registrarAvaliacao(DesafioIdentificador desafio, br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador aluno,
            br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.ProfissionalIdentificador profissional,
            TipoAvaliacaoDesafio tipo, java.math.BigDecimal peso, java.math.BigDecimal cintura, java.math.BigDecimal gordura) {
        validarGestorOuInstrutor();
        desafios.bloquear(desafio);
        Validate.validState(alunos.existePorIdentificador(aluno), "Aluno não encontrado.");
        Validate.validState(profissionais.existePorIdentificador(profissional), "Instrutor não encontrado.");
        var cadastrado = profissionais.buscarPorIdentificador(profissional);
        Validate.validState(cadastrado.getSituacao() == SituacaoProfissional.ATIVO && cadastrado.professor(), "Instrutor inativo ou não habilitado.");
        if (tipo == TipoAvaliacaoDesafio.INICIAL)
            Validate.validState(!desafios.existeAvaliacaoInicial(desafio, aluno), "O aluno já possui avaliação inicial neste desafio.");
        desafios.registrarAvaliacao(desafio, aluno, profissional, tipo, peso, cintura, gordura, OffsetDateTime.now(relogio));
    }

    public void encerrar(DesafioIdentificador id) {
        validarAutorizacao(Optional.of(id), DesafioAutorizacoes::getAlterar);
        desafios.encerrar(desafios.bloquear(id).encerrar());
    }

    public void editarAvaliacao(Long id, java.math.BigDecimal peso, java.math.BigDecimal cintura, java.math.BigDecimal gordura) {
        validarGestorOuInstrutor();
        var atual = desafios.buscarAvaliacao(id).orElseThrow(() -> new java.util.NoSuchElementException("Avaliação não encontrada."));
        Validate.validState(VigenciaDesafio.avaliacaoEditavel(atual.registro(), OffsetDateTime.now(relogio)), "Avaliações com mais de 48 horas não podem ser alteradas.");
        desafios.alterarAvaliacao(atual.alterar(peso, cintura, gordura));
    }

    public void excluirAvaliacao(Long id) {
        validarGestorOuInstrutor();
        var atual = desafios.buscarAvaliacao(id).orElseThrow(() -> new java.util.NoSuchElementException("Avaliação não encontrada."));
        Validate.validState(VigenciaDesafio.avaliacaoEditavel(atual.registro(), OffsetDateTime.now(relogio)), "Avaliações com mais de 48 horas não podem ser excluídas.");
        Validate.validState(desafios.excluirAvaliacao(id), "A avaliação não pôde ser excluída.");
    }

    private boolean gestor() {
        return getSegurancaServico().possuiAutorizacao(Autorizacao.ADMINISTRADOR.name())
                || getSegurancaServico().possuiAutorizacao(Autorizacao.PERSONAL.name());
    }
    private void validarGestorOuInstrutor() {
        if (!gestor())
            throw new dev.sauloaraujo.d5.framework.dominio.spi.NaoAutorizadoExcecao("Ação disponível para gestor ou profissional autorizado.");
    }
}

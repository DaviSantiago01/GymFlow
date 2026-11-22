package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aula;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.IAlunoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aula.spi.IAulaRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.ProfissionalIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.SituacaoProfissional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.spi.IProfissionalRepositorio;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoServicoAbstrato;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
import dev.sauloaraujo.d5.framework.dominio.spi.NaoAutorizadoExcecao;

public final class AulaServico extends AgregadoServicoAbstrato<AulaIdentificador,Aula,AulaAutorizacoes> {
    public static final String INEXISTENTE = "Aula %s não encontrada";
    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private final IAulaRepositorio aulas;
    private final IAlunoRepositorio alunos;
    private final IProfissionalRepositorio profissionais;
    private final Clock relogio;

    public AulaServico(ISegurancaServico seguranca, IAulaRepositorio aulas, IAlunoRepositorio alunos,
            IProfissionalRepositorio profissionais, Clock relogio) {
        super(seguranca,aulas,"Aula %s já existe",INEXISTENTE);
        this.aulas=Objects.requireNonNull(aulas); this.alunos=Objects.requireNonNull(alunos);
        this.profissionais=Objects.requireNonNull(profissionais); this.relogio=Objects.requireNonNull(relogio);
    }

    @Override public AulaAutorizacoes criarAutorizacoes(Optional<AulaIdentificador> identificador) {
        Objects.requireNonNull(identificador);
        boolean gestor=gestor();
        return new AulaAutorizacoes(gestor||getSegurancaServico().possuiAutorizacao(Autorizacao.USUARIO.name()),
                gestor,gestor,gestor);
    }

    public Aula cadastrar(DadosAula dados) {
        validarAutorizacao(Optional.empty(),AulaAutorizacoes::getCriar);
        validarProfessor(dados.professor());
        Validate.validState(!aulas.existeChoque(dados.professor(),dados.data(),dados.periodo().inicio(),dados.periodo().fim(),Optional.empty()),
                "O professor já possui aula sobreposta nesse dia.");
        return aulas.incluir(Aula.cadastrar(dados));
    }

    public Aula editar(AulaIdentificador identificador,DadosAula dados) {
        validarAutorizacao(Optional.of(identificador),AulaAutorizacoes::getAlterar);
        var atual=aulas.bloquear(identificador);
        validarProfessor(dados.professor());
        Validate.validState(!aulas.existeChoque(dados.professor(),dados.data(),dados.periodo().inicio(),dados.periodo().fim(),Optional.of(identificador)),
                "O professor já possui aula sobreposta nesse dia.");
        var editada=atual.editar(dados,aulas.contarInscritos(identificador)); aulas.alterar(editada); return editada;
    }

    public Aula cancelar(AulaIdentificador identificador) {
        validarAutorizacao(Optional.of(identificador),AulaAutorizacoes::getExcluir);
        var atual=aulas.bloquear(identificador); var cancelada=atual.cancelar();
        aulas.alterar(cancelada); aulas.cancelarInscritos(identificador,atual.titulo(),OffsetDateTime.now(relogio));
        return cancelada;
    }

    public void inscrever(AulaIdentificador identificador) {
        validarAlunoAutenticado();
        var aluno=alunos.buscarPorUsuario(getSegurancaServico().getUsuario());
        Validate.validState(aluno.isAtivo(),"Aluno inativo não pode se inscrever.");
        var alunoId=aluno.getIdentificador().orElseThrow();
        var aula=aulas.bloquear(identificador);
        var agora=LocalDateTime.now(relogio.withZone(FUSO));
        Validate.validState(aula.situacao()==SituacaoAula.ABERTA && LocalDateTime.of(aula.data(),aula.periodo().inicio()).isAfter(agora),
                "As inscrições desta aula estão encerradas.");
        Validate.validState(!aulas.existeInscricaoAtiva(identificador,alunoId),"Você já está inscrito nesta aula.");
        Validate.validState(aulas.contarInscritos(identificador)<aula.limite(),"A aula está esgotada.");
        aulas.inscrever(identificador,alunoId,OffsetDateTime.now(relogio));
    }

    public void cancelarInscricao(Long inscricao) {
        var aluno=alunoAutenticado();
        Validate.validState(aulas.cancelarInscricao(inscricao,aluno,OffsetDateTime.now(relogio)),
                "O cancelamento só pode ocorrer até 30 minutos antes do início.");
    }

    public void cancelarInscricaoGestao(Long inscricao) {
        validarAutorizacao(Optional.empty(),AulaAutorizacoes::getCriar);
        Validate.validState(aulas.cancelarInscricaoGestao(inscricao,OffsetDateTime.now(relogio)),"A inscrição não está ativa.");
    }

    private void validarProfessor(ProfissionalIdentificador identificador) {
        Validate.validState(profissionais.existePorIdentificador(identificador),"Professor não encontrado.");
        var professor=profissionais.buscarPorIdentificador(identificador);
        Validate.validState(professor.getSituacao()==SituacaoProfissional.ATIVO && professor.professor(),"Selecione um professor ativo cadastrado.");
    }
    private boolean gestor() { return getSegurancaServico().possuiAutorizacao(Autorizacao.ADMINISTRADOR.name()) || getSegurancaServico().possuiAutorizacao(Autorizacao.PERSONAL.name()); }
    private void validarAlunoAutenticado() { if (!getSegurancaServico().possuiAutorizacao(Autorizacao.USUARIO.name())) throw new NaoAutorizadoExcecao("Ação disponível para aluno autenticado"); }
    private AlunoIdentificador alunoAutenticado() { validarAlunoAutenticado(); return alunos.buscarPorUsuario(getSegurancaServico().getUsuario()).getIdentificador().orElseThrow(); }
}

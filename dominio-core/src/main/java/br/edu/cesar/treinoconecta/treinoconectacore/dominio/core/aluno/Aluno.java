package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.academia.AcademiaIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.Cpf;
import dev.sauloaraujo.d5.framework.dominio.IAgregado;

/**
 * Aluno da academia. Contas criadas pelo autocadastro (F002) têm só usuário e nome; a administração completa os dados
 * cadastrais (F005), que então passam a ser obrigatórios. Bloqueio e desativação preservam o cadastro e o histórico.
 */
public final class Aluno implements IAgregado<AlunoIdentificador> {
    public static final String ALUNO_INATIVO = "O aluno está inativo. Reative o cadastro antes de alterá-lo";
    public static final String ALUNO_JA_ATIVO = "O aluno já está ativo";
    public static final String ALUNO_JA_BLOQUEADO = "O aluno já possui um bloqueio ativo";
    public static final String ALUNO_SEM_BLOQUEIO = "O aluno não possui bloqueio ativo";

    private final Optional<AlunoIdentificador> identificador;
    private final String usuario;
    private final String nome;
    private final AcademiaIdentificador academia;
    private final Optional<Cpf> cpf;
    private final Optional<LocalDate> nascimento;
    private final Optional<Email> email;
    private final String telefone;
    private final String endereco;
    private final Optional<ResponsavelLegal> responsavel;
    private final boolean ativo;
    private final Optional<BloqueioAluno> bloqueio;

    public Aluno(Optional<AlunoIdentificador> identificador, String usuario, String nome) {
        this(identificador, usuario, nome, new AcademiaIdentificador("1"));
    }

    public Aluno(Optional<AlunoIdentificador> identificador, String usuario, String nome, AcademiaIdentificador academia) {
        this(identificador, usuario, nome, academia, Optional.empty(), Optional.empty(), Optional.empty(), "", "",
                Optional.empty(), true, Optional.empty());
    }

    public Aluno(Optional<AlunoIdentificador> identificador, String usuario, String nome, AcademiaIdentificador academia,
            Optional<Cpf> cpf, Optional<LocalDate> nascimento, Optional<Email> email, String telefone, String endereco,
            Optional<ResponsavelLegal> responsavel, boolean ativo, Optional<BloqueioAluno> bloqueio) {
        this.identificador = Objects.requireNonNull(identificador);
        this.usuario = Validate.notBlank(usuario, "Usuário obrigatório");
        this.nome = Validate.notBlank(nome, "Nome obrigatório");
        Validate.isTrue(usuario.length() <= 100 && nome.length() <= 100, "Nome ou usuário muito longo");
        this.academia = Objects.requireNonNull(academia);
        this.cpf = Objects.requireNonNull(cpf);
        this.nascimento = Objects.requireNonNull(nascimento);
        this.email = Objects.requireNonNull(email);
        this.telefone = Objects.requireNonNull(telefone);
        this.endereco = Objects.requireNonNull(endereco);
        this.responsavel = Objects.requireNonNull(responsavel);
        this.ativo = ativo;
        this.bloqueio = Objects.requireNonNull(bloqueio);
    }

    /** Cadastro feito pela administração (F005): os dados já chegam validados para a data atual. */
    public static Aluno cadastrar(String usuario, DadosAluno dados) {
        Objects.requireNonNull(dados);
        return new Aluno(Optional.empty(), usuario, dados.nome(), new AcademiaIdentificador("1"), Optional.of(dados.cpf()),
                Optional.of(dados.nascimento()), dados.email(), dados.telefone(), dados.endereco(), dados.responsavel(),
                true, Optional.empty());
    }

    public Aluno editar(DadosAluno dados) {
        Objects.requireNonNull(dados);
        Validate.validState(ativo, ALUNO_INATIVO);
        return new Aluno(identificador, usuario, dados.nome(), academia, Optional.of(dados.cpf()),
                Optional.of(dados.nascimento()), dados.email(), dados.telefone(), dados.endereco(), dados.responsavel(),
                ativo, bloqueio);
    }

    public Aluno bloquear(String motivo, Instant agora) {
        Validate.validState(ativo, ALUNO_INATIVO);
        Validate.validState(bloqueio.isEmpty(), ALUNO_JA_BLOQUEADO);
        return comSituacao(ativo, Optional.of(new BloqueioAluno(motivo, agora)));
    }

    public Aluno desbloquear() {
        Validate.validState(bloqueio.isPresent(), ALUNO_SEM_BLOQUEIO);
        return comSituacao(ativo, Optional.empty());
    }

    /** RN010: quando a exclusão definitiva não é permitida, o cadastro é desativado. */
    public Aluno desativar() {
        Validate.validState(ativo, ALUNO_INATIVO);
        return comSituacao(false, bloqueio);
    }

    public Aluno reativar() {
        Validate.validState(!ativo, ALUNO_JA_ATIVO);
        return comSituacao(true, bloqueio);
    }

    public boolean bloqueado() {
        return bloqueio.isPresent();
    }

    public boolean menorDeIdadeEm(LocalDate hoje) {
        return nascimento.map(data -> java.time.Period.between(data, hoje).getYears() < DadosAluno.MAIORIDADE)
                .orElse(false);
    }

    private Aluno comSituacao(boolean novoAtivo, Optional<BloqueioAluno> novoBloqueio) {
        return new Aluno(identificador, usuario, nome, academia, cpf, nascimento, email, telefone, endereco, responsavel,
                novoAtivo, novoBloqueio);
    }

    public Optional<AlunoIdentificador> getIdentificador() { return identificador; }
    public String getUsuario() { return usuario; }
    public AcademiaIdentificador getAcademia() { return academia; }
    public String getNome() { return nome; }
    public Optional<Cpf> getCpf() { return cpf; }
    public Optional<LocalDate> getNascimento() { return nascimento; }
    public Optional<Email> getEmail() { return email; }
    public String getTelefone() { return telefone; }
    public String getEndereco() { return endereco; }
    public Optional<ResponsavelLegal> getResponsavel() { return responsavel; }
    public boolean isAtivo() { return ativo; }
    public Optional<BloqueioAluno> getBloqueio() { return bloqueio; }
}

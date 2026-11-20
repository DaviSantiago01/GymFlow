package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.IAlunoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.IAlunoVinculosRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.ICredencialRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.IHistoricoAlunoRepositorio;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoServicoAbstrato;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;

public final class AlunoServico extends AgregadoServicoAbstrato<AlunoIdentificador, Aluno, AlunoAutorizacoes> {
    public static final String IDENTIFICADOR_EXISTENTE_MODELO = "Já existe um aluno com o identificador %s";
    public static final String IDENTIFICADOR_INEXISTENTE_MODELO = "Não existe aluno com o identificador %s";
    public static final String USUARIO_INVALIDO = "O usuário deve ter de 3 a 40 caracteres: letras, números, ponto, hífen ou sublinhado";
    public static final String SENHA_INVALIDA = "A senha deve ter pelo menos 8 caracteres e no máximo 72 bytes";
    public static final String USUARIO_DUPLICADO = "Este nome de usuário já está em uso";
    public static final String CPF_DUPLICADO = "Já existe outro aluno cadastrado com este CPF";
    public static final String EMAIL_DUPLICADO = "Já existe outro aluno cadastrado com este e-mail";

    /** Resultado da exclusão pedida pela administração (F005, RN010). */
    public enum ResultadoExclusao { EXCLUIDO, DESATIVADO }

    private final IAlunoRepositorio alunos;
    private final ICredencialRepositorio credenciais;
    private final IAlunoVinculosRepositorio vinculos;
    private final IHistoricoAlunoRepositorio historico;
    private final Clock relogio;

    public AlunoServico(ISegurancaServico seguranca, IAlunoRepositorio repositorio, ICredencialRepositorio credenciais,
            IAlunoVinculosRepositorio vinculos, IHistoricoAlunoRepositorio historico, Clock relogio) {
        super(seguranca, repositorio, IDENTIFICADOR_EXISTENTE_MODELO, IDENTIFICADOR_INEXISTENTE_MODELO);
        this.alunos = Objects.requireNonNull(repositorio);
        this.credenciais = Objects.requireNonNull(credenciais);
        this.vinculos = Objects.requireNonNull(vinculos);
        this.historico = Objects.requireNonNull(historico);
        this.relogio = Objects.requireNonNull(relogio);
    }

    /** Cadastro público de aluno; a transação do POST inclui aluno e credencial. */
    public Aluno cadastrar(String nome, String usuario, String senha, String confirmacaoSenha) {
        Objects.requireNonNull(nome);
        Objects.requireNonNull(confirmacaoSenha);
        String nomeNormalizado = nome.strip();
        String usuarioNormalizado = validarUsuario(usuario);
        Validate.isTrue(nomeNormalizado.length() >= 2 && nomeNormalizado.length() <= 100,
                "Informe um nome com 2 a 100 caracteres");
        validarSenha(senha);
        Validate.isTrue(senha.equals(confirmacaoSenha), "As senhas não conferem");
        Validate.validState(!alunos.existePorUsuario(usuarioNormalizado), USUARIO_DUPLICADO);
        var aluno = alunos.incluir(new Aluno(Optional.empty(), usuarioNormalizado, nomeNormalizado));
        credenciais.incluir(usuarioNormalizado, senha);
        return aluno;
    }

    @Override
    public AlunoAutorizacoes criarAutorizacoes(Optional<AlunoIdentificador> identificador) {
        Objects.requireNonNull(identificador);
        boolean personal = getSegurancaServico().possuiAutorizacao(Autorizacao.PERSONAL.name());
        boolean administrador = getSegurancaServico().possuiAutorizacao(Autorizacao.ADMINISTRADOR.name());
        return new AlunoAutorizacoes(personal || proprioAluno(identificador), administrador);
    }

    /** F005: cadastro pela administração, com dados completos e uma senha inicial para o aluno entrar no sistema. */
    public Aluno cadastrarPelaAdministracao(DadosAluno dados, String usuario, String senha) {
        Objects.requireNonNull(dados);
        validarAutorizacao(Optional.empty(), AlunoAutorizacoes::getGerenciar);
        String usuarioNormalizado = validarUsuario(usuario);
        validarSenha(senha);
        dados.validarEm(hoje());
        Validate.validState(!alunos.existePorUsuario(usuarioNormalizado), USUARIO_DUPLICADO);
        validarDadosUnicos(dados, Optional.empty());
        var aluno = alunos.incluir(Aluno.cadastrar(usuarioNormalizado, dados));
        credenciais.incluir(usuarioNormalizado, senha);
        registrar(aluno, TipoAlteracaoAluno.CADASTRO, "Cadastro realizado pela administração");
        return aluno;
    }

    public Aluno editar(AlunoIdentificador identificador, DadosAluno dados) {
        Objects.requireNonNull(dados);
        validarGerenciamento(identificador);
        dados.validarEm(hoje());
        var atual = alunos.bloquear(identificador);
        validarDadosUnicos(dados, Optional.of(identificador));
        var editado = atual.editar(dados);
        alunos.alterar(editado);
        registrar(editado, TipoAlteracaoAluno.ALTERACAO, descreverAlteracao(atual, editado));
        return editado;
    }

    public Aluno bloquear(AlunoIdentificador identificador, String motivo) {
        validarGerenciamento(identificador);
        var bloqueado = alunos.bloquear(identificador).bloquear(motivo, Instant.now(relogio));
        alunos.alterar(bloqueado);
        registrar(bloqueado, TipoAlteracaoAluno.BLOQUEIO, "Bloqueio: " + bloqueado.getBloqueio().orElseThrow().motivo());
        return bloqueado;
    }

    public Aluno desbloquear(AlunoIdentificador identificador) {
        validarGerenciamento(identificador);
        var desbloqueado = alunos.bloquear(identificador).desbloquear();
        alunos.alterar(desbloqueado);
        registrar(desbloqueado, TipoAlteracaoAluno.DESBLOQUEIO, "Bloqueio encerrado");
        return desbloqueado;
    }

    /**
     * RN010: com matrícula ativa, frequência ou qualquer outro registro associado, o aluno é desativado para preservar o
     * histórico; sem nenhum vínculo, o cadastro e a credencial são excluídos definitivamente.
     */
    public ResultadoExclusao remover(AlunoIdentificador identificador) {
        validarGerenciamento(identificador);
        var aluno = alunos.bloquear(identificador);
        if (vinculos.possuiMatriculaAtiva(identificador) || vinculos.possuiFrequencia(identificador)
                || vinculos.possuiOutrosRegistros(identificador)) {
            var desativado = aluno.desativar();
            alunos.alterar(desativado);
            registrar(desativado, TipoAlteracaoAluno.DESATIVACAO,
                    "Cadastro desativado: o aluno possui matrícula, frequência ou outros registros");
            return ResultadoExclusao.DESATIVADO;
        }
        alunos.excluirDefinitivamente(identificador);
        return ResultadoExclusao.EXCLUIDO;
    }

    public Aluno reativar(AlunoIdentificador identificador) {
        validarGerenciamento(identificador);
        var reativado = alunos.bloquear(identificador).reativar();
        alunos.alterar(reativado);
        registrar(reativado, TipoAlteracaoAluno.REATIVACAO, "Cadastro reativado");
        return reativado;
    }

    /** A lista e a ficha da administração exigem a mesma permissão das alterações. */
    public void validarGestao() {
        validarAutorizacao(Optional.empty(), AlunoAutorizacoes::getGerenciar);
    }

    private void validarGerenciamento(AlunoIdentificador identificador) {
        Objects.requireNonNull(identificador);
        validarAutorizacao(Optional.of(identificador), AlunoAutorizacoes::getGerenciar);
        validarExistencia(identificador);
    }

    /** RN001 e RN005: CPF e e-mail não podem pertencer a outro aluno. */
    private void validarDadosUnicos(DadosAluno dados, Optional<AlunoIdentificador> ignorado) {
        Validate.validState(!alunos.existePorCpf(dados.cpf(), ignorado), CPF_DUPLICADO);
        dados.email().ifPresent(email -> Validate.validState(!alunos.existePorEmail(email, ignorado), EMAIL_DUPLICADO));
    }

    private void registrar(Aluno aluno, TipoAlteracaoAluno tipo, String descricao) {
        historico.registrar(aluno.getIdentificador().orElseThrow(), tipo, descricao, getSegurancaServico().getUsuario(),
                Instant.now(relogio));
    }

    private static String descreverAlteracao(Aluno anterior, Aluno novo) {
        var campos = new ArrayList<String>();
        if (!anterior.getNome().equals(novo.getNome())) campos.add("nome");
        if (!anterior.getCpf().equals(novo.getCpf())) campos.add("CPF");
        if (!anterior.getNascimento().equals(novo.getNascimento())) campos.add("data de nascimento");
        if (!anterior.getEmail().equals(novo.getEmail())) campos.add("e-mail");
        if (!anterior.getTelefone().equals(novo.getTelefone())) campos.add("telefone");
        if (!anterior.getEndereco().equals(novo.getEndereco())) campos.add("endereço");
        if (!anterior.getResponsavel().equals(novo.getResponsavel())) campos.add("responsável legal");
        return campos.isEmpty() ? "Dados salvos sem alterações" : "Alterado: " + String.join(", ", campos);
    }

    private LocalDate hoje() {
        return LocalDate.now(relogio.withZone(ParametrosAcademia.FUSO));
    }

    private static String validarUsuario(String usuario) {
        Objects.requireNonNull(usuario);
        String normalizado = usuario.strip().toLowerCase(Locale.ROOT);
        Validate.matchesPattern(normalizado, "[a-z0-9][a-z0-9._-]{2,39}", USUARIO_INVALIDO);
        return normalizado;
    }

    private static void validarSenha(String senha) {
        Objects.requireNonNull(senha);
        Validate.isTrue(senha.length() >= 8 && senha.getBytes(StandardCharsets.UTF_8).length <= 72, SENHA_INVALIDA);
    }

    private boolean proprioAluno(Optional<AlunoIdentificador> identificador) {
        if (identificador.isEmpty() || !getSegurancaServico().possuiAutorizacao(Autorizacao.USUARIO.name())) return false;
        return alunos.buscarPorUsuario(getSegurancaServico().getUsuario()).getIdentificador().equals(identificador);
    }
}

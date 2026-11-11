package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno;

import java.util.Objects;
import java.util.Optional;
import java.util.Locale;
import java.nio.charset.StandardCharsets;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.IAlunoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.ICredencialRepositorio;
import dev.sauloaraujo.d5.framework.dominio.impl.*;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
public final class AlunoServico extends AgregadoServicoAbstrato<AlunoIdentificador, Aluno, AgregadoAutorizacoes<Aluno>> {
    private final IAlunoRepositorio alunos;
    private final ICredencialRepositorio credenciais;

    public AlunoServico(ISegurancaServico seguranca, IAlunoRepositorio repositorio, ICredencialRepositorio credenciais) {
        super(seguranca, repositorio, "Registro %s existente", "Registro %s inexistente");
        this.alunos = Objects.requireNonNull(repositorio);
        this.credenciais = Objects.requireNonNull(credenciais);
    }

    /** Cadastro público de aluno; a transação do POST inclui aluno e credencial. */
    public Aluno cadastrar(String nome, String usuario, String senha, String confirmacaoSenha) {
        Objects.requireNonNull(nome);
        Objects.requireNonNull(usuario);
        Objects.requireNonNull(senha);
        Objects.requireNonNull(confirmacaoSenha);
        String nomeNormalizado = nome.strip();
        String usuarioNormalizado = usuario.strip().toLowerCase(Locale.ROOT);
        Validate.isTrue(nomeNormalizado.length() >= 2 && nomeNormalizado.length() <= 100,
                "Informe um nome com 2 a 100 caracteres");
        Validate.matchesPattern(usuarioNormalizado, "[a-z0-9][a-z0-9._-]{2,39}",
                "O usuário deve ter de 3 a 40 caracteres: letras, números, ponto, hífen ou sublinhado");
        Validate.isTrue(senha.length() >= 8 && senha.getBytes(StandardCharsets.UTF_8).length <= 72,
                "A senha deve ter pelo menos 8 caracteres e no máximo 72 bytes");
        Validate.isTrue(senha.equals(confirmacaoSenha), "As senhas não conferem");
        Validate.validState(!alunos.existePorUsuario(usuarioNormalizado), "Este nome de usuário já está em uso");
        var aluno = alunos.incluir(new Aluno(Optional.empty(), usuarioNormalizado, nomeNormalizado));
        credenciais.incluir(usuarioNormalizado, senha);
        return aluno;
    }
    @Override public AgregadoAutorizacoes<Aluno> criarAutorizacoes(Optional<AlunoIdentificador> identificador) {
        Objects.requireNonNull(identificador);
        boolean personal = getSegurancaServico().possuiAutorizacao(Autorizacao.PERSONAL.name());
        return new AgregadoAutorizacoes<>(false, personal || proprioAluno(identificador), false, false);
    }
    private boolean proprioAluno(Optional<AlunoIdentificador> identificador) {
        if (identificador.isEmpty() || !getSegurancaServico().possuiAutorizacao(Autorizacao.USUARIO.name())) return false;
        return alunos.buscarPorUsuario(getSegurancaServico().getUsuario()).getIdentificador().equals(identificador);
    }
}

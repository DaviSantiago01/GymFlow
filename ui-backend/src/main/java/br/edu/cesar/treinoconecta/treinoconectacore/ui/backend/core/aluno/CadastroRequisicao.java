package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.aluno;

import java.util.Objects;

/** Não usa record/toString para não incluir senhas em representações de diagnóstico. */
public final class CadastroRequisicao {
    private final String nome;
    private final String usuario;
    private final String senha;
    private final String confirmacaoSenha;

    public CadastroRequisicao(String nome, String usuario, String senha, String confirmacaoSenha) {
        this.nome = Objects.requireNonNull(nome);
        this.usuario = Objects.requireNonNull(usuario);
        this.senha = Objects.requireNonNull(senha);
        this.confirmacaoSenha = Objects.requireNonNull(confirmacaoSenha);
    }
    public String getNome() { return nome; }
    public String getUsuario() { return usuario; }
    public String getSenha() { return senha; }
    public String getConfirmacaoSenha() { return confirmacaoSenha; }
}

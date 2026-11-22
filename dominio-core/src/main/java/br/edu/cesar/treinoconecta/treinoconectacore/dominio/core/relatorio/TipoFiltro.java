package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio;

public enum TipoFiltro {
    PLANO("plano contratado"), MODALIDADE("modalidade"), PERSONAL("personal"), AULA_COLETIVA("aula coletiva");

    private final String descricao;

    TipoFiltro(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() { return descricao; }
}

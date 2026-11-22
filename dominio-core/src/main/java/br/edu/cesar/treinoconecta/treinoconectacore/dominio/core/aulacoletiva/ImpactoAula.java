package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aulacoletiva;

public enum ImpactoAula {
    BAIXO("Baixo impacto"), MODERADO("Impacto moderado"), ALTO("Alto impacto");

    private final String descricao;

    ImpactoAula(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() { return descricao; }
}

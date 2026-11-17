package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade;

/** ATRASADA não é gravada: é calculada a partir do vencimento de uma mensalidade ABERTA. */
public enum SituacaoMensalidade {
    ABERTA, ATRASADA, PAGA
}

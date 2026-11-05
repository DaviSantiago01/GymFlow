package dev.sauloaraujo.d5.framework.aplicacao;

public interface IAgregadoAplicacaoServico<P extends IProjecao<?>, C extends IConsulta<? extends IOrdenacao<P>>> {
	IConsultaResultado<P> pesquisar(C consulta);
}
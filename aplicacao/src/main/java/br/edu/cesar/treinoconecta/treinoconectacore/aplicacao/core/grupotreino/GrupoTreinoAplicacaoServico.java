package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.grupotreino;

import java.util.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.grupotreino.spi.IGrupoTreinoAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.grupotreino.DadosGrupos.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.grupotreino.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import dev.sauloaraujo.d5.framework.aplicacao.impl.AgregadoAplicacaoServicoAbstrato;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;
public final class GrupoTreinoAplicacaoServico extends AgregadoAplicacaoServicoAbstrato<GrupoTreinoIdentificador,GrupoTreino,AgregadoAutorizacoes<GrupoTreino>,GrupoTreinoProjecao,GrupoTreinoConsulta> {
    private final IGrupoTreinoAplicacaoRepositorio consultas;private final GrupoTreinoServico servico;
    public GrupoTreinoAplicacaoServico(IGrupoTreinoAplicacaoRepositorio consultas,GrupoTreinoServico servico){super(consultas,servico);this.consultas=Objects.requireNonNull(consultas);this.servico=Objects.requireNonNull(servico);}
    private AlunoIdentificador aluno(){return servico.autenticado().getIdentificador().orElseThrow();}
    public Contexto contexto(){return consultas.contexto(aluno());}
    public Pagina<AlunoOpcao> alunos(String busca,int pagina,Optional<GrupoTreinoIdentificador> grupo){return consultas.alunos(aluno(),busca,pagina,grupo);}
    public GrupoDetalhe detalhe(GrupoTreinoIdentificador id,boolean historico,int pagina){return consultas.detalhe(id,aluno(),historico,pagina);}
    public TreinoDetalhe treino(GrupoTreinoIdentificador id,long treino){return consultas.treino(id,treino,aluno());}
    public Pagina<ConviteEntrada> convites(int pagina,boolean historico){return consultas.convites(aluno(),pagina,historico);}
    public Pagina<TreinoResumo> convitesTreino(int pagina,boolean historico){return consultas.convitesTreino(aluno(),pagina,historico);}
}

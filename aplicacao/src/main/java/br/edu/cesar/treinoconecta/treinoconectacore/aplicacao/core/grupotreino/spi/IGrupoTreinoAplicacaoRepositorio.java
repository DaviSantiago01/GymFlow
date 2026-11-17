package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.grupotreino.spi;

import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.grupotreino.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.grupotreino.DadosGrupos.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.grupotreino.GrupoTreinoIdentificador;
import dev.sauloaraujo.d5.framework.aplicacao.spi.IAgregadoAplicacaoRepositorio;
import java.util.Optional;
public interface IGrupoTreinoAplicacaoRepositorio extends IAgregadoAplicacaoRepositorio<GrupoTreinoProjecao,GrupoTreinoConsulta> {
    Contexto contexto(AlunoIdentificador aluno);
    Pagina<AlunoOpcao> alunos(AlunoIdentificador aluno,String busca,int pagina,Optional<GrupoTreinoIdentificador> grupo);
    GrupoDetalhe detalhe(GrupoTreinoIdentificador grupo,AlunoIdentificador aluno,boolean historico,int pagina);
    TreinoDetalhe treino(GrupoTreinoIdentificador grupo,long treino,AlunoIdentificador aluno);
    Pagina<ConviteEntrada> convites(AlunoIdentificador aluno,int pagina,boolean historico);
    Pagina<TreinoResumo> convitesTreino(AlunoIdentificador aluno,int pagina,boolean historico);
}

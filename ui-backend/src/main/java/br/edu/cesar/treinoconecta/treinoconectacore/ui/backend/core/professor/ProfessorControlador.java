package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.professor;

import java.util.List;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.professor.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.professor.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaoprofessor.*;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;
import dev.sauloaraujo.d5.framework.ui.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping(Backend.PREFIXO+"core/professor")
public class ProfessorControlador extends AgregadoControladorAbstrato<ProfessorIdentificador,Professor,AgregadoAutorizacoes<Professor>,ProfessorProjecao,ProfessorConsulta>{
    private final ProfessorAplicacaoServico consultas;private final AvaliacaoProfessorAplicacaoServico avaliacoes;
    public ProfessorControlador(ProfessorServico servico,ProfessorAplicacaoServico consultas,AvaliacaoProfessorAplicacaoServico avaliacoes){super(servico,consultas);this.consultas=consultas;this.avaliacoes=avaliacoes;}
    @GetMapping("{identificador}/visualizacao") public PerfilProfessor perfil(@PathVariable ProfessorIdentificador identificador){return consultas.perfil(identificador);}
    @GetMapping("{identificador}/estatistica") public EstatisticasProfessor estatistica(@PathVariable ProfessorIdentificador identificador){return consultas.detalhe(identificador).estatisticas();}
    @GetMapping("{identificador}/recentes") public List<AvaliacaoProfessorProjecao> recentes(@PathVariable ProfessorIdentificador identificador){consultas.detalhe(identificador);return avaliacoes.recentes(identificador);}
}

package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.treino;

import java.util.Map;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.treino.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaoprofessor.AvaliacaoProfessorServico;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.treino.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaoprofessor.*;
import br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.avaliacaoprofessor.AvaliacaoRequisicao;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;
import dev.sauloaraujo.d5.framework.ui.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
@RestController @RequestMapping(Backend.PREFIXO+"core/treino")
public class TreinoControlador extends AgregadoControladorAbstrato<TreinoIdentificador,Treino,AgregadoAutorizacoes<Treino>,TreinoProjecao,TreinoConsulta>{
    private final TreinoAplicacaoServico consultas;private final AvaliacaoProfessorServico avaliacoes;private final AvaliacaoProfessorAplicacaoServico consultasAvaliacoes;
    public TreinoControlador(TreinoServico servico,TreinoAplicacaoServico consultas,AvaliacaoProfessorServico avaliacoes,AvaliacaoProfessorAplicacaoServico consultasAvaliacoes){super(servico,consultas);this.consultas=consultas;this.avaliacoes=avaliacoes;this.consultasAvaliacoes=consultasAvaliacoes;}
    @GetMapping({"{identificador}/visualizacao","{identificador}/avaliacao"}) public TreinoProjecao detalhe(@PathVariable TreinoIdentificador identificador){return consultas.detalhe(identificador);}
    @GetMapping("{identificador}/minha-avaliacao") public MinhaAvaliacao minha(@PathVariable TreinoIdentificador identificador){consultas.detalhe(identificador);return consultasAvaliacoes.minha(identificador);}
    @PostMapping("{identificador}/avaliar") public AvaliacaoProfessorProjecao avaliar(@PathVariable TreinoIdentificador identificador,@Valid @RequestBody AvaliacaoRequisicao requisicao){var a=avaliacoes.avaliar(identificador,requisicao.notas(),requisicao.comentarioOpcional());return consultasAvaliacoes.detalhe(a.getIdentificador().orElseThrow());}
    @GetMapping("demonstracao") public Map<String,Boolean> demonstracao(){return Map.of("disponivel",consultas.demonstracaoDisponivel());}
    @PostMapping("demonstracao") @ResponseStatus(HttpStatus.NO_CONTENT) public void carregarDemonstracao(){consultas.carregarDemonstracao();}
}

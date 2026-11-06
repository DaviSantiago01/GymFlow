package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.avaliacaoprofessor;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaoprofessor.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaoprofessor.*;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;
import dev.sauloaraujo.d5.framework.ui.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
@RestController @RequestMapping(Backend.PREFIXO+"core/avaliacao-professor")
public class AvaliacaoProfessorControlador extends AgregadoControladorAbstrato<AvaliacaoProfessorIdentificador,AvaliacaoProfessor,AgregadoAutorizacoes<AvaliacaoProfessor>,AvaliacaoProfessorProjecao,AvaliacaoProfessorConsulta> {
    private final AvaliacaoProfessorServico comandos;
    private final AvaliacaoProfessorAplicacaoServico consultas;
    public AvaliacaoProfessorControlador(AvaliacaoProfessorServico comandos,AvaliacaoProfessorAplicacaoServico consultas) { super(comandos,consultas);this.comandos=comandos;this.consultas=consultas; }
    @GetMapping({"{identificador}/visualizacao","{identificador}/edicao"})
    public AvaliacaoProfessorProjecao detalhe(@PathVariable AvaliacaoProfessorIdentificador identificador) { return consultas.detalhe(identificador); }
    @PostMapping("{identificador}/editar")
    public AvaliacaoProfessorProjecao editar(@PathVariable AvaliacaoProfessorIdentificador identificador,@Valid @RequestBody AvaliacaoRequisicao requisicao) {
        comandos.editar(identificador,requisicao.notas(),requisicao.comentarioOpcional());return consultas.detalhe(identificador);
    }
    @PostMapping("{identificador}/remover") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable AvaliacaoProfessorIdentificador identificador) { comandos.remover(identificador); }
}

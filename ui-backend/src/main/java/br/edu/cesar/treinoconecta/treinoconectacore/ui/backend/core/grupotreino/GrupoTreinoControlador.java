package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.grupotreino;

import java.time.Instant;
import java.util.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.grupotreino.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.grupotreino.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.grupotreino.DadosGrupos.*;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;
import dev.sauloaraujo.d5.framework.ui.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
@RestController @RequestMapping(Backend.PREFIXO+"core/grupo-treino")
public class GrupoTreinoControlador extends AgregadoControladorAbstrato<GrupoTreinoIdentificador,GrupoTreino,AgregadoAutorizacoes<GrupoTreino>,GrupoTreinoProjecao,GrupoTreinoConsulta> {
    private final GrupoTreinoServico comandos;private final GrupoTreinoAplicacaoServico consultas;
    public GrupoTreinoControlador(GrupoTreinoServico comandos,GrupoTreinoAplicacaoServico consultas){super(comandos,consultas);this.comandos=comandos;this.consultas=consultas;}
    public record Criacao(@NotNull String nome,Optional<@Size(max=50) List<@NotNull AlunoIdentificador>> convidados){}
    public record Nome(@NotNull String nome){}
    public record Destinatario(@NotNull AlunoIdentificador aluno){}
    public record Agenda(@NotNull Instant inicio){}
    @GetMapping("criacao") public Contexto contexto(){return consultas.contexto();}
    @GetMapping("alunos") public Pagina<AlunoOpcao> alunos(@RequestParam(defaultValue="")String busca,@RequestParam(defaultValue="0")int pagina,@RequestParam Optional<GrupoTreinoIdentificador> grupo){return consultas.alunos(busca,pagina,grupo);}
    @PostMapping("criar") public Map<String,String> criar(@Valid @RequestBody Criacao r){return Map.of("identificador",comandos.criar(new NomeGrupo(r.nome()),r.convidados().orElse(List.of())).getIdentificador().orElseThrow().toString());}
    @GetMapping({"{identificador}/visualizacao","{identificador}/agenda"}) public GrupoDetalhe detalhe(@PathVariable GrupoTreinoIdentificador identificador,@RequestParam(defaultValue="false")boolean historico,@RequestParam(defaultValue="0")int pagina){return consultas.detalhe(identificador,historico,pagina);}
    @GetMapping("{identificador}/historico") public GrupoDetalhe historicoGrupo(@PathVariable GrupoTreinoIdentificador identificador,@RequestParam(defaultValue="0")int pagina){return consultas.detalhe(identificador,true,pagina);}
    @GetMapping("{identificador}/membros") public List<Membro> membros(@PathVariable GrupoTreinoIdentificador identificador){return consultas.detalhe(identificador,false,0).membros();}
    @PostMapping("{identificador}/renomear") @ResponseStatus(HttpStatus.NO_CONTENT) public void renomear(@PathVariable GrupoTreinoIdentificador identificador,@Valid @RequestBody Nome r){comandos.renomear(identificador,new NomeGrupo(r.nome()));}
    @PostMapping("{identificador}/convidar") @ResponseStatus(HttpStatus.NO_CONTENT) public void convidar(@PathVariable GrupoTreinoIdentificador identificador,@Valid @RequestBody Destinatario r){comandos.convidar(identificador,r.aluno());}
    @GetMapping("convites") public Pagina<ConviteEntrada> convites(@RequestParam(defaultValue="0")int pagina,@RequestParam(defaultValue="false")boolean historico){return consultas.convites(pagina,historico);}
    @PostMapping("{identificador}/convites/{convite}/aceitar") @ResponseStatus(HttpStatus.NO_CONTENT) public void aceitar(@PathVariable GrupoTreinoIdentificador identificador,@PathVariable long convite){comandos.responderGrupo(identificador,convite,true);}
    @PostMapping("{identificador}/convites/{convite}/recusar") @ResponseStatus(HttpStatus.NO_CONTENT) public void recusar(@PathVariable GrupoTreinoIdentificador identificador,@PathVariable long convite){comandos.responderGrupo(identificador,convite,false);}
    @PostMapping("{identificador}/sair") @ResponseStatus(HttpStatus.NO_CONTENT) public void sair(@PathVariable GrupoTreinoIdentificador identificador){comandos.sair(identificador);}
    @PostMapping("{identificador}/encerrar") @ResponseStatus(HttpStatus.NO_CONTENT) public void encerrar(@PathVariable GrupoTreinoIdentificador identificador){comandos.encerrar(identificador);}
    @PostMapping("{identificador}/propor") @ResponseStatus(HttpStatus.NO_CONTENT) public void propor(@PathVariable GrupoTreinoIdentificador identificador,@Valid @RequestBody Agenda r){comandos.propor(identificador,r.inicio());}
    @GetMapping({"{identificador}/treinos/{treino}/visualizacao","{identificador}/treinos/{treino}/convite"}) public TreinoDetalhe treino(@PathVariable GrupoTreinoIdentificador identificador,@PathVariable long treino){return consultas.treino(identificador,treino);}
    @GetMapping("{identificador}/treinos/{treino}/confirmados") public List<Presenca> confirmados(@PathVariable GrupoTreinoIdentificador identificador,@PathVariable long treino){return consultas.treino(identificador,treino).presencas().stream().filter(p->p.situacao().equals("CONFIRMADO")).toList();}
    @GetMapping("convites-treino") public Pagina<TreinoResumo> convitesTreino(@RequestParam(defaultValue="0")int pagina,@RequestParam(defaultValue="false")boolean historico){return consultas.convitesTreino(pagina,historico);}
    @GetMapping("historico") public Pagina<TreinoResumo> historico(@RequestParam(defaultValue="0")int pagina){return consultas.convitesTreino(pagina,true);}
    @PostMapping("{identificador}/treinos/{treino}/cancelar") @ResponseStatus(HttpStatus.NO_CONTENT) public void cancelarTreino(@PathVariable GrupoTreinoIdentificador identificador,@PathVariable long treino){comandos.cancelarTreino(identificador,treino);}
    @PostMapping("{identificador}/treinos/{treino}/convites/{convite}/confirmar") @ResponseStatus(HttpStatus.NO_CONTENT) public void confirmar(@PathVariable GrupoTreinoIdentificador identificador,@PathVariable long treino,@PathVariable long convite){comandos.responderTreino(identificador,treino,convite,true);}
    @PostMapping("{identificador}/treinos/{treino}/convites/{convite}/recusar") @ResponseStatus(HttpStatus.NO_CONTENT) public void recusarTreino(@PathVariable GrupoTreinoIdentificador identificador,@PathVariable long treino,@PathVariable long convite){comandos.responderTreino(identificador,treino,convite,false);}
    @PostMapping("{identificador}/treinos/{treino}/convites/{convite}/cancelar") @ResponseStatus(HttpStatus.NO_CONTENT) public void cancelarPresenca(@PathVariable GrupoTreinoIdentificador identificador,@PathVariable long treino,@PathVariable long convite){comandos.cancelarPresenca(identificador,treino,convite);}
}

package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.aulasdesafios;

import java.math.BigDecimal;import java.time.*;import java.util.*;import org.jspecify.annotations.Nullable;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aula.AulaAplicacaoServico;import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aulasdesafios.AulasDesafiosDados;import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.desafio.DesafioAplicacaoServico;import dev.sauloaraujo.d5.framework.ui.Backend;
import jakarta.validation.Valid;import jakarta.validation.constraints.*;import org.springframework.format.annotation.DateTimeFormat;import org.springframework.web.bind.annotation.*;

/** Adaptador HTTP; comandos e consultas são delegados aos serviços de aplicação dos respectivos domínios. */
@RestController @RequestMapping(Backend.PREFIXO+"core/aulas-desafios")
public class AulasDesafiosControlador {
 private final AulaAplicacaoServico aulas;private final DesafioAplicacaoServico desafios;
 public AulasDesafiosControlador(AulaAplicacaoServico aulas,DesafioAplicacaoServico desafios){this.aulas=Objects.requireNonNull(aulas);this.desafios=Objects.requireNonNull(desafios);}
 @GetMapping("professores") public List<AulasDesafiosDados.PessoaLinha> professores(){return aulas.professores();}
 @GetMapping("alunos") public List<AulasDesafiosDados.PessoaLinha> alunos(){return desafios.alunos();}
 @GetMapping("aulas") public List<AulasDesafiosDados.AulaLinha> aulas(@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) Optional<LocalDate> data,@RequestParam Optional<String> modalidade,@RequestParam Optional<String> busca){return aulas.aulas(data,modalidade,busca);}
 @GetMapping("aulas/{id}") public AulasDesafiosDados.AulaLinha aula(@PathVariable long id){return aulas.aula(id);}
 @PostMapping("aulas") public AulasDesafiosDados.AulaLinha cadastrarAula(@Valid @RequestBody AulaRequisicao r){return aulas.cadastrar(r.titulo(),r.modalidade(),Optional.ofNullable(r.descricao()).filter(x->!x.isBlank()),r.data(),r.inicio(),r.fim(),r.limite(),r.professor());}
 @PutMapping("aulas/{id}") public AulasDesafiosDados.AulaLinha editarAula(@PathVariable long id,@Valid @RequestBody AulaRequisicao r){return aulas.editar(id,r.titulo(),r.modalidade(),Optional.ofNullable(r.descricao()).filter(x->!x.isBlank()),r.data(),r.inicio(),r.fim(),r.limite(),r.professor());}
 @PostMapping("aulas/{id}/cancelar") public AulasDesafiosDados.AulaLinha cancelarAula(@PathVariable long id){return aulas.cancelar(id);}
 @PostMapping("aulas/{id}/inscricao") public void inscreverAula(@PathVariable long id){aulas.inscrever(id);}
 @GetMapping("meus-agendamentos") public List<AulasDesafiosDados.InscricaoLinha> minhasAulas(){return aulas.minhasAulas();}
 @PostMapping("inscricoes/{id}/cancelar") public void cancelarInscricao(@PathVariable long id){aulas.cancelarInscricao(id);}
 @PostMapping("inscricoes/{id}/cancelar/gestao") public void cancelarInscricaoGestao(@PathVariable long id){aulas.cancelarInscricaoGestao(id);}
 @GetMapping("aulas/{id}/inscritos") public List<AulasDesafiosDados.InscricaoLinha> inscritos(@PathVariable long id){return aulas.inscritos(id);}
 @GetMapping("notificacoes") public List<AulasDesafiosDados.NotificacaoLinha> notificacoes(){return aulas.notificacoes();}
 @GetMapping("desafios") public List<AulasDesafiosDados.DesafioLinha> desafios(){return desafios.desafios();}
 @PostMapping("desafios") public AulasDesafiosDados.DesafioLinha criarDesafio(@Valid @RequestBody DesafioRequisicao r){return desafios.criar(r.titulo(),r.indicador(),r.inicio(),r.fim());}
 @GetMapping("desafios/{id}/avaliacoes") public List<AulasDesafiosDados.AvaliacaoLinha> avaliacoes(@PathVariable long id){return desafios.avaliacoes(id);}
 @PostMapping("avaliacoes") public void registrarAvaliacao(@Valid @RequestBody AvaliacaoRequisicao r){desafios.registrarAvaliacao(r.desafio(),r.aluno(),r.profissional(),r.tipo(),r.peso(),r.cintura(),r.gordura());}
 @PutMapping("avaliacoes/{id}") public void editarAvaliacao(@PathVariable long id,@Valid @RequestBody AvaliacaoRequisicao r){desafios.editarAvaliacao(id,r.peso(),r.cintura(),r.gordura());}
 @DeleteMapping("avaliacoes/{id}") public void excluirAvaliacao(@PathVariable long id){desafios.excluirAvaliacao(id);}
 @PostMapping("desafios/inscricao") public void inscreverDesafio(@Valid @RequestBody InscricaoDesafioRequisicao r){desafios.inscrever(r.desafio(),r.indicador(),r.meta());}
 @GetMapping("desafios/{id}/evolucao") public List<AulasDesafiosDados.EvolucaoLinha> evolucao(@PathVariable long id){return desafios.evolucao(id);}
 @PostMapping("desafios/{id}/encerrar") public void encerrarDesafio(@PathVariable long id){desafios.encerrar(id);}
 public record AulaRequisicao(@NotBlank @Size(max=100) String titulo,@NotBlank @Size(max=60) String modalidade,@Nullable @Size(max=500) String descricao,@NotNull LocalDate data,@NotNull LocalTime inicio,@NotNull LocalTime fim,@Positive int limite,@Positive long professor){}
 public record DesafioRequisicao(@NotBlank @Size(max=100) String titulo,@NotBlank String indicador,@NotNull LocalDate inicio,@NotNull LocalDate fim){}
 public record AvaliacaoRequisicao(@Positive long desafio,@Positive long aluno,@Positive long profissional,@NotBlank String tipo,@NotNull @Positive BigDecimal peso,@NotNull @Positive BigDecimal cintura,@NotNull @Positive BigDecimal gordura){}
 public record InscricaoDesafioRequisicao(@Positive long desafio,@NotBlank String indicador,@NotNull @Positive BigDecimal meta){}
}

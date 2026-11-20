package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.aluno;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.AlunoAplicacaoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.AlunoConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.AlunoFicha;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.AlunoGestaoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.AlunoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.Aluno;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoAutorizacoes;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.DadosAluno;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.Email;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.ResponsavelLegal;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.SituacaoAluno;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.Cpf;
import dev.sauloaraujo.d5.framework.ui.AgregadoControladorAbstrato;
import dev.sauloaraujo.d5.framework.ui.Backend;

/** Pesquisa usada pelos personais e gerenciamento de alunos pela administração (F005). */
@RestController
@RequestMapping(Backend.PREFIXO + "core/aluno")
public class AlunoControlador extends
        AgregadoControladorAbstrato<AlunoIdentificador, Aluno, AlunoAutorizacoes, AlunoProjecao, AlunoConsulta> {
    private final AlunoServico servico;
    private final AlunoAplicacaoServico consultas;

    public AlunoControlador(AlunoServico servico, AlunoAplicacaoServico consultas) {
        super(servico, consultas);
        this.servico = Objects.requireNonNull(servico);
        this.consultas = Objects.requireNonNull(consultas);
    }

    @GetMapping("gestao")
    public List<AlunoGestaoProjecao> gestao(@RequestParam Optional<String> busca,
            @RequestParam Optional<SituacaoAluno> situacao) {
        return consultas.pesquisarGestao(busca, situacao);
    }

    @GetMapping("{identificador}/ficha")
    public AlunoFicha ficha(@PathVariable AlunoIdentificador identificador) {
        return consultas.ficha(identificador);
    }

    @PostMapping("gestao/cadastrar")
    public AlunoFicha cadastrar(@RequestBody AlunoRequisicao requisicao) {
        var aluno = servico.cadastrarPelaAdministracao(dados(requisicao), texto(requisicao.usuario()),
                texto(requisicao.senha()));
        return consultas.ficha(aluno.getIdentificador().orElseThrow());
    }

    @PostMapping("{identificador}/editar")
    public AlunoFicha editar(@PathVariable AlunoIdentificador identificador, @RequestBody AlunoRequisicao requisicao) {
        servico.editar(identificador, dados(requisicao));
        return consultas.ficha(identificador);
    }

    @PostMapping("{identificador}/bloquear")
    public AlunoFicha bloquear(@PathVariable AlunoIdentificador identificador, @RequestBody Map<String, String> corpo) {
        servico.bloquear(identificador, Objects.requireNonNullElse(corpo.get("motivo"), ""));
        return consultas.ficha(identificador);
    }

    @PostMapping("{identificador}/desbloquear")
    public AlunoFicha desbloquear(@PathVariable AlunoIdentificador identificador) {
        servico.desbloquear(identificador);
        return consultas.ficha(identificador);
    }

    @PostMapping("{identificador}/reativar")
    public AlunoFicha reativar(@PathVariable AlunoIdentificador identificador) {
        servico.reativar(identificador);
        return consultas.ficha(identificador);
    }

    /** RN010: devolve EXCLUIDO ou DESATIVADO. */
    @PostMapping("{identificador}/remover")
    public Map<String, String> remover(@PathVariable AlunoIdentificador identificador) {
        return Map.of("resultado", servico.remover(identificador).name());
    }

    private static DadosAluno dados(AlunoRequisicao requisicao) {
        Optional<ResponsavelLegal> responsavel = Optional.empty();
        if (!texto(requisicao.responsavelNome()).isBlank() || !texto(requisicao.responsavelCpf()).isBlank()) {
            responsavel = Optional.of(new ResponsavelLegal(texto(requisicao.responsavelNome()),
                    new Cpf(texto(requisicao.responsavelCpf())), texto(requisicao.responsavelTelefone())));
        }
        var email = Optional.of(texto(requisicao.email())).filter(valor -> !valor.isBlank()).map(Email::new);
        return new DadosAluno(texto(requisicao.nome()), new Cpf(texto(requisicao.cpf())),
                Objects.requireNonNull(requisicao.nascimento(), DadosAluno.NASCIMENTO_OBRIGATORIO), email,
                texto(requisicao.telefone()), texto(requisicao.endereco()), responsavel);
    }

    private static String texto(@Nullable String valor) {
        return Objects.requireNonNullElse(valor, "");
    }
}

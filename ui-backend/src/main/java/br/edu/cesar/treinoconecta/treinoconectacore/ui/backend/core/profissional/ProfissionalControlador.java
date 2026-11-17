package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.profissional;

import java.util.Objects;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.profissional.ProfissionalAplicacaoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.profissional.ProfissionalConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.profissional.ProfissionalDetalhe;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.profissional.ProfissionalProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.ClassificacaoProfissional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.Cpf;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.Cref;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.Especialidade;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.Profissional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.ProfissionalAutorizacoes;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.ProfissionalIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.ProfissionalServico;
import dev.sauloaraujo.d5.framework.ui.AgregadoControladorAbstrato;
import dev.sauloaraujo.d5.framework.ui.Backend;

/** Não há rota de exclusão: profissionais são inativados para preservar o histórico. */
@RestController
@RequestMapping(Backend.PREFIXO + "core/profissional")
public class ProfissionalControlador extends AgregadoControladorAbstrato<ProfissionalIdentificador, Profissional,
        ProfissionalAutorizacoes, ProfissionalProjecao, ProfissionalConsulta> {
    private final ProfissionalServico servico;
    private final ProfissionalAplicacaoServico consultas;

    public ProfissionalControlador(ProfissionalServico servico, ProfissionalAplicacaoServico consultas) {
        super(servico, consultas);
        this.servico = Objects.requireNonNull(servico);
        this.consultas = Objects.requireNonNull(consultas);
    }

    @GetMapping("{identificador}/visualizacao")
    public ProfissionalDetalhe visualizacao(@PathVariable ProfissionalIdentificador identificador) {
        return consultas.detalhe(identificador);
    }

    @PostMapping("cadastrar")
    public ProfissionalDetalhe cadastrar(@RequestBody ProfissionalRequisicao requisicao) {
        var profissional = servico.cadastrar(texto(requisicao.nome()), new Cpf(texto(requisicao.cpf())),
                new Cref(texto(requisicao.cref())), texto(requisicao.contato()), classificacoes(requisicao.classificacoes()));
        return consultas.detalhe(profissional.getIdentificador().orElseThrow());
    }

    @PostMapping("{identificador}/editar")
    public ProfissionalDetalhe editar(@PathVariable ProfissionalIdentificador identificador,
            @RequestBody ProfissionalRequisicao requisicao) {
        servico.editar(identificador, texto(requisicao.nome()), new Cpf(texto(requisicao.cpf())),
                new Cref(texto(requisicao.cref())), texto(requisicao.contato()), classificacoes(requisicao.classificacoes()));
        return consultas.detalhe(identificador);
    }

    @PostMapping("{identificador}/especialidades/adicionar")
    public ProfissionalDetalhe adicionarEspecialidade(@PathVariable ProfissionalIdentificador identificador,
            @RequestBody EspecialidadeRequisicao requisicao) {
        servico.adicionarEspecialidade(identificador, new Especialidade(texto(requisicao.nome())));
        return consultas.detalhe(identificador);
    }

    @PostMapping("{identificador}/especialidades/remover")
    public ProfissionalDetalhe removerEspecialidade(@PathVariable ProfissionalIdentificador identificador,
            @RequestBody EspecialidadeRequisicao requisicao) {
        servico.removerEspecialidade(identificador, new Especialidade(texto(requisicao.nome())));
        return consultas.detalhe(identificador);
    }

    @PostMapping("{identificador}/disponibilidades/adicionar")
    public ProfissionalDetalhe adicionarDisponibilidade(@PathVariable ProfissionalIdentificador identificador,
            @RequestBody DisponibilidadeRequisicao requisicao) {
        servico.adicionarDisponibilidade(identificador, obrigatorio(requisicao.dia()), obrigatorio(requisicao.inicio()),
                obrigatorio(requisicao.fim()));
        return consultas.detalhe(identificador);
    }

    @PostMapping("{identificador}/disponibilidades/{disponibilidade}/alterar")
    public ProfissionalDetalhe alterarDisponibilidade(@PathVariable ProfissionalIdentificador identificador,
            @PathVariable long disponibilidade, @RequestBody DisponibilidadeRequisicao requisicao) {
        servico.alterarDisponibilidade(identificador, disponibilidade, obrigatorio(requisicao.dia()),
                obrigatorio(requisicao.inicio()), obrigatorio(requisicao.fim()));
        return consultas.detalhe(identificador);
    }

    @PostMapping("{identificador}/disponibilidades/{disponibilidade}/remover")
    public ProfissionalDetalhe removerDisponibilidade(@PathVariable ProfissionalIdentificador identificador,
            @PathVariable long disponibilidade) {
        servico.removerDisponibilidade(identificador, disponibilidade);
        return consultas.detalhe(identificador);
    }

    @PostMapping("{identificador}/inativar")
    public ProfissionalDetalhe inativar(@PathVariable ProfissionalIdentificador identificador) {
        servico.inativar(identificador);
        return consultas.detalhe(identificador);
    }

    @PostMapping("{identificador}/reativar")
    public ProfissionalDetalhe reativar(@PathVariable ProfissionalIdentificador identificador) {
        servico.reativar(identificador);
        return consultas.detalhe(identificador);
    }

    private static String texto(@Nullable String valor) {
        return Objects.requireNonNullElse(valor, "");
    }

    private static <T> T obrigatorio(@Nullable T valor) {
        return Objects.requireNonNull(valor, ProfissionalServico.DISPONIBILIDADE_INCOMPLETA);
    }

    private static Set<ClassificacaoProfissional> classificacoes(@Nullable Set<ClassificacaoProfissional> valor) {
        return Objects.requireNonNullElse(valor, Set.of());
    }
}

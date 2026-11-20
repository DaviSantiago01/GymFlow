package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.registroacesso;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.registroacesso.FrequenciaAluno;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.registroacesso.PainelPresenca;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.registroacesso.RegistroAcessoAplicacaoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.registroacesso.RegistroAcessoConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.registroacesso.RegistroAcessoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroacesso.RegistroAcesso;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroacesso.RegistroAcessoAutorizacoes;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroacesso.RegistroAcessoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroacesso.RegistroAcessoServico;
import dev.sauloaraujo.d5.framework.ui.AgregadoControladorAbstrato;
import dev.sauloaraujo.d5.framework.ui.Backend;

/** Recepção da academia: entradas, saídas, presentes e frequência (F006). */
@RestController
@RequestMapping(Backend.PREFIXO + "core/registro-acesso")
public class RegistroAcessoControlador extends AgregadoControladorAbstrato<RegistroAcessoIdentificador, RegistroAcesso,
        RegistroAcessoAutorizacoes, RegistroAcessoProjecao, RegistroAcessoConsulta> {
    private final RegistroAcessoServico servico;
    private final RegistroAcessoAplicacaoServico consultas;

    public RegistroAcessoControlador(RegistroAcessoServico servico, RegistroAcessoAplicacaoServico consultas) {
        super(servico, consultas);
        this.servico = Objects.requireNonNull(servico);
        this.consultas = Objects.requireNonNull(consultas);
    }

    @PostMapping("aluno/{aluno}/entrada")
    public PainelPresenca entrada(@PathVariable AlunoIdentificador aluno) {
        servico.registrarEntrada(aluno);
        return consultas.presentes();
    }

    @PostMapping("aluno/{aluno}/saida")
    public PainelPresenca saida(@PathVariable AlunoIdentificador aluno) {
        servico.registrarSaida(aluno);
        return consultas.presentes();
    }

    @GetMapping("presentes")
    public PainelPresenca presentes() {
        return consultas.presentes();
    }

    @GetMapping("aluno/{aluno}/frequencia")
    public FrequenciaAluno frequencia(@PathVariable AlunoIdentificador aluno,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> fim) {
        return consultas.frequencia(aluno, inicio, fim);
    }
}

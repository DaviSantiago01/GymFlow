package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.seguranca;

import java.util.Map;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoServico;
import dev.sauloaraujo.d5.framework.ui.Backend;

@RestController
@RequestMapping(Backend.PREFIXO + "cadastro")
public class CadastroControlador {
    private final AlunoServico alunos;
    public CadastroControlador(AlunoServico alunos) { this.alunos = Objects.requireNonNull(alunos); }

    @GetMapping
    public Map<String, String> formulario() {
        // O filtro CSRF do D5 emite o cookie também para visitantes anônimos.
        return Map.of("mensagem", "Crie sua conta de aluno");
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> cadastrar(@RequestBody CadastroRequisicao requisicao) {
        Objects.requireNonNull(requisicao);
        var aluno = alunos.cadastrar(requisicao.getNome(), requisicao.getUsuario(),
                requisicao.getSenha(), requisicao.getConfirmacaoSenha());
        return Map.of("usuario", aluno.getUsuario(), "nome", aluno.getNome());
    }
}

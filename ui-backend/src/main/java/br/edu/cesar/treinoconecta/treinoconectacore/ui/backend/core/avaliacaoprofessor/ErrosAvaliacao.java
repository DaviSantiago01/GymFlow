package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.avaliacaoprofessor;

import java.util.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.professor.ProfessorControlador;
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes={AvaliacaoProfessorControlador.class,ProfessorControlador.class,br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.treino.TreinoControlador.class})
public class ErrosAvaliacao {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Erro> validar(MethodArgumentNotValidException erro) {
        Map<String,String> campos=new LinkedHashMap<>();
        for(var campo:erro.getBindingResult().getFieldErrors()) campos.putIfAbsent(campo.getField(),
            campo.getField().equals("comentario")?"O comentário deve ter no máximo 500 caracteres":"Informe uma nota inteira de 0 a 5");
        return ResponseEntity.badRequest().body(new Erro("Revise os campos indicados",campos));
    }
    public record Erro(String mensagem,Map<String,String> campos) { }
}

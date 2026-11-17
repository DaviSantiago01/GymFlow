package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.professor;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.professor.Professor;
import dev.sauloaraujo.d5.framework.aplicacao.IProjecao;
public record ProfessorProjecao(String identificador,String nome,String especialidade,String biografia,boolean demonstracao,
        EstatisticasProfessor estatisticas) implements IProjecao<Professor> {
    public ProfessorProjecao(Long id,String nome,String especialidade,String biografia,boolean demonstracao,Long quantidade,
            Long auxilio,Long pontualidade,Long postura,Long didatica,Long adequacao) {
        this(id.toString(),nome,especialidade,biografia,demonstracao,EstatisticasProfessor.calcular(quantidade,auxilio,pontualidade,postura,didatica,adequacao));
    }
}

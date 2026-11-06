package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaoprofessor;

import org.apache.commons.lang3.Validate;
import dev.sauloaraujo.d5.framework.dominio.IObjetoValor;
public record NotasAvaliacao(int auxilio, int pontualidade, int postura, int didatica, int adequacao) implements IObjetoValor {
    public NotasAvaliacao {
        for(int nota:new int[]{auxilio,pontualidade,postura,didatica,adequacao})
            Validate.isTrue(nota>=0 && nota<=5,"Todas as cinco notas devem ser inteiras entre 0 e 5");
    }
}

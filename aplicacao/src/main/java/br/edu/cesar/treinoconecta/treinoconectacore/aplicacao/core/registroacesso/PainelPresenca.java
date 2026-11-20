package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.registroacesso;

import java.time.LocalTime;
import java.util.List;

/** Alunos presentes na academia agora, com a lotação em relação à capacidade máxima (F006, RN010). */
public record PainelPresenca(int capacidadeMaxima, int presentes, long permanenciaMaximaMinutos, LocalTime abertura,
        LocalTime fechamento, List<PermanenciaRegistro> registros) { }

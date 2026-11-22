package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aula.spi;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aula.Aula;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aula.AulaIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.ProfissionalIdentificador;
import dev.sauloaraujo.d5.framework.dominio.spi.IAgregadoRepositorio;

public interface IAulaRepositorio extends IAgregadoRepositorio<AulaIdentificador, Aula> {
    Aula bloquear(AulaIdentificador identificador);
    boolean existeChoque(ProfissionalIdentificador professor, LocalDate data, LocalTime inicio, LocalTime fim,
            Optional<AulaIdentificador> ignorada);
    int contarInscritos(AulaIdentificador aula);
    boolean existeInscricaoAtiva(AulaIdentificador aula, AlunoIdentificador aluno);
    void inscrever(AulaIdentificador aula, AlunoIdentificador aluno, OffsetDateTime instante);
    boolean cancelarInscricao(Long inscricao, AlunoIdentificador aluno, OffsetDateTime agora);
    boolean cancelarInscricaoGestao(Long inscricao, OffsetDateTime agora);
    void cancelarInscritos(AulaIdentificador aula, String titulo, OffsetDateTime instante);
}

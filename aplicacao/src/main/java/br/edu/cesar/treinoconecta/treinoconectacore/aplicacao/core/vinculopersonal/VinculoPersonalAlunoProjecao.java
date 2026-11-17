package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.vinculopersonal;

import java.time.LocalDate;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.ProfissionalIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.vinculopersonal.SituacaoVinculo;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.vinculopersonal.VinculoPersonalAluno;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.vinculopersonal.VinculoPersonalAlunoIdentificador;
import dev.sauloaraujo.d5.framework.aplicacao.IProjecao;

public record VinculoPersonalAlunoProjecao(VinculoPersonalAlunoIdentificador identificador,
        ProfissionalIdentificador profissional, String profissionalNome, AlunoIdentificador aluno, String alunoNome,
        LocalDate inicio, Optional<LocalDate> encerramento, SituacaoVinculo situacao)
        implements IProjecao<VinculoPersonalAluno> {
    public VinculoPersonalAlunoProjecao(Long identificador, Long profissional, String profissionalNome, Long aluno,
            String alunoNome, LocalDate inicio, @Nullable LocalDate encerramento, SituacaoVinculo situacao) {
        this(new VinculoPersonalAlunoIdentificador(identificador.toString()),
                new ProfissionalIdentificador(profissional.toString()), profissionalNome,
                new AlunoIdentificador(aluno.toString()), alunoNome, inicio, Optional.ofNullable(encerramento), situacao);
    }
}

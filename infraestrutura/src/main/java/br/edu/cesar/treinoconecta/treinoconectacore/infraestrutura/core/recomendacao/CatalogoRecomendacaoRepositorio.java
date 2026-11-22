package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.recomendacao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.CategoriaExercicio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.ExercicioIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.NivelExperiencia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.ObjetivoAluno;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.RegiaoCorporal;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.Plano;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.PlanoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.SituacaoPlano;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.ExercicioClassificado;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.spi.ICatalogoRecomendacaoRepositorio;

public final class CatalogoRecomendacaoRepositorio implements ICatalogoRecomendacaoRepositorio {
    private final JdbcTemplate banco;

    public CatalogoRecomendacaoRepositorio(JdbcTemplate banco) {
        this.banco = Objects.requireNonNull(banco);
    }

    @Override
    public List<ExercicioClassificado> exerciciosClassificados() {
        return banco.query("""
                select e.EXE_ID, e.EXE_NOME, e.EXE_CATEGORIA, c.CEX_NIVEL, c.CEX_OBJETIVOS, c.CEX_CONTRAINDICACOES
                  from EXE_EXERCICIO e join CEX_CLASSIFICACAO_EXERCICIO c on c.EXE_ID = e.EXE_ID
                 order by e.EXE_NOME
                """, (linha, indice) -> {
            var objetivos = EnumSet.noneOf(ObjetivoAluno.class);
            for (var objetivo : linha.getString(5).split(",")) {
                if (!objetivo.isBlank()) {
                    objetivos.add(ObjetivoAluno.valueOf(objetivo.strip()));
                }
            }
            return new ExercicioClassificado(new ExercicioIdentificador(linha.getString(1)), linha.getString(2),
                    CategoriaExercicio.valueOf(linha.getString(3)), NivelExperiencia.valueOf(linha.getString(4)), objetivos,
                    RegiaoCorporal.lista(linha.getString(6)));
        });
    }

    @Override
    public List<LocalDateTime> entradasDoAluno(AlunoIdentificador aluno, LocalDate desde) {
        return banco.query("select RAC_ENTRADA from RAC_REGISTRO_ACESSO where ALU_ID = ? and RAC_ENTRADA >= ?",
                (linha, indice) -> horarioLocal(linha.getObject(1, OffsetDateTime.class)), Long.valueOf(aluno.toString()),
                inicioLocal(desde));
    }

    @Override
    public List<LocalDateTime> entradasDaAcademia(LocalDate desde, LocalDate ate) {
        return banco.query("select RAC_ENTRADA from RAC_REGISTRO_ACESSO where RAC_ENTRADA >= ? and RAC_ENTRADA < ?",
                (linha, indice) -> horarioLocal(linha.getObject(1, OffsetDateTime.class)), inicioLocal(desde),
                inicioLocal(ate.plusDays(1)));
    }

    /** As entradas do controle de acesso são gravadas com fuso; as recomendações usam o horário da academia. */
    private static LocalDateTime horarioLocal(OffsetDateTime entrada) {
        return entrada.atZoneSameInstant(ParametrosAcademia.FUSO).toLocalDateTime();
    }

    private static OffsetDateTime inicioLocal(LocalDate data) {
        return data.atStartOfDay(ParametrosAcademia.FUSO).toOffsetDateTime();
    }

    @Override
    public List<Plano> planosAtivos() {
        return banco.query("select PLA_ID, PLA_NOME, PLA_VALOR, PLA_DURACAO_MESES from PLA_PLANO where PLA_SITUACAO = 'ATIVO'",
                (linha, indice) -> new Plano(Optional.of(new PlanoIdentificador(linha.getString(1))), linha.getString(2),
                        linha.getBigDecimal(3), linha.getInt(4), SituacaoPlano.ATIVO));
    }
}

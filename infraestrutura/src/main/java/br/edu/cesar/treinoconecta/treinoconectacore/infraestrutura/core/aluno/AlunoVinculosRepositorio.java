package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.IAlunoVinculosRepositorio;

/**
 * Procura registros do aluno em todas as tabelas que guardam o identificador dele. A lista de tabelas vem do catálogo
 * do banco, para que novas funcionalidades também impeçam a exclusão definitiva sem precisar alterar esta classe.
 */
public final class AlunoVinculosRepositorio implements IAlunoVinculosRepositorio {
    /** Tabelas que pertencem ao próprio aluno e são apagadas junto com ele. */
    private static final Set<String> PROPRIAS = Set.of("ALU_ALUNO", "AHI_ALUNO_HISTORICO");
    private static final String NOME_SEGURO = "[A-Z0-9_]+";
    private static final String TABELAS = """
            select TABLE_NAME, COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS
             where TABLE_SCHEMA = 'PUBLIC' and COLUMN_NAME in ('ALU_ID','ALU_ORGANIZADOR')
            """;

    private final JdbcTemplate banco;

    public AlunoVinculosRepositorio(JdbcTemplate banco) {
        this.banco = Objects.requireNonNull(banco);
    }

    @Override
    public boolean possuiMatriculaAtiva(AlunoIdentificador aluno) {
        return existe("select count(*) from ADP_ADESAO_PLANO where ALU_ID = ? and ADP_SITUACAO = 'ATIVA'", aluno);
    }

    @Override
    public boolean possuiFrequencia(AlunoIdentificador aluno) {
        return existe("select count(*) from RAC_REGISTRO_ACESSO where ALU_ID = ?", aluno);
    }

    @Override
    public boolean possuiOutrosRegistros(AlunoIdentificador aluno) {
        List<String[]> colunas = banco.query(TABELAS,
                (linha, indice) -> new String[] {linha.getString(1), linha.getString(2)});
        for (var coluna : colunas) {
            if (PROPRIAS.contains(coluna[0]) || !coluna[0].matches(NOME_SEGURO) || !coluna[1].matches(NOME_SEGURO)) {
                continue;
            }
            if (existe("select count(*) from " + coluna[0] + " where " + coluna[1] + " = ?", aluno)) {
                return true;
            }
        }
        return false;
    }

    private boolean existe(String sql, AlunoIdentificador aluno) {
        Objects.requireNonNull(aluno);
        Long total = banco.queryForObject(sql, Long.class, Long.valueOf(aluno.toString()));
        return total != null && total > 0;
    }
}

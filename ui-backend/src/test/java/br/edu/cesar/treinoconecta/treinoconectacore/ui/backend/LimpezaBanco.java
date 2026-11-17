package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;

/** Apaga, na ordem das chaves estrangeiras, as tabelas que dependem de alunos, professores, personals e armários. */
public final class LimpezaBanco {
    private static final List<String> DEPENDENTES = List.of(
            "PAG_PAGAMENTO", "MEN_MENSALIDADE", "ADP_ADESAO_PLANO", "VPA_VINCULO_PERSONAL_ALUNO",
            "CTR_CONVITE_TREINO", "TGR_TREINO_GRUPO", "CGR_CONVITE_GRUPO", "PGR_PARTICIPACAO_GRUPO", "GRT_GRUPO_TREINO",
            "AVP_AVALIACAO_PROFESSOR", "VAT_TREINO_AVALIADO", "TRN_TREINO", "VAP_AVALIACAO_REALIZADA",
            "MET_META", "MDC_MEDIDA_CORPORAL", "AVF_AVALIACAO_FISICA", "EXC_EXECUCAO_EXERCICIO", "SRE_SERIE",
            "TRE_TREINO", "PLT_PLANO_TREINO", "RSV_RESERVA_ARMARIO");

    private LimpezaBanco() { }

    public static void limparDependentes(JdbcTemplate banco) {
        DEPENDENTES.forEach(tabela -> banco.update("delete from " + tabela));
    }
}

package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.profissional;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.Profissional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.ProfissionalIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.SituacaoProfissional;
import dev.sauloaraujo.d5.framework.aplicacao.IProjecao;

public record ProfissionalProjecao(ProfissionalIdentificador identificador, String nome, String cpf, String cref,
        String contato, boolean professor, boolean personalTrainer, SituacaoProfissional situacao, long vinculosAtivos)
        implements IProjecao<Profissional> {
    public ProfissionalProjecao(Long identificador, String nome, String cpf, String cref, String contato, Boolean professor,
            Boolean personalTrainer, SituacaoProfissional situacao, Long vinculosAtivos) {
        this(new ProfissionalIdentificador(identificador.toString()), nome, cpf, cref, contato, professor, personalTrainer,
                situacao, vinculosAtivos);
    }
}

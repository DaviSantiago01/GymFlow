package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio;

import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.spi.IExercicioRepositorio;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoServicoAbstrato;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;

public final class ExercicioServico
        extends AgregadoServicoAbstrato<ExercicioIdentificador, Exercicio, AgregadoAutorizacoes<Exercicio>> {
    public static final String IDENTIFICADOR_EXISTENTE_MODELO = "Já existe um exercício com o identificador %s";
    public static final String IDENTIFICADOR_INEXISTENTE_MODELO = "Não existe exercício com o identificador %s";
    public static final String NOME_INVALIDO = "Informe um nome de exercício com até 100 caracteres";
    public static final String CARGA_MAXIMA_INVALIDA = "A carga máxima do exercício deve ser maior que zero";
    public static final String INCREMENTO_INVALIDO = "O incremento de carga deve ser maior que zero";

    public ExercicioServico(ISegurancaServico seguranca, IExercicioRepositorio repositorio) {
        super(seguranca, repositorio, IDENTIFICADOR_EXISTENTE_MODELO, IDENTIFICADOR_INEXISTENTE_MODELO);
    }

    @Override
    public AgregadoAutorizacoes<Exercicio> criarAutorizacoes(Optional<ExercicioIdentificador> identificador) {
        Objects.requireNonNull(identificador);
        boolean personal = getSegurancaServico().possuiAutorizacao(Autorizacao.PERSONAL.name());
        boolean aluno = getSegurancaServico().possuiAutorizacao(Autorizacao.USUARIO.name());
        return new AgregadoAutorizacoes<>(false, personal || aluno, false, false);
    }
}

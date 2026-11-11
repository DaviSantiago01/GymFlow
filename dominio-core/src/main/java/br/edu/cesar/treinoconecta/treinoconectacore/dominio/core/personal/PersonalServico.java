package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal;

import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.spi.IPersonalRepositorio;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoServicoAbstrato;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;

public final class PersonalServico
        extends AgregadoServicoAbstrato<PersonalIdentificador, Personal, AgregadoAutorizacoes<Personal>> {
    public static final String IDENTIFICADOR_EXISTENTE_MODELO = "Já existe um personal com o identificador %s";
    public static final String IDENTIFICADOR_INEXISTENTE_MODELO = "Não existe personal com o identificador %s";
    public static final String USUARIO_INVALIDO = "Informe um usuário com até 100 caracteres";
    public static final String NOME_INVALIDO = "Informe um nome com até 100 caracteres";
    public static final String USUARIO_SEM_CADASTRO = "Usuário sem cadastro de personal";

    public PersonalServico(ISegurancaServico seguranca, IPersonalRepositorio repositorio) {
        super(seguranca, repositorio, IDENTIFICADOR_EXISTENTE_MODELO, IDENTIFICADOR_INEXISTENTE_MODELO);
    }

    @Override
    public AgregadoAutorizacoes<Personal> criarAutorizacoes(Optional<PersonalIdentificador> identificador) {
        Objects.requireNonNull(identificador);
        boolean personal = getSegurancaServico().possuiAutorizacao(Autorizacao.PERSONAL.name());
        return new AgregadoAutorizacoes<>(false, personal, false, false);
    }
}

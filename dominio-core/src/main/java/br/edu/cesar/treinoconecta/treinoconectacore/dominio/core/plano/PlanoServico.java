package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.spi.IPlanoRepositorio;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoServicoAbstrato;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;

public final class PlanoServico extends AgregadoServicoAbstrato<PlanoIdentificador, Plano, PlanoAutorizacoes> {
    public static final String IDENTIFICADOR_EXISTENTE_MODELO = "Já existe um plano com o identificador %s";
    public static final String IDENTIFICADOR_INEXISTENTE_MODELO = "Não existe plano com o identificador %s";
    public static final String NOME_OBRIGATORIO = "Informe o nome do plano com 2 a " + Plano.TAMANHO_NOME + " caracteres";
    public static final String VALOR_INVALIDO = "O valor do plano deve ser maior que zero e no máximo " + Plano.VALOR_MAXIMO;
    public static final String DURACAO_INVALIDA = "A duração do plano deve ser de " + Plano.DURACAO_MINIMA_MESES + " a "
            + Plano.DURACAO_MAXIMA_MESES + " meses";
    public static final String PLANO_DUPLICADO = "Já existe um plano ativo com este nome e esta duração";
    public static final String PLANO_INATIVO = "Somente um plano ativo pode ser alterado";

    private final IPlanoRepositorio repositorio;

    public PlanoServico(ISegurancaServico seguranca, IPlanoRepositorio repositorio) {
        super(seguranca, repositorio, IDENTIFICADOR_EXISTENTE_MODELO, IDENTIFICADOR_INEXISTENTE_MODELO);
        this.repositorio = Objects.requireNonNull(repositorio);
    }

    @Override
    public PlanoAutorizacoes criarAutorizacoes(Optional<PlanoIdentificador> identificador) {
        Objects.requireNonNull(identificador);
        boolean administrador = getSegurancaServico().possuiAutorizacao(Autorizacao.ADMINISTRADOR.name());
        return new PlanoAutorizacoes(administrador, administrador, administrador, administrador);
    }

    public Plano cadastrar(String nome, BigDecimal valor, int duracaoMeses) {
        validarAutorizacao(Optional.empty(), PlanoAutorizacoes::getCadastrar);
        var plano = Plano.cadastrar(nome, valor, duracaoMeses);
        validarSemDuplicidade(plano, Optional.empty());
        return repositorio.incluir(plano);
    }

    public Plano editar(PlanoIdentificador identificador, String nome, BigDecimal valor, int duracaoMeses) {
        Objects.requireNonNull(identificador);
        validarAutorizacao(Optional.of(identificador), PlanoAutorizacoes::getEditar);
        validarExistencia(identificador);
        var editado = repositorio.bloquear(identificador).editar(nome, valor, duracaoMeses);
        validarSemDuplicidade(editado, Optional.of(identificador));
        repositorio.alterar(editado);
        return editado;
    }

    public Plano inativar(PlanoIdentificador identificador) {
        Objects.requireNonNull(identificador);
        validarAutorizacao(Optional.of(identificador), PlanoAutorizacoes::getInativar);
        validarExistencia(identificador);
        var inativado = repositorio.bloquear(identificador).inativar();
        repositorio.alterar(inativado);
        return inativado;
    }

    private void validarSemDuplicidade(Plano plano, Optional<PlanoIdentificador> ignorado) {
        Validate.validState(!repositorio.existeAtivoComNomeEDuracao(plano.getNome(), plano.getDuracaoMeses(), ignorado),
                PLANO_DUPLICADO);
    }
}

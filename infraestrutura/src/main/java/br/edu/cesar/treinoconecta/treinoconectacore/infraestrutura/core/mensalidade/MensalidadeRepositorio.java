package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.mensalidade;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.AdesaoPlanoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.Mensalidade;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.MensalidadeIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.MensalidadeServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.Pagamento;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.PeriodoCobranca;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.spi.IMensalidadeRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.adesaoplano.IAdesaoPlanoRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.IAlunoRepositorioJpa;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoRepositorioAbstrato;

public final class MensalidadeRepositorio
        extends AgregadoRepositorioAbstrato<MensalidadeIdentificador, Mensalidade, Long, MensalidadeJpa>
        implements IMensalidadeRepositorio {
    private final IMensalidadeRepositorioJpa repositorioJpa;
    private final IAdesaoPlanoRepositorioJpa adesoes;
    private final IAlunoRepositorioJpa alunos;

    public MensalidadeRepositorio(IMensalidadeRepositorioJpa repositorioJpa, IAdesaoPlanoRepositorioJpa adesoes,
            IAlunoRepositorioJpa alunos) {
        super(repositorioJpa, MensalidadeServico.IDENTIFICADOR_INEXISTENTE_MODELO);
        this.repositorioJpa = Objects.requireNonNull(repositorioJpa);
        this.adesoes = Objects.requireNonNull(adesoes);
        this.alunos = Objects.requireNonNull(alunos);
    }

    @Override
    protected Long paraIdentificadorJpa(MensalidadeIdentificador identificador) {
        return Long.valueOf(identificador.toString());
    }

    @Override
    protected MensalidadeJpa paraJpa(Mensalidade agregado) {
        var identificador = agregado.getIdentificador().map(this::paraIdentificadorJpa).orElse(null);
        var pagamentos = new ArrayList<PagamentoJpa>();
        for (var pagamento : agregado.getPagamentos()) {
            pagamentos.add(new PagamentoJpa(pagamento.getIdentificador().orElse(null), pagamento.getValor(),
                    pagamento.getForma(), pagamento.getRegistro(), pagamento.getSituacao(),
                    pagamento.getEstorno().orElse(null), pagamento.getMotivoEstorno().orElse(null)));
        }
        return new MensalidadeJpa(identificador, adesoes.getReferenceById(Long.valueOf(agregado.getAdesao().toString())),
                alunos.getReferenceById(Long.valueOf(agregado.getAluno().toString())), agregado.getPeriodo().inicio(),
                agregado.getPeriodo().fim(), agregado.getVencimento(), agregado.getValor(), agregado.getSituacao(), pagamentos);
    }

    @Override
    protected Mensalidade paraAgregado(MensalidadeJpa entidadeJpa) {
        var pagamentos = new ArrayList<Pagamento>();
        for (var pagamento : entidadeJpa.getPagamentos()) {
            pagamentos.add(new Pagamento(Optional.ofNullable(pagamento.getIdentificador()), pagamento.getValor(),
                    pagamento.getForma(), pagamento.getRegistro(), pagamento.getSituacao(),
                    Optional.ofNullable(pagamento.getEstorno()), Optional.ofNullable(pagamento.getMotivoEstorno())));
        }
        return new Mensalidade(Optional.of(new MensalidadeIdentificador(entidadeJpa.getIdentificador().toString())),
                new AdesaoPlanoIdentificador(entidadeJpa.getAdesao().getIdentificador().toString()),
                new AlunoIdentificador(entidadeJpa.getAluno().getIdentificador().toString()),
                new PeriodoCobranca(entidadeJpa.getPeriodoInicio(), entidadeJpa.getPeriodoFim()), entidadeJpa.getVencimento(),
                entidadeJpa.getValor(), entidadeJpa.getSituacao(), pagamentos);
    }

    /** O estorno é gravado de imediato: a coluna de pagamento confirmado é única por mensalidade. */
    @Override
    public void alterar(Mensalidade agregado) {
        Objects.requireNonNull(agregado);
        repositorioJpa.saveAndFlush(paraJpa(agregado));
    }

    @Override
    public Mensalidade bloquear(MensalidadeIdentificador identificador) {
        Objects.requireNonNull(identificador);
        return paraAgregado(repositorioJpa.bloquear(paraIdentificadorJpa(identificador)).orElseThrow(
                () -> new NoSuchElementException(MensalidadeServico.IDENTIFICADOR_INEXISTENTE_MODELO.formatted(identificador))));
    }

    @Override
    public boolean existePorAdesaoEPeriodo(AdesaoPlanoIdentificador adesao, LocalDate inicioPeriodo) {
        Objects.requireNonNull(adesao);
        Objects.requireNonNull(inicioPeriodo);
        return repositorioJpa.existePorAdesaoEPeriodo(Long.valueOf(adesao.toString()), inicioPeriodo);
    }

    @Override
    public boolean existeAtrasadaDoAluno(br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador aluno,
            LocalDate hoje) {
        Objects.requireNonNull(aluno);
        Objects.requireNonNull(hoje);
        return repositorioJpa.existeAtrasadaDoAluno(Long.valueOf(aluno.toString()), hoje);
    }
}

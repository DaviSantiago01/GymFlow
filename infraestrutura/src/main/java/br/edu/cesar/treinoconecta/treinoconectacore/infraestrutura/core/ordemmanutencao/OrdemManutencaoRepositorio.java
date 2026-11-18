package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.ordemmanutencao;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ordemmanutencao.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.problemaequipamento.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ordemmanutencao.spi.IOrdemManutencaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.problemaequipamento.IProblemaEquipamentoRepositorioJpa;
import dev.sauloaraujo.d5.framework.dominio.AgregadoInexistenteExcecao;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoRepositorioAbstrato;

public final class OrdemManutencaoRepositorio extends AgregadoRepositorioAbstrato<OrdemManutencaoIdentificador, OrdemManutencao, Long, OrdemManutencaoJpa>
        implements IOrdemManutencaoRepositorio {
    private static final List<SituacaoOrdem> SITUACOES_ATIVAS = List.of(SituacaoOrdem.ABERTA, SituacaoOrdem.EM_ANDAMENTO);

    private final IOrdemManutencaoRepositorioJpa jpa;
    private final IProblemaEquipamentoRepositorioJpa referencias;

    public OrdemManutencaoRepositorio(IOrdemManutencaoRepositorioJpa jpa, IProblemaEquipamentoRepositorioJpa referencias) {
        super(jpa, OrdemManutencaoServico.INEXISTENTE);
        this.jpa = Objects.requireNonNull(jpa);
        this.referencias = Objects.requireNonNull(referencias);
    }

    @Override
    protected Long paraIdentificadorJpa(OrdemManutencaoIdentificador identificador) {
        return Long.valueOf(identificador.toString());
    }

    @Override
    protected OrdemManutencaoJpa paraJpa(OrdemManutencao agregado) {
        return new OrdemManutencaoJpa(agregado.identificador().map(this::paraIdentificadorJpa).orElse(null),
                referencias.getReferenceById(Long.valueOf(agregado.problema().toString())),
                agregado.dados().tecnico(),
                agregado.dados().diagnostico(),
                agregado.abertura(),
                agregado.responsavel(),
                agregado.situacao(),
                agregado.inicio().orElse(null),
                agregado.termino().orElse(null),
                agregado.dados().custoEstimado().orElse(null),
                agregado.custoReal().orElse(null),
                agregado.servicoExecutado().orElse(null),
                agregado.justificativa().orElse(null));
    }

    @Override
    protected OrdemManutencao paraAgregado(OrdemManutencaoJpa entidade) {
        return new OrdemManutencao(Optional.of(new OrdemManutencaoIdentificador(entidade.getIdentificador().toString())),
                new ProblemaEquipamentoIdentificador(entidade.getProblema().getIdentificador().toString()),
                new DadosOrdem(entidade.getTecnico(), entidade.getDiagnostico(), Optional.ofNullable(entidade.getCustoEstimado())),
                entidade.getAbertura(),
                entidade.getResponsavel(),
                entidade.getSituacao(),
                Optional.ofNullable(entidade.getInicio()),
                Optional.ofNullable(entidade.getTermino()),
                Optional.ofNullable(entidade.getCustoReal()),
                Optional.ofNullable(entidade.getServicoExecutado()),
                Optional.ofNullable(entidade.getJustificativa()));
    }

    @Override
    public ProblemaEquipamentoIdentificador identificarProblema(OrdemManutencaoIdentificador ordem) {
        return jpa.identificarProblema(paraIdentificadorJpa(ordem))
                .map(id -> new ProblemaEquipamentoIdentificador(id.toString()))
                .orElseThrow(() -> new AgregadoInexistenteExcecao(OrdemManutencaoServico.INEXISTENTE.formatted(ordem)));
    }

    @Override
    public boolean existeAtiva(ProblemaEquipamentoIdentificador problema) {
        return jpa.existsByProblemaIdentificadorAndSituacaoIn(Long.valueOf(problema.toString()), SITUACOES_ATIVAS);
    }

    @Override
    public boolean existeAtivaNoEquipamento(EquipamentoIdentificador equipamento) {
        return jpa.existsByProblemaEquipamentoIdentificadorAndSituacaoIn(Long.valueOf(equipamento.toString()), SITUACOES_ATIVAS);
    }
}

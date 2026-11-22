package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aulacoletiva;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aulacoletiva.AulaColetiva;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aulacoletiva.AulaColetivaIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aulacoletiva.AulaColetivaServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aulacoletiva.spi.IAulaColetivaRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.ObjetivoAluno;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.RegiaoCorporal;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoRepositorioAbstrato;

public final class AulaColetivaRepositorio
        extends AgregadoRepositorioAbstrato<AulaColetivaIdentificador, AulaColetiva, Long, AulaColetivaJpa>
        implements IAulaColetivaRepositorio {
    private final IAulaColetivaRepositorioJpa repositorioJpa;
    private final Clock relogio;

    public AulaColetivaRepositorio(IAulaColetivaRepositorioJpa repositorioJpa, Clock relogio) {
        super(repositorioJpa, AulaColetivaServico.IDENTIFICADOR_INEXISTENTE_MODELO);
        this.repositorioJpa = Objects.requireNonNull(repositorioJpa);
        this.relogio = Objects.requireNonNull(relogio);
    }

    @Override
    protected Long paraIdentificadorJpa(AulaColetivaIdentificador identificador) {
        return Long.valueOf(identificador.toString());
    }

    /** Mantém a data das inscrições já gravadas; as novas recebem o instante atual. */
    @Override
    protected AulaColetivaJpa paraJpa(AulaColetiva agregado) {
        var identificador = agregado.getIdentificador().map(this::paraIdentificadorJpa).orElse(null);
        var existentes = new HashMap<Long, InscricaoAulaJpa>();
        if (identificador != null) {
            repositorioJpa.findById(identificador).ifPresent(atual -> atual.getInscricoes()
                    .forEach(inscricao -> existentes.put(inscricao.getAluno(), inscricao)));
        }
        var inscricoes = new ArrayList<InscricaoAulaJpa>();
        for (var aluno : agregado.getInscritos()) {
            var codigo = Long.valueOf(aluno.toString());
            var existente = existentes.get(codigo);
            inscricoes.add(existente != null ? new InscricaoAulaJpa(codigo, existente.getData())
                    : new InscricaoAulaJpa(codigo, relogio.instant()));
        }
        return new AulaColetivaJpa(identificador, agregado.getNome(), agregado.getModalidade(), agregado.getInstrutor(),
                agregado.getSala(), agregado.getDia(), agregado.getInicio(), agregado.getFim(), agregado.getCapacidade(),
                agregado.getNivel(), agregado.getImpacto(),
                agregado.getObjetivos().stream().map(Enum::name).sorted().collect(Collectors.joining(",")),
                agregado.getContraindicacoes().stream().map(Enum::name).sorted().collect(Collectors.joining(",")),
                agregado.isAtiva(), inscricoes);
    }

    @Override
    protected AulaColetiva paraAgregado(AulaColetivaJpa entidade) {
        var objetivos = new HashSet<ObjetivoAluno>();
        for (var objetivo : entidade.getObjetivos().split(",")) {
            if (!objetivo.isBlank()) {
                objetivos.add(ObjetivoAluno.valueOf(objetivo.strip()));
            }
        }
        var inscritos = entidade.getInscricoes().stream()
                .map(inscricao -> new AlunoIdentificador(inscricao.getAluno().toString())).toList();
        return new AulaColetiva(Optional.of(new AulaColetivaIdentificador(entidade.getIdentificador().toString())),
                entidade.getNome(), entidade.getModalidade(), entidade.getInstrutor(), entidade.getSala(), entidade.getDia(),
                entidade.getInicio(), entidade.getFim(), entidade.getCapacidade(), entidade.getNivel(), entidade.getImpacto(),
                objetivos, RegiaoCorporal.lista(entidade.getContraindicacoes()), entidade.isAtiva(), inscritos);
    }

    @Override
    public AulaColetiva bloquear(AulaColetivaIdentificador identificador) {
        Objects.requireNonNull(identificador);
        return paraAgregado(repositorioJpa.bloquear(paraIdentificadorJpa(identificador)).orElseThrow(
                () -> new NoSuchElementException(AulaColetivaServico.IDENTIFICADOR_INEXISTENTE_MODELO.formatted(identificador))));
    }

    @Override
    public List<AulaColetiva> buscarAtivas() {
        return repositorioJpa.buscarAtivas().stream().map(this::paraAgregado).toList();
    }

    @Override
    public List<AulaColetiva> buscarInscricoesDoAluno(AlunoIdentificador aluno) {
        Objects.requireNonNull(aluno);
        return repositorioJpa.buscarInscricoesDoAluno(Long.valueOf(aluno.toString())).stream().map(this::paraAgregado)
                .toList();
    }
}

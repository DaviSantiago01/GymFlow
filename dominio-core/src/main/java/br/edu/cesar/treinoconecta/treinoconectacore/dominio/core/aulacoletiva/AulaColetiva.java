package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aulacoletiva;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.NivelExperiencia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.ObjetivoAluno;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.PerfilAluno;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.RegiaoCorporal;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.DiaSemana;
import dev.sauloaraujo.d5.framework.dominio.IAgregado;

/** Aula coletiva semanal, com capacidade, nível exigido, objetivos atendidos e contraindicações. */
public final class AulaColetiva implements IAgregado<AulaColetivaIdentificador> {
    private final Optional<AulaColetivaIdentificador> identificador;
    private final String nome;
    private final String modalidade;
    private final String instrutor;
    private final String sala;
    private final DiaSemana dia;
    private final LocalTime inicio;
    private final LocalTime fim;
    private final int capacidade;
    private final NivelExperiencia nivel;
    private final ImpactoAula impacto;
    private final Set<ObjetivoAluno> objetivos;
    private final Set<RegiaoCorporal> contraindicacoes;
    private final boolean ativa;
    private final List<AlunoIdentificador> inscritos;

    public AulaColetiva(Optional<AulaColetivaIdentificador> identificador, String nome, String modalidade, String instrutor,
            String sala, DiaSemana dia, LocalTime inicio, LocalTime fim, int capacidade, NivelExperiencia nivel,
            ImpactoAula impacto, Set<ObjetivoAluno> objetivos, Set<RegiaoCorporal> contraindicacoes, boolean ativa,
            List<AlunoIdentificador> inscritos) {
        this.identificador = Objects.requireNonNull(identificador);
        this.nome = Validate.notBlank(nome, "Nome da aula obrigatório");
        this.modalidade = Validate.notBlank(modalidade, "Modalidade obrigatória");
        this.instrutor = Validate.notBlank(instrutor, "Instrutor obrigatório");
        this.sala = Validate.notBlank(sala, "Sala obrigatória");
        this.dia = Objects.requireNonNull(dia);
        this.inicio = Objects.requireNonNull(inicio);
        this.fim = Objects.requireNonNull(fim);
        Validate.isTrue(inicio.isBefore(fim), "O início da aula deve ser anterior ao fim");
        Validate.isTrue(capacidade > 0, "A capacidade da aula deve ser maior que zero");
        this.capacidade = capacidade;
        this.nivel = Objects.requireNonNull(nivel);
        this.impacto = Objects.requireNonNull(impacto);
        Validate.isTrue(!objetivos.isEmpty(), "Informe ao menos um objetivo atendido pela aula");
        this.objetivos = Set.copyOf(EnumSet.copyOf(objetivos));
        this.contraindicacoes = contraindicacoes.isEmpty() ? Set.of() : Set.copyOf(EnumSet.copyOf(contraindicacoes));
        this.ativa = ativa;
        this.inscritos = List.copyOf(inscritos);
        Validate.isTrue(this.inscritos.size() == Set.copyOf(this.inscritos).size(), "Aluno inscrito mais de uma vez");
        Validate.isTrue(this.inscritos.size() <= capacidade, AulaColetivaServico.SEM_VAGAS);
    }

    public int vagas() {
        return capacidade - inscritos.size();
    }

    public boolean possuiVaga() {
        return vagas() > 0;
    }

    public boolean inscrito(AlunoIdentificador aluno) {
        return inscritos.contains(aluno);
    }

    public boolean conflitaCom(AulaColetiva outra) {
        Objects.requireNonNull(outra);
        return !outra.identificador.equals(identificador) && outra.dia == dia && inicio.isBefore(outra.fim)
                && outra.inicio.isBefore(fim);
    }

    public AulaColetiva inscrever(AlunoIdentificador aluno) {
        Objects.requireNonNull(aluno);
        Validate.validState(ativa, AulaColetivaServico.AULA_INATIVA);
        Validate.validState(!inscrito(aluno), AulaColetivaServico.JA_INSCRITO);
        Validate.validState(possuiVaga(), AulaColetivaServico.SEM_VAGAS);
        var lista = new ArrayList<>(inscritos);
        lista.add(aluno);
        return new AulaColetiva(identificador, nome, modalidade, instrutor, sala, dia, inicio, fim, capacidade, nivel,
                impacto, objetivos, contraindicacoes, ativa, lista);
    }

    /**
     * RN004, RN005 e RN006: motivo pelo qual a aula não pode ser recomendada ao aluno, ou vazio quando ela é compatível
     * com as restrições, o objetivo, o nível e a disponibilidade dele, não conflita com as aulas em que ele já está
     * inscrito e ainda tem vaga.
     */
    public Optional<MotivoIncompatibilidade> incompatibilidadeCom(PerfilAluno perfil, List<AulaColetiva> inscricoesDoAluno) {
        Objects.requireNonNull(perfil);
        Objects.requireNonNull(inscricoesDoAluno);
        var aluno = perfil.getAluno();
        if (inscrito(aluno)) {
            return Optional.of(new MotivoIncompatibilidade(TipoIncompatibilidade.JA_INSCRITO, "Você já está inscrito"));
        }
        var restricao = perfil.restricaoConflitante(contraindicacoes);
        if (restricao.isPresent()) {
            return Optional.of(new MotivoIncompatibilidade(TipoIncompatibilidade.CONTRAINDICADA,
                    impacto.getDescricao() + ", contraindicada para a restrição em "
                            + restricao.get().regiao().getDescricao().toLowerCase()));
        }
        if (perfil.getNivel().filter(nivelAluno -> !nivelAluno.comporta(nivel)).isPresent()) {
            return Optional.of(new MotivoIncompatibilidade(TipoIncompatibilidade.NIVEL,
                    "Nível " + nivel.getDescricao().toLowerCase() + ", acima do seu nível"));
        }
        if (perfil.getObjetivo().filter(objetivo -> !objetivos.contains(objetivo)).isPresent()) {
            return Optional.of(new MotivoIncompatibilidade(TipoIncompatibilidade.OBJETIVO, "Não atende ao seu objetivo"));
        }
        if (perfil.getDisponibilidade().filter(disponivel -> !disponivel.comporta(dia, inicio, fim)).isPresent()) {
            return Optional.of(new MotivoIncompatibilidade(TipoIncompatibilidade.DISPONIBILIDADE, "Fora da sua disponibilidade"));
        }
        for (var inscrita : inscricoesDoAluno) {
            if (conflitaCom(inscrita)) {
                return Optional.of(new MotivoIncompatibilidade(TipoIncompatibilidade.CONFLITO,
                        "Mesmo horário da sua aula de " + inscrita.getNome().toLowerCase()));
            }
        }
        if (!possuiVaga()) {
            return Optional.of(new MotivoIncompatibilidade(TipoIncompatibilidade.SEM_VAGAS, "Sem vagas no momento"));
        }
        return Optional.empty();
    }

    public enum TipoIncompatibilidade {
        JA_INSCRITO, CONTRAINDICADA, NIVEL, OBJETIVO, DISPONIBILIDADE, CONFLITO, SEM_VAGAS
    }

    public record MotivoIncompatibilidade(TipoIncompatibilidade tipo, String descricao) {
    }

    public Optional<AulaColetivaIdentificador> getIdentificador() { return identificador; }
    public String getNome() { return nome; }
    public String getModalidade() { return modalidade; }
    public String getInstrutor() { return instrutor; }
    public String getSala() { return sala; }
    public DiaSemana getDia() { return dia; }
    public LocalTime getInicio() { return inicio; }
    public LocalTime getFim() { return fim; }
    public int getCapacidade() { return capacidade; }
    public NivelExperiencia getNivel() { return nivel; }
    public ImpactoAula getImpacto() { return impacto; }
    public Set<ObjetivoAluno> getObjetivos() { return objetivos; }
    public Set<RegiaoCorporal> getContraindicacoes() { return contraindicacoes; }
    public boolean isAtiva() { return ativa; }
    public List<AlunoIdentificador> getInscritos() { return inscritos; }
}

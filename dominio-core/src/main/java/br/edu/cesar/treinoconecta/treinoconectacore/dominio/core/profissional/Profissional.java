package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.Validate;
import dev.sauloaraujo.d5.framework.dominio.IAgregado;

/**
 * Professor ou personal trainer da academia. Especialidades e disponibilidades pertencem ao agregado, que garante
 * especialidades sem repetição, disponibilidades sem sobreposição no mesmo dia e a carga horária semanal máxima.
 */
public final class Profissional implements IAgregado<ProfissionalIdentificador> {
    public static final int TAMANHO_NOME = 100;
    public static final int TAMANHO_CONTATO = 100;

    private final Optional<ProfissionalIdentificador> identificador;
    private final String nome;
    private final Cpf cpf;
    private final Cref cref;
    private final String contato;
    private final Set<ClassificacaoProfissional> classificacoes;
    private final List<Especialidade> especialidades;
    private final List<DisponibilidadeProfissional> disponibilidades;
    private final SituacaoProfissional situacao;

    public Profissional(Optional<ProfissionalIdentificador> identificador, String nome, Cpf cpf, Cref cref, String contato,
            Set<ClassificacaoProfissional> classificacoes, List<Especialidade> especialidades,
            List<DisponibilidadeProfissional> disponibilidades, SituacaoProfissional situacao) {
        this.identificador = Objects.requireNonNull(identificador);
        Validate.notBlank(nome, ProfissionalServico.NOME_OBRIGATORIO);
        this.nome = nome.strip().replaceAll("\\s+", " ");
        Validate.isTrue(this.nome.length() >= 2 && this.nome.length() <= TAMANHO_NOME, ProfissionalServico.NOME_OBRIGATORIO);
        this.cpf = Validate.notNull(cpf, Cpf.MENSAGEM);
        this.cref = Validate.notNull(cref, Cref.MENSAGEM);
        Validate.notBlank(contato, ProfissionalServico.CONTATO_OBRIGATORIO);
        this.contato = contato.strip();
        Validate.isTrue(this.contato.length() <= TAMANHO_CONTATO, ProfissionalServico.CONTATO_OBRIGATORIO);
        Validate.notNull(classificacoes, ProfissionalServico.CLASSIFICACAO_OBRIGATORIA);
        Validate.isTrue(!classificacoes.isEmpty(), ProfissionalServico.CLASSIFICACAO_OBRIGATORIA);
        this.classificacoes = Set.copyOf(EnumSet.copyOf(classificacoes));
        this.especialidades = List.copyOf(especialidades);
        this.disponibilidades = List.copyOf(disponibilidades);
        this.situacao = Objects.requireNonNull(situacao);
        Validate.isTrue(new HashSet<>(this.especialidades).size() == this.especialidades.size(),
                ProfissionalServico.ESPECIALIDADE_DUPLICADA);
        for (int i = 0; i < this.disponibilidades.size(); i++) {
            for (int j = i + 1; j < this.disponibilidades.size(); j++) {
                Validate.isTrue(!this.disponibilidades.get(i).sobrepoe(this.disponibilidades.get(j)),
                        ProfissionalServico.DISPONIBILIDADE_SOBREPOSTA);
            }
        }
        Validate.isTrue(cargaHorariaSemanal().compareTo(ParametrosProfissionais.CARGA_HORARIA_SEMANAL_MAXIMA) <= 0,
                ProfissionalServico.CARGA_EXCEDIDA);
    }

    public static Profissional cadastrar(String nome, Cpf cpf, Cref cref, String contato,
            Set<ClassificacaoProfissional> classificacoes) {
        return new Profissional(Optional.empty(), nome, cpf, cref, contato, classificacoes, List.of(), List.of(),
                SituacaoProfissional.ATIVO);
    }

    public Profissional editar(String novoNome, Cpf novoCpf, Cref novoCref, String novoContato,
            Set<ClassificacaoProfissional> novasClassificacoes, boolean possuiVinculosAtivos) {
        validarAtivo();
        var editado = new Profissional(identificador, novoNome, novoCpf, novoCref, novoContato, novasClassificacoes,
                especialidades, disponibilidades, situacao);
        Validate.validState(!possuiVinculosAtivos || editado.personalTrainer(), ProfissionalServico.VINCULOS_IMPEDEM_CLASSIFICACAO);
        return editado;
    }

    public Profissional adicionarEspecialidade(Especialidade especialidade) {
        Objects.requireNonNull(especialidade);
        validarAtivo();
        Validate.validState(!especialidades.contains(especialidade), ProfissionalServico.ESPECIALIDADE_DUPLICADA);
        var novas = new ArrayList<>(especialidades);
        novas.add(especialidade);
        return comEspecialidades(novas);
    }

    public Profissional removerEspecialidade(Especialidade especialidade) {
        Objects.requireNonNull(especialidade);
        validarAtivo();
        Validate.validState(especialidades.contains(especialidade), ProfissionalServico.ESPECIALIDADE_INEXISTENTE);
        var novas = new ArrayList<>(especialidades);
        novas.remove(especialidade);
        return comEspecialidades(novas);
    }

    public Profissional adicionarDisponibilidade(DayOfWeek dia, LocalTime inicio, LocalTime fim) {
        validarAtivo();
        var novas = new ArrayList<>(disponibilidades);
        novas.add(validarSemConflito(DisponibilidadeProfissional.nova(dia, inicio, fim), disponibilidades));
        return comDisponibilidades(novas);
    }

    public Profissional alterarDisponibilidade(long disponibilidade, DayOfWeek dia, LocalTime inicio, LocalTime fim) {
        validarAtivo();
        var atual = buscarDisponibilidade(disponibilidade);
        var demais = new ArrayList<>(disponibilidades);
        demais.remove(atual);
        var alterada = new DisponibilidadeProfissional(atual.getIdentificador(), dia, inicio, fim);
        var novas = new ArrayList<DisponibilidadeProfissional>();
        for (var existente : disponibilidades) {
            novas.add(existente == atual ? validarSemConflito(alterada, demais) : existente);
        }
        return comDisponibilidades(novas);
    }

    public Profissional removerDisponibilidade(long disponibilidade) {
        validarAtivo();
        var novas = new ArrayList<>(disponibilidades);
        novas.remove(buscarDisponibilidade(disponibilidade));
        return comDisponibilidades(novas);
    }

    /** Soma das disponibilidades da semana, recalculada a cada inclusão, alteração ou remoção. */
    public Duration cargaHorariaSemanal() {
        var total = Duration.ZERO;
        for (var disponibilidade : disponibilidades) {
            total = total.plus(disponibilidade.duracao());
        }
        return total;
    }

    public Profissional inativar(boolean possuiVinculosAtivos) {
        validarAtivo();
        Validate.validState(!possuiVinculosAtivos, ProfissionalServico.VINCULOS_IMPEDEM_INATIVACAO);
        return comSituacao(SituacaoProfissional.INATIVO);
    }

    public Profissional reativar() {
        Validate.validState(situacao == SituacaoProfissional.INATIVO, ProfissionalServico.PROFISSIONAL_JA_ATIVO);
        return comSituacao(SituacaoProfissional.ATIVO);
    }

    public boolean ativo() {
        return situacao == SituacaoProfissional.ATIVO;
    }

    public boolean personalTrainer() {
        return classificacoes.contains(ClassificacaoProfissional.PERSONAL_TRAINER);
    }

    public boolean professor() {
        return classificacoes.contains(ClassificacaoProfissional.PROFESSOR);
    }

    private DisponibilidadeProfissional validarSemConflito(DisponibilidadeProfissional nova,
            List<DisponibilidadeProfissional> demais) {
        for (var existente : demais) {
            Validate.validState(!existente.sobrepoe(nova), ProfissionalServico.DISPONIBILIDADE_SOBREPOSTA);
        }
        var carga = nova.duracao();
        for (var existente : demais) {
            carga = carga.plus(existente.duracao());
        }
        Validate.validState(carga.compareTo(ParametrosProfissionais.CARGA_HORARIA_SEMANAL_MAXIMA) <= 0,
                ProfissionalServico.CARGA_EXCEDIDA);
        return nova;
    }

    private DisponibilidadeProfissional buscarDisponibilidade(long disponibilidade) {
        return disponibilidades.stream().filter(item -> item.possuiIdentificador(disponibilidade)).findFirst()
                .orElseThrow(() -> new IllegalStateException(ProfissionalServico.DISPONIBILIDADE_INEXISTENTE));
    }

    private void validarAtivo() {
        Validate.validState(ativo(), ProfissionalServico.PROFISSIONAL_INATIVO);
    }

    private Profissional comEspecialidades(List<Especialidade> novas) {
        return new Profissional(identificador, nome, cpf, cref, contato, classificacoes, novas, disponibilidades, situacao);
    }

    private Profissional comDisponibilidades(List<DisponibilidadeProfissional> novas) {
        return new Profissional(identificador, nome, cpf, cref, contato, classificacoes, especialidades, novas, situacao);
    }

    private Profissional comSituacao(SituacaoProfissional nova) {
        return new Profissional(identificador, nome, cpf, cref, contato, classificacoes, especialidades, disponibilidades, nova);
    }

    public Optional<ProfissionalIdentificador> getIdentificador() { return identificador; }
    public String getNome() { return nome; }
    public Cpf getCpf() { return cpf; }
    public Cref getCref() { return cref; }
    public String getContato() { return contato; }
    public Set<ClassificacaoProfissional> getClassificacoes() { return classificacoes; }
    public List<Especialidade> getEspecialidades() { return especialidades; }
    public List<DisponibilidadeProfissional> getDisponibilidades() { return disponibilidades; }
    public SituacaoProfissional getSituacao() { return situacao; }
}

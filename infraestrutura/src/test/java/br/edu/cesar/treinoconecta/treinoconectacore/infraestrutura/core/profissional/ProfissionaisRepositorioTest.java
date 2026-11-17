package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.profissional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.profissional.ProfissionalConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.profissional.ProfissionalOrdenacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.ClassificacaoProfissional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.Cpf;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.Cref;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.Especialidade;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.Profissional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.SituacaoProfissional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.vinculopersonal.VinculoPersonalAluno;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.AlunoJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.AlunoJpaTeste;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.IAlunoRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.vinculopersonal.IVinculoPersonalAlunoRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.vinculopersonal.VinculoPersonalAlunoRepositorio;

@DataJpaTest
class ProfissionaisRepositorioTest {
    private static final LocalDate HOJE = LocalDate.parse("2026-11-17");

    @Autowired
    private EntityManager entidades;
    @Autowired
    private IProfissionalRepositorioJpa profissionaisJpa;
    @Autowired
    private IVinculoPersonalAlunoRepositorioJpa vinculosJpa;
    @Autowired
    private IAlunoRepositorioJpa alunosJpa;
    private ProfissionalRepositorio profissionais;
    private VinculoPersonalAlunoRepositorio vinculos;
    private AlunoJpa mariana;
    private Profissional rafael;

    @BeforeEach
    void configurar() {
        profissionais = new ProfissionalRepositorio(profissionaisJpa);
        vinculos = new VinculoPersonalAlunoRepositorio(vinculosJpa, profissionaisJpa, alunosJpa);
        mariana = alunosJpa.save(AlunoJpaTeste.novo(entidades, "mariana-equipe", "Mariana"));
        rafael = profissionais.incluir(Profissional.cadastrar("Rafael Souza", new Cpf("529.982.247-25"),
                new Cref("123456-G/PE"), "rafael@academia.com", EnumSet.of(ClassificacaoProfissional.PERSONAL_TRAINER)));
    }

    private void limparContexto() {
        entidades.flush();
        entidades.clear();
    }

    private Profissional recarregar() {
        limparContexto();
        return profissionais.buscarPorIdentificador(rafael.getIdentificador().orElseThrow());
    }

    @Test
    void especialidadesEDisponibilidadesSaoPersistidasComOAgregado() {
        var alterado = rafael.adicionarEspecialidade(new Especialidade("Musculação"))
                .adicionarEspecialidade(new Especialidade("Pilates"))
                .adicionarDisponibilidade(DayOfWeek.MONDAY, LocalTime.parse("08:00"), LocalTime.parse("12:00"))
                .adicionarDisponibilidade(DayOfWeek.TUESDAY, LocalTime.parse("14:00"), LocalTime.parse("18:00"));
        profissionais.alterar(alterado);
        var salvo = recarregar();
        assertEquals(List.of("Musculação", "Pilates"), salvo.getEspecialidades().stream().map(Especialidade::getNome).toList());
        assertEquals(Duration.ofHours(8), salvo.cargaHorariaSemanal());
        assertTrue(salvo.getDisponibilidades().stream().allMatch(item -> item.getIdentificador().isPresent()));
        assertTrue(salvo.personalTrainer());
        assertFalse(salvo.professor());
    }

    @Test
    void alterarERemoverDisponibilidadeMantemOsIdentificadores() {
        profissionais.alterar(rafael
                .adicionarDisponibilidade(DayOfWeek.MONDAY, LocalTime.parse("08:00"), LocalTime.parse("12:00"))
                .adicionarDisponibilidade(DayOfWeek.TUESDAY, LocalTime.parse("08:00"), LocalTime.parse("12:00")));
        var salvo = recarregar();
        var segunda = salvo.getDisponibilidades().get(0).getIdentificador().orElseThrow();
        var terca = salvo.getDisponibilidades().get(1).getIdentificador().orElseThrow();
        profissionais.alterar(salvo.alterarDisponibilidade(segunda, DayOfWeek.MONDAY, LocalTime.parse("06:00"),
                LocalTime.parse("08:00")).removerDisponibilidade(terca));
        var atual = recarregar();
        assertEquals(1, atual.getDisponibilidades().size());
        assertEquals(Optional.of(segunda), atual.getDisponibilidades().get(0).getIdentificador());
        assertEquals(LocalTime.parse("06:00"), atual.getDisponibilidades().get(0).getInicio());
    }

    @Test
    void removerEspecialidadeApagaSomenteAquelaLinha() {
        profissionais.alterar(rafael.adicionarEspecialidade(new Especialidade("Musculação"))
                .adicionarEspecialidade(new Especialidade("Pilates")));
        profissionais.alterar(recarregar().removerEspecialidade(new Especialidade("MUSCULACAO")));
        assertEquals(List.of("Pilates"), recarregar().getEspecialidades().stream().map(Especialidade::getNome).toList());
    }

    @Test
    void cpfECrefSaoUnicosNoRepositorioENoBanco() {
        assertTrue(profissionais.existePorCpf(new Cpf("52998224725"), Optional.empty()));
        assertFalse(profissionais.existePorCpf(new Cpf("52998224725"), rafael.getIdentificador()));
        assertTrue(profissionais.existePorCref(new Cref("123456-g/pe"), Optional.empty()));
        assertThrows(DataIntegrityViolationException.class, () -> {
            profissionais.incluir(Profissional.cadastrar("Outro", new Cpf("529.982.247-25"), new Cref("654321-G/SP"),
                    "outro@academia.com", EnumSet.of(ClassificacaoProfissional.PROFESSOR)));
            entidades.flush();
        });
    }

    @Test
    void bancoImpedeDoisVinculosAtivosEntreOMesmoPar() {
        var aluno = new AlunoIdentificador(mariana.getIdentificador().toString());
        var vinculo = vinculos.incluir(VinculoPersonalAluno.vincular(rafael, aluno, HOJE));
        assertTrue(vinculos.existeAtivo(rafael.getIdentificador().orElseThrow(), aluno));
        assertTrue(vinculos.existeAtivoDoProfissional(rafael.getIdentificador().orElseThrow()));
        limparContexto();
        assertThrows(DataIntegrityViolationException.class, () -> {
            vinculos.incluir(VinculoPersonalAluno.vincular(rafael, aluno, HOJE));
            entidades.flush();
        });
        entidades.clear();
        vinculos.alterar(vinculos.bloquear(vinculo.getIdentificador().orElseThrow()).encerrar(HOJE));
        vinculos.incluir(VinculoPersonalAluno.vincular(rafael, aluno, HOJE));
        limparContexto();
        assertEquals(2, vinculosJpa.count());
    }

    @Test
    void pesquisaFiltraPorClassificacaoSituacaoECpfFormatadoEContaVinculos() {
        profissionais.incluir(Profissional.cadastrar("Camila Rocha", new Cpf("390.533.447-05"), new Cref("654321-G/SP"),
                "camila@academia.com", EnumSet.of(ClassificacaoProfissional.PROFESSOR)));
        vinculos.incluir(VinculoPersonalAluno.vincular(rafael, new AlunoIdentificador(mariana.getIdentificador().toString()),
                HOJE));
        limparContexto();
        var consultas = new ProfissionalAplicacaoRepositorio(profissionaisJpa);

        var personais = consultas.pesquisar(consulta(Optional.empty(), Optional.of(ClassificacaoProfissional.PERSONAL_TRAINER),
                Optional.empty())).getProjecoes();
        assertEquals(List.of("Rafael Souza"), personais.stream().map(item -> item.nome()).toList());
        assertEquals(1, personais.get(0).vinculosAtivos());

        var porCpf = consultas.pesquisar(consulta(Optional.of("390.533"), Optional.empty(), Optional.empty())).getProjecoes();
        assertEquals(List.of("Camila Rocha"), porCpf.stream().map(item -> item.nome()).toList());

        assertEquals(2, consultas.pesquisar(consulta(Optional.empty(), Optional.empty(),
                Optional.of(SituacaoProfissional.ATIVO))).getTotal());
        assertEquals(0, consultas.pesquisar(consulta(Optional.empty(), Optional.empty(),
                Optional.of(SituacaoProfissional.INATIVO))).getTotal());
    }

    private static ProfissionalConsulta consulta(Optional<String> busca, Optional<ClassificacaoProfissional> classificacao,
            Optional<SituacaoProfissional> situacao) {
        return new ProfissionalConsulta(Optional.of(ProfissionalOrdenacao.NOME), Optional.empty(), Optional.empty(),
                Optional.empty(), busca, classificacao, situacao);
    }
}

package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.IAlunoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.spi.IProfissionalRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.vinculopersonal.SituacaoVinculo;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.vinculopersonal.VinculoPersonalAluno;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.vinculopersonal.VinculoPersonalAlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.vinculopersonal.VinculoPersonalAlunoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.vinculopersonal.spi.IVinculoPersonalAlunoRepositorio;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

/** Repositórios em memória simulam a persistência; os serviços de domínio são os reais. */
public final class ProfissionaisStepDefinitions {
    private static final DayOfWeek[] DIAS_UTEIS = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY};

    private final ISegurancaServico seguranca = mock(ISegurancaServico.class);
    private final IProfissionalRepositorio profissionais = mock(IProfissionalRepositorio.class);
    private final IVinculoPersonalAlunoRepositorio vinculos = mock(IVinculoPersonalAlunoRepositorio.class);
    private final IAlunoRepositorio alunos = mock(IAlunoRepositorio.class);
    private final Clock relogio = mock(Clock.class);
    private final Map<String, Profissional> profissionaisCadastrados = new LinkedHashMap<>();
    private final Map<String, VinculoPersonalAluno> vinculosCadastrados = new LinkedHashMap<>();
    private final Set<String> alunosCadastrados = new HashSet<>();
    private final ProfissionalServico servico = new ProfissionalServico(seguranca, profissionais, vinculos);
    private final VinculoPersonalAlunoServico servicoVinculo = new VinculoPersonalAlunoServico(seguranca, vinculos,
            profissionais, alunos, relogio);
    private String papel = Autorizacao.ADMINISTRADOR.name();
    private Instant agora = Instant.parse("2026-11-17T15:00:00Z");
    private int proximoIdentificador = 1;
    private long proximaDisponibilidade = 1;
    private RuntimeException falha;

    public ProfissionaisStepDefinitions() {
        when(relogio.instant()).thenAnswer(chamada -> agora);
        when(seguranca.getUsuario()).thenReturn("usuario");
        when(seguranca.possuiAutorizacao(anyString())).thenAnswer(chamada -> papel.equals(chamada.getArgument(0)));
        when(alunos.existePorIdentificador(any())).thenAnswer(
                chamada -> alunosCadastrados.contains(chamada.getArgument(0).toString()));
        configurarProfissionais();
        configurarVinculos();
    }

    private void configurarProfissionais() {
        when(profissionais.existePorIdentificador(any())).thenAnswer(
                chamada -> profissionaisCadastrados.containsKey(chamada.getArgument(0).toString()));
        when(profissionais.buscarPorIdentificador(any())).thenAnswer(
                chamada -> profissionaisCadastrados.get(chamada.getArgument(0).toString()));
        when(profissionais.bloquear(any())).thenAnswer(chamada -> profissionaisCadastrados.get(chamada.getArgument(0).toString()));
        when(profissionais.incluir(any())).thenAnswer(chamada -> guardar(chamada.getArgument(0),
                Optional.of(new ProfissionalIdentificador(String.valueOf(proximoIdentificador++)))));
        doAnswer(chamada -> {
            Profissional alterado = chamada.getArgument(0);
            guardar(alterado, alterado.getIdentificador());
            return null;
        }).when(profissionais).alterar(any());
        when(profissionais.existePorCpf(any(), any())).thenAnswer(chamada -> profissionaisCadastrados.values().stream()
                .anyMatch(item -> item.getCpf().equals(chamada.getArgument(0))
                        && !item.getIdentificador().equals(chamada.getArgument(1))));
        when(profissionais.existePorCref(any(), any())).thenAnswer(chamada -> profissionaisCadastrados.values().stream()
                .anyMatch(item -> item.getCref().equals(chamada.getArgument(0))
                        && !item.getIdentificador().equals(chamada.getArgument(1))));
    }

    private void configurarVinculos() {
        when(vinculos.existePorIdentificador(any())).thenAnswer(
                chamada -> vinculosCadastrados.containsKey(chamada.getArgument(0).toString()));
        when(vinculos.buscarPorIdentificador(any())).thenAnswer(chamada -> vinculosCadastrados.get(chamada.getArgument(0).toString()));
        when(vinculos.bloquear(any())).thenAnswer(chamada -> vinculosCadastrados.get(chamada.getArgument(0).toString()));
        when(vinculos.incluir(any())).thenAnswer(chamada -> {
            VinculoPersonalAluno novo = chamada.getArgument(0);
            var salvo = new VinculoPersonalAluno(
                    Optional.of(new VinculoPersonalAlunoIdentificador(String.valueOf(proximoIdentificador++))),
                    novo.getProfissional(), novo.getAluno(), novo.getInicio(), novo.getEncerramento(), novo.getSituacao());
            vinculosCadastrados.put(salvo.getIdentificador().orElseThrow().toString(), salvo);
            return salvo;
        });
        doAnswer(chamada -> {
            VinculoPersonalAluno alterado = chamada.getArgument(0);
            vinculosCadastrados.put(alterado.getIdentificador().orElseThrow().toString(), alterado);
            return null;
        }).when(vinculos).alterar(any());
        when(vinculos.existeAtivo(any(), any())).thenAnswer(chamada -> vinculosCadastrados.values().stream()
                .anyMatch(item -> item.ativo() && item.getProfissional().equals(chamada.getArgument(0))
                        && item.getAluno().equals(chamada.getArgument(1))));
        when(vinculos.existeAtivoDoProfissional(any())).thenAnswer(chamada -> vinculosCadastrados.values().stream()
                .anyMatch(item -> item.ativo() && item.getProfissional().equals(chamada.getArgument(0))));
    }

    /** Simula a chave gerada pelo banco para disponibilidades novas. */
    private Profissional guardar(Profissional profissional, Optional<ProfissionalIdentificador> identificador) {
        var disponibilidades = new ArrayList<DisponibilidadeProfissional>();
        for (var disponibilidade : profissional.getDisponibilidades()) {
            disponibilidades.add(disponibilidade.getIdentificador().isPresent() ? disponibilidade
                    : new DisponibilidadeProfissional(Optional.of(proximaDisponibilidade++), disponibilidade.getDia(),
                            disponibilidade.getInicio(), disponibilidade.getFim()));
        }
        var salvo = new Profissional(identificador, profissional.getNome(), profissional.getCpf(), profissional.getCref(),
                profissional.getContato(), profissional.getClassificacoes(), profissional.getEspecialidades(),
                disponibilidades, profissional.getSituacao());
        profissionaisCadastrados.put(identificador.orElseThrow().toString(), salvo);
        return salvo;
    }

    private void executar(Runnable acao) {
        try {
            acao.run();
        } catch (RuntimeException excecao) {
            falha = excecao;
        }
    }

    private static Set<ClassificacaoProfissional> classificacoes(String texto) {
        var resultado = EnumSet.noneOf(ClassificacaoProfissional.class);
        for (var item : texto.split(",")) {
            if (!item.isBlank() && !item.strip().equals("NENHUMA")) {
                resultado.add(ClassificacaoProfissional.valueOf(item.strip()));
            }
        }
        return resultado;
    }

    private Profissional profissional(String nome) {
        return profissionaisCadastrados.values().stream().filter(item -> item.getNome().equals(nome)).findFirst().orElseThrow();
    }

    private ProfissionalIdentificador identificador(String nome) {
        return profissional(nome).getIdentificador().orElseThrow();
    }

    private long disponibilidadeDe(String nome, String dia) {
        return profissional(nome).getDisponibilidades().stream()
                .filter(item -> item.getDia() == DayOfWeek.valueOf(dia)).findFirst().orElseThrow()
                .getIdentificador().orElseThrow();
    }

    private Profissional cadastrar(String nome, String cpf, String cref, String classificacao) {
        return servico.cadastrar(nome, new Cpf(cpf), new Cref(cref), "(81) 99999-0000", classificacoes(classificacao));
    }

    @Dado("que estou autenticado como administrador da equipe")
    public void autenticarAdministrador() {
        papel = Autorizacao.ADMINISTRADOR.name();
    }

    @Dado("que estou autenticado como personal na equipe")
    public void autenticarPersonal() {
        papel = Autorizacao.PERSONAL.name();
    }

    @Dado("que hoje é {string} para a equipe")
    public void definirHoje(String data) {
        agora = LocalDate.parse(data).atTime(12, 0).atZone(ParametrosAcademia.FUSO).toInstant();
    }

    @Dado("que existe o aluno {string} para a equipe")
    public void cadastrarAluno(String aluno) {
        alunosCadastrados.add(aluno);
    }

    @Dado("o profissional {string} com CPF {string}, CREF {string} e classificação {string}")
    public void cadastrarExistente(String nome, String cpf, String cref, String classificacao) {
        cadastrar(nome, cpf, cref, classificacao);
    }

    @Dado("que o profissional {string} possui a especialidade {string}")
    public void especialidadeExistente(String nome, String especialidade) {
        servico.adicionarEspecialidade(identificador(nome), new Especialidade(especialidade));
    }

    @Dado("que o profissional {string} está disponível {string} das {string} às {string}")
    public void disponibilidadeExistente(String nome, String dia, String inicio, String fim) {
        servico.adicionarDisponibilidade(identificador(nome), DayOfWeek.valueOf(dia), LocalTime.parse(inicio),
                LocalTime.parse(fim));
    }

    @Dado("que o profissional {string} trabalha das {string} às {string} de segunda a sexta")
    public void jornadaExistente(String nome, String inicio, String fim) {
        for (var dia : DIAS_UTEIS) {
            disponibilidadeExistente(nome, dia.name(), inicio, fim);
        }
    }

    @Dado("que o profissional {string} foi inativado")
    public void inativarExistente(String nome) {
        servico.inativar(identificador(nome));
    }

    @Dado("que o aluno {string} está vinculado ao profissional {string}")
    public void vinculoExistente(String aluno, String nome) {
        servicoVinculo.vincular(identificador(nome), new AlunoIdentificador(aluno));
    }

    @Quando("eu cadastrar o profissional {string} com CPF {string}, CREF {string} e classificação {string}")
    public void cadastrarProfissional(String nome, String cpf, String cref, String classificacao) {
        executar(() -> cadastrar(nome, cpf, cref, classificacao));
    }

    @Quando("eu alterar o contato do profissional {string} para {string}")
    public void alterarContato(String nome, String contato) {
        var atual = profissional(nome);
        executar(() -> servico.editar(identificador(nome), atual.getNome(), atual.getCpf(), atual.getCref(), contato,
                atual.getClassificacoes()));
    }

    @Quando("eu alterar a classificação do profissional {string} para {string}")
    public void alterarClassificacao(String nome, String classificacao) {
        var atual = profissional(nome);
        executar(() -> servico.editar(identificador(nome), atual.getNome(), atual.getCpf(), atual.getCref(),
                atual.getContato(), classificacoes(classificacao)));
    }

    @Quando("eu adicionar a especialidade {string} ao profissional {string}")
    public void adicionarEspecialidade(String especialidade, String nome) {
        executar(() -> servico.adicionarEspecialidade(identificador(nome), new Especialidade(especialidade)));
    }

    @Quando("eu remover a especialidade {string} do profissional {string}")
    public void removerEspecialidade(String especialidade, String nome) {
        executar(() -> servico.removerEspecialidade(identificador(nome), new Especialidade(especialidade)));
    }

    @Quando("eu adicionar a disponibilidade {string} das {string} às {string} ao profissional {string}")
    public void adicionarDisponibilidade(String dia, String inicio, String fim, String nome) {
        executar(() -> servico.adicionarDisponibilidade(identificador(nome), DayOfWeek.valueOf(dia), LocalTime.parse(inicio),
                LocalTime.parse(fim)));
    }

    @Quando("eu alterar a disponibilidade de {string} do profissional {string} para {string} das {string} às {string}")
    public void alterarDisponibilidade(String diaAtual, String nome, String dia, String inicio, String fim) {
        var disponibilidade = disponibilidadeDe(nome, diaAtual);
        executar(() -> servico.alterarDisponibilidade(identificador(nome), disponibilidade, DayOfWeek.valueOf(dia),
                LocalTime.parse(inicio), LocalTime.parse(fim)));
    }

    @Quando("eu remover a disponibilidade de {string} do profissional {string}")
    public void removerDisponibilidade(String dia, String nome) {
        var disponibilidade = disponibilidadeDe(nome, dia);
        executar(() -> servico.removerDisponibilidade(identificador(nome), disponibilidade));
    }

    @Quando("eu inativar o profissional {string}")
    public void inativar(String nome) {
        executar(() -> servico.inativar(identificador(nome)));
    }

    @Quando("eu reativar o profissional {string}")
    public void reativar(String nome) {
        executar(() -> servico.reativar(identificador(nome)));
    }

    @Quando("eu vincular o aluno {string} ao profissional {string}")
    public void vincular(String aluno, String nome) {
        executar(() -> servicoVinculo.vincular(identificador(nome), new AlunoIdentificador(aluno)));
    }

    @Quando("eu encerrar o vínculo do aluno {string} com o profissional {string}")
    public void encerrarVinculo(String aluno, String nome) {
        var vinculo = vinculosCadastrados.values().stream()
                .filter(item -> item.ativo() && item.getAluno().equals(new AlunoIdentificador(aluno))
                        && item.getProfissional().equals(identificador(nome)))
                .findFirst().orElseThrow();
        executar(() -> servicoVinculo.encerrar(vinculo.getIdentificador().orElseThrow()));
    }

    @Então("a operação da equipe é rejeitada com a mensagem {string}")
    public void verificarRejeicao(String mensagem) {
        assertNotNull(falha, "A operação deveria ter sido rejeitada");
        assertEquals(mensagem, falha.getMessage());
    }

    @Então("o profissional {string} fica {string} com CPF {string} e CREF {string}")
    public void verificarProfissional(String nome, String situacao, String cpf, String cref) {
        assertNull(falha, () -> falha.getMessage());
        var profissional = profissional(nome);
        assertEquals(SituacaoProfissional.valueOf(situacao), profissional.getSituacao());
        assertEquals(cpf, profissional.getCpf().toString());
        assertEquals(cref, profissional.getCref().toString());
    }

    @Então("o profissional {string} possui as classificações {string}")
    public void verificarClassificacoes(String nome, String classificacao) {
        assertNull(falha, () -> falha.getMessage());
        assertEquals(classificacoes(classificacao), profissional(nome).getClassificacoes());
    }

    @Então("o profissional {string} tem o contato {string}")
    public void verificarContato(String nome, String contato) {
        assertNull(falha, () -> falha.getMessage());
        assertEquals(contato, profissional(nome).getContato());
    }

    @Então("o profissional {string} possui as especialidades {string}")
    public void verificarEspecialidades(String nome, String especialidades) {
        assertNull(falha, () -> falha.getMessage());
        var nomes = profissional(nome).getEspecialidades().stream().map(Especialidade::getNome).toList();
        assertEquals(java.util.List.of(especialidades.split(",")), nomes);
    }

    @Então("a carga horária semanal do profissional {string} é de {int} horas")
    public void verificarCarga(String nome, int horas) {
        assertNull(falha, () -> falha.getMessage());
        assertEquals(Duration.ofHours(horas), profissional(nome).cargaHorariaSemanal());
    }

    @Então("o aluno {string} tem {int} vínculo {string} com o profissional {string}")
    public void verificarVinculos(String aluno, int quantidade, String situacao, String nome) {
        assertNull(falha, () -> falha.getMessage());
        assertEquals(quantidade, vinculosCadastrados.values().stream()
                .filter(item -> item.getAluno().equals(new AlunoIdentificador(aluno))
                        && item.getProfissional().equals(identificador(nome))
                        && item.getSituacao() == SituacaoVinculo.valueOf(situacao))
                .count());
    }
}

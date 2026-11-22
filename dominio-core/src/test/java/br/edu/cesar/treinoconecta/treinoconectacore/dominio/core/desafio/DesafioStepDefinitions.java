package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.desafio;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import io.cucumber.java.pt.*;

public class DesafioStepDefinitions {
    private VigenciaDesafio vigencia;
    private RuntimeException falha;
    private boolean permitido;
    private boolean bloqueada;

    @Quando("criar um desafio com início {string} e término {string}")
    public void criar(String inicio, String fim) { try { vigencia=new VigenciaDesafio(LocalDate.parse(inicio),LocalDate.parse(fim)); } catch (RuntimeException e) { falha=e; } }
    @Dado("um desafio de {string} a {string}")
    public void desafio(String inicio, String fim) { vigencia=new VigenciaDesafio(LocalDate.parse(inicio),LocalDate.parse(fim)); }
    @Quando("verificar inscrição em {string}")
    public void verificarInscricao(String data) { permitido=vigencia.aceitaInscricao(LocalDate.parse(data)); }
    @Dado("avaliação registrada em {string}")
    public void avaliacao(String registro) { vigencia=null; this.registro=OffsetDateTime.parse(registro); }
    private OffsetDateTime registro;
    @Quando("verificar às {string}")
    public void verificarAvaliacao(String agora) { bloqueada=!VigenciaDesafio.avaliacaoEditavel(registro,OffsetDateTime.parse(agora)); }
    @Então("o desafio é válido") public void desafioValido() { assertNotNull(vigencia); assertNull(falha); }
    @Então("o desafio é rejeitado") public void desafioInvalido() { assertNotNull(falha); }
    @Então("a inscrição está aberta") public void inscricaoAberta() { assertTrue(permitido); }
    @Então("a inscrição está encerrada") public void inscricaoEncerrada() { assertFalse(permitido); }
    @Então("a avaliação está bloqueada") public void avaliacaoBloqueada() { assertTrue(bloqueada); }
}

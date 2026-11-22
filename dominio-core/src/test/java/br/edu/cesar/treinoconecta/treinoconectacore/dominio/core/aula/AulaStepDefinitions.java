package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aula;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalTime;
import io.cucumber.java.pt.*;

public class AulaStepDefinitions {
    private PeriodoAula aula;
    private boolean sobrepoe;
    private RuntimeException falha;

    @Dado("uma aula das {string} às {string}")
    public void aula(String inicio, String fim) { aula = new PeriodoAula(LocalTime.parse(inicio), LocalTime.parse(fim)); }

    @Quando("comparar com uma aula das {string} às {string}")
    public void comparar(String inicio, String fim) { sobrepoe = aula.sobrepoe(new PeriodoAula(LocalTime.parse(inicio), LocalTime.parse(fim))); }

    @Quando("criar uma aula das {string} às {string}")
    public void criar(String inicio, String fim) { try { new PeriodoAula(LocalTime.parse(inicio), LocalTime.parse(fim)); } catch (RuntimeException e) { falha=e; } }

    @Então("os horários se sobrepõem") public void sobreposicao() { assertTrue(sobrepoe); }
    @Então("os horários não se sobrepõem") public void semSobreposicao() { assertFalse(sobrepoe); }
    @Então("o intervalo é inválido") public void intervaloInvalido() { assertNotNull(falha); }
}

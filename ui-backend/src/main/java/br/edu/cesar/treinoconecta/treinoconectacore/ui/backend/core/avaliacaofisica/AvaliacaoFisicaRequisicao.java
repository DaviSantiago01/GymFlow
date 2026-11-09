package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.avaliacaofisica;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.MedidaCorporal;

public record AvaliacaoFisicaRequisicao(AlunoIdentificador aluno, LocalDate data, BigDecimal peso, BigDecimal altura,
        List<MedidaCorporal> medidas) { }

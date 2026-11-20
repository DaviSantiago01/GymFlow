package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.registroacesso;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.registroacesso.RegistroAcessoConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.registroacesso.RegistroAcessoOrdenacao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.registroacesso.RegistroAcessoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.registroacesso.spi.IRegistroAcessoAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoAplicacaoRepositorioAbstrato;

public final class RegistroAcessoAplicacaoRepositorio
        extends AgregadoAplicacaoRepositorioAbstrato<RegistroAcessoProjecao, RegistroAcessoOrdenacao, RegistroAcessoConsulta>
        implements IRegistroAcessoAplicacaoRepositorio {
    private final IRegistroAcessoRepositorioJpa jpa;

    public RegistroAcessoAplicacaoRepositorio(IRegistroAcessoRepositorioJpa jpa) {
        this.jpa = Objects.requireNonNull(jpa);
    }

    /** As datas da consulta são inclusivas e interpretadas no fuso da academia. */
    @Override
    protected Page<RegistroAcessoProjecao> pesquisar(RegistroAcessoConsulta consulta, Pageable pageable) {
        Instant inicio = consulta.getInicio().map(data -> data.atStartOfDay(ParametrosAcademia.FUSO).toInstant()).orElse(null);
        Instant fim = consulta.getFim().map(data -> data.plusDays(1).atStartOfDay(ParametrosAcademia.FUSO).toInstant())
                .orElse(null);
        Long aluno = consulta.getAluno().map(item -> Long.valueOf(item.toString())).orElse(null);
        return jpa.pesquisar(aluno, inicio, fim, pageable);
    }

    @Override
    protected String paraPropriedadeOrdenacao(RegistroAcessoOrdenacao ordenacao) {
        return switch (ordenacao) {
        case ENTRADA -> "entrada";
        };
    }

    @Override
    public List<RegistroAcessoProjecao> presentes() {
        return jpa.presentes();
    }

    @Override
    public List<RegistroAcessoProjecao> doAluno(AlunoIdentificador aluno, Instant inicio, Instant fim) {
        return jpa.doAluno(Long.valueOf(aluno.toString()), inicio, fim);
    }
}

package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.desafio;

import java.math.BigDecimal;import java.time.LocalDate;import java.util.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aulasdesafios.AulasDesafiosDados;import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.desafio.spi.IDesafioAplicacaoRepositorio;import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.desafio.*;import dev.sauloaraujo.d5.framework.aplicacao.impl.AgregadoAplicacaoServicoAbstrato;import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;import dev.sauloaraujo.d5.framework.dominio.spi.NaoAutorizadoExcecao;

public final class DesafioAplicacaoServico extends AgregadoAplicacaoServicoAbstrato<DesafioIdentificador,Desafio,DesafioAutorizacoes,AulasDesafiosDados.DesafioLinha,DesafioConsulta>{
 private final IDesafioAplicacaoRepositorio consultas;private final DesafioServico dominio;private final ISegurancaServico seguranca;
 public DesafioAplicacaoServico(IDesafioAplicacaoRepositorio consultas,DesafioServico dominio,ISegurancaServico seguranca){super(consultas,dominio);this.consultas=Objects.requireNonNull(consultas);this.dominio=Objects.requireNonNull(dominio);this.seguranca=Objects.requireNonNull(seguranca);}
 public List<AulasDesafiosDados.PessoaLinha> alunos(){validarGestor();return consultas.alunos();}public List<AulasDesafiosDados.DesafioLinha> desafios(){return consultas.desafios();}
 public List<AulasDesafiosDados.AvaliacaoLinha> avaliacoes(long id){validarGestor();return consultas.avaliacoes(id);}
 public List<AulasDesafiosDados.EvolucaoLinha> evolucao(long id){return consultas.evolucao(id,gestor()?"":seguranca.getUsuario());}
 public AulasDesafiosDados.DesafioLinha criar(String titulo,String indicador,LocalDate inicio,LocalDate fim){var d=dominio.criar(new DadosDesafio(titulo,IndicadorDesafio.valueOf(indicador),new VigenciaDesafio(inicio,fim)));return consultas.desafio(Long.parseLong(d.getIdentificador().orElseThrow().toString())).orElseThrow();}
 public void inscrever(long id,String indicador,BigDecimal meta){dominio.inscrever(new DesafioIdentificador(Long.toString(id)),indicador,meta);}
 public void registrarAvaliacao(long desafio,long aluno,long profissional,String tipo,BigDecimal peso,BigDecimal cintura,BigDecimal gordura){dominio.registrarAvaliacao(new DesafioIdentificador(Long.toString(desafio)),new br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador(Long.toString(aluno)),new br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.ProfissionalIdentificador(Long.toString(profissional)),TipoAvaliacaoDesafio.valueOf(tipo),peso,cintura,gordura);}
 public void editarAvaliacao(long id,BigDecimal peso,BigDecimal cintura,BigDecimal gordura){dominio.editarAvaliacao(id,peso,cintura,gordura);}public void excluirAvaliacao(long id){dominio.excluirAvaliacao(id);}public void encerrar(long id){dominio.encerrar(new DesafioIdentificador(Long.toString(id)));}
 private boolean gestor(){return seguranca.possuiAutorizacao(Autorizacao.ADMINISTRADOR.name())||seguranca.possuiAutorizacao(Autorizacao.PERSONAL.name());}
 private void validarGestor(){if(!gestor())throw new NaoAutorizadoExcecao("Ação disponível para gestor ou profissional autorizado.");}
}

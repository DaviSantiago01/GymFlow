package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aula;

import java.time.*;import java.util.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aula.spi.IAulaAplicacaoRepositorio;import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aulasdesafios.*;import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aula.*;import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.ProfissionalIdentificador;import dev.sauloaraujo.d5.framework.aplicacao.impl.AgregadoAplicacaoServicoAbstrato;import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;import dev.sauloaraujo.d5.framework.dominio.spi.NaoAutorizadoExcecao;

public final class AulaAplicacaoServico extends AgregadoAplicacaoServicoAbstrato<AulaIdentificador,Aula,AulaAutorizacoes,AulasDesafiosDados.AulaLinha,AulasDesafiosConsulta>{
 private final IAulaAplicacaoRepositorio consultas;private final AulaServico dominio;private final ISegurancaServico seguranca;
 public AulaAplicacaoServico(IAulaAplicacaoRepositorio consultas,AulaServico dominio,ISegurancaServico seguranca){super(consultas,dominio);this.consultas=Objects.requireNonNull(consultas);this.dominio=Objects.requireNonNull(dominio);this.seguranca=Objects.requireNonNull(seguranca);}
 public List<AulasDesafiosDados.AulaLinha> aulas(Optional<LocalDate>d,Optional<String>m,Optional<String>b){return consultas.aulas(d,m,b,seguranca.getUsuario());}
 public AulasDesafiosDados.AulaLinha aula(long id){return consultas.aula(id,seguranca.getUsuario()).orElseThrow(()->new NoSuchElementException("Aula não encontrada"));}
 public List<AulasDesafiosDados.InscricaoLinha> minhasAulas(){return consultas.minhasAulas(seguranca.getUsuario());}
 public List<AulasDesafiosDados.InscricaoLinha> inscritos(long id){validarGestor();return consultas.inscritos(id);}
 public List<AulasDesafiosDados.NotificacaoLinha> notificacoes(){return consultas.notificacoes(gestor()?"":seguranca.getUsuario());}
 public List<AulasDesafiosDados.PessoaLinha> professores(){return consultas.professores();}
 public AulasDesafiosDados.AulaLinha cadastrar(String titulo,String modalidade,Optional<String>descricao,LocalDate data,LocalTime inicio,LocalTime fim,int limite,long professor){var a=dominio.cadastrar(new DadosAula(titulo,modalidade,descricao,data,new PeriodoAula(inicio,fim),limite,new ProfissionalIdentificador(Long.toString(professor))));return aula(Long.parseLong(a.getIdentificador().orElseThrow().toString()));}
 public AulasDesafiosDados.AulaLinha editar(long id,String titulo,String modalidade,Optional<String>descricao,LocalDate data,LocalTime inicio,LocalTime fim,int limite,long professor){dominio.editar(new AulaIdentificador(Long.toString(id)),new DadosAula(titulo,modalidade,descricao,data,new PeriodoAula(inicio,fim),limite,new ProfissionalIdentificador(Long.toString(professor))));return aula(id);}
 public AulasDesafiosDados.AulaLinha cancelar(long id){dominio.cancelar(new AulaIdentificador(Long.toString(id)));return aula(id);}
 public void inscrever(long id){dominio.inscrever(new AulaIdentificador(Long.toString(id)));}public void cancelarInscricao(long id){dominio.cancelarInscricao(id);}public void cancelarInscricaoGestao(long id){dominio.cancelarInscricaoGestao(id);}
 private boolean gestor(){return seguranca.possuiAutorizacao(Autorizacao.ADMINISTRADOR.name())||seguranca.possuiAutorizacao(Autorizacao.PERSONAL.name());}
 private void validarGestor(){if(!gestor())throw new NaoAutorizadoExcecao("Ação disponível para gestor ou profissional autorizado.");}
}

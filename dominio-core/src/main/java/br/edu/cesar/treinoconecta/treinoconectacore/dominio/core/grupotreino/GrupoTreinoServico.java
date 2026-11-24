package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.grupotreino;

import java.time.*;
import java.util.*;
import java.util.function.BiConsumer;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.academia.AcademiaIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.IAlunoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aula.spi.IAulaRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.grupotreino.spi.IGrupoTreinoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.Profissional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.ProfissionalIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.spi.IProfissionalRepositorio;
import dev.sauloaraujo.d5.framework.dominio.impl.*;
import dev.sauloaraujo.d5.framework.dominio.spi.*;
public final class GrupoTreinoServico extends AgregadoServicoAbstrato<GrupoTreinoIdentificador,GrupoTreino,AgregadoAutorizacoes<GrupoTreino>> {
    public static final String SEM_VAGAS="Não há vagas disponíveis nesse horário. Escolha outro horário.";
    public static final String PROFESSOR_INVALIDO="Selecione um professor ativo cadastrado.";
    public static final String PROFESSOR_FORA_DA_JORNADA="O professor escolhido não trabalha nesse horário.";
    public static final String PROFESSOR_EM_OUTRO_TREINO="O professor escolhido já conduz outro treino de grupo nesse horário.";
    public static final String PROFESSOR_EM_AULA="O professor escolhido tem uma aula nesse horário.";
    private final IGrupoTreinoRepositorio grupos;private final IAlunoRepositorio alunos;private final IProfissionalRepositorio profissionais;private final IAulaRepositorio aulas;private final Clock relogio;
    public GrupoTreinoServico(ISegurancaServico seguranca,IGrupoTreinoRepositorio grupos,IAlunoRepositorio alunos,IProfissionalRepositorio profissionais,IAulaRepositorio aulas,Clock relogio){super(seguranca,grupos,"Grupo %s existente","Grupo %s inexistente");this.grupos=Objects.requireNonNull(grupos);this.alunos=Objects.requireNonNull(alunos);this.profissionais=Objects.requireNonNull(profissionais);this.aulas=Objects.requireNonNull(aulas);this.relogio=Objects.requireNonNull(relogio);}
    public Aluno autenticado(){if(!getSegurancaServico().possuiAutorizacao("USUARIO"))throw new NaoAutorizadoExcecao("Acesso reservado ao aluno");return alunos.buscarPorUsuario(getSegurancaServico().getUsuario());}
    public AgregadoAutorizacoes<GrupoTreino> criarAutorizacoes(Optional<GrupoTreinoIdentificador> id){boolean pode=getSegurancaServico().possuiAutorizacao("USUARIO");if(pode && id.isPresent())pode=grupos.buscarPorIdentificador(id.get()).podeConsultar(autenticado().getIdentificador().orElseThrow());return new AgregadoAutorizacoes<>(false,pode,false,false);}
    public GrupoTreino criar(NomeGrupo nome,List<AlunoIdentificador> convidados){var autor=autenticado();var agora=relogio.instant();var g=GrupoTreino.criar(nome,autor,agora);for(var id:List.copyOf(convidados))g.convidar(autor.getIdentificador().orElseThrow(),alunos.buscarPorIdentificador(id),agora);return grupos.incluir(g);}
    private void executar(GrupoTreinoIdentificador id,BiConsumer<GrupoTreino,Instant> acao){var g=grupos.bloquear(id);acao.accept(g,relogio.instant());grupos.alterar(g);}
    public void renomear(GrupoTreinoIdentificador id,NomeGrupo nome){var a=autenticado().getIdentificador().orElseThrow();executar(id,(g,t)->g.renomear(a,nome));}
    public void convidar(GrupoTreinoIdentificador id,AlunoIdentificador destinatario){var a=autenticado().getIdentificador().orElseThrow();executar(id,(g,t)->{g.exigirOrganizador(a);g.convidar(a,alunos.buscarPorIdentificador(destinatario),t);});}
    public void responderGrupo(GrupoTreinoIdentificador id,long convite,boolean aceitar){var a=autenticado();executar(id,(g,t)->g.responderConvite(convite,a,aceitar,t));}

    /**
     * Propõe um treino em um horário fixo com um professor. O horário precisa ter vaga na academia e o professor precisa
     * trabalhar durante todo o horário, sem outro treino de grupo nem aula ao mesmo tempo.
     */
    public void propor(GrupoTreinoIdentificador id,Instant inicio,ProfissionalIdentificador professor){
        var a=autenticado().getIdentificador().orElseThrow();
        var horario=new HorarioTreinoGrupo(inicio);
        Objects.requireNonNull(professor,"Escolha o professor do treino");
        executar(id,(g,t)->{
            g.exigirOrganizador(a);
            Validate.isTrue(horario.aceitaProposta(t),"O treino deve começar daqui a mais de uma hora, pois a confirmação encerra uma hora antes");
            grupos.bloquearAgenda(g.getAcademia());
            Validate.validState(vagasRestantes(g.getAcademia(),horario)>0,SEM_VAGAS);
            Validate.validState(profissionais.existePorIdentificador(professor),PROFESSOR_INVALIDO);
            var escolhido=profissionais.bloquear(professor);
            Validate.validState(escolhido.ativo() && escolhido.professor(),PROFESSOR_INVALIDO);
            Validate.validState(horario.dentroDaJornada(escolhido),PROFESSOR_FORA_DA_JORNADA);
            Validate.validState(!grupos.professorOcupado(professor,horario.inicio(),horario.fim()),PROFESSOR_EM_OUTRO_TREINO);
            Validate.validState(!emAula(professor,horario),PROFESSOR_EM_AULA);
            g.propor(a,horario,professor,t);
        });
    }

    /** Vagas que ainda restam no horário para treinos de grupo da academia. */
    public int vagasRestantes(AcademiaIdentificador academia,HorarioTreinoGrupo horario){
        return Math.max(0,ParametrosAcademia.VAGAS_TREINO_GRUPO_POR_HORARIO-grupos.contarTreinosNoHorario(academia,horario.inicio(),horario.fim()));
    }

    /** Professores ativos que trabalham durante todo o horário e estão livres de treinos de grupo e aulas. */
    public List<Profissional> professoresDisponiveis(HorarioTreinoGrupo horario){
        return profissionais.professoresAtivos().stream().filter(p->horario.dentroDaJornada(p))
                .filter(p->{var id=p.getIdentificador().orElseThrow();return !grupos.professorOcupado(id,horario.inicio(),horario.fim()) && !emAula(id,horario);})
                .toList();
    }

    /** Grupo que o aluno autenticado organiza e que ainda está ativo, para montar a agenda de propostas. */
    public GrupoTreino organizado(GrupoTreinoIdentificador id){var a=autenticado().getIdentificador().orElseThrow();var g=grupos.buscarPorIdentificador(id);g.exigirOrganizador(a);return g;}

    public Instant agora(){return relogio.instant();}

    private boolean emAula(ProfissionalIdentificador professor,HorarioTreinoGrupo horario){
        return aulas.existeChoque(professor,horario.data(),horario.horaInicio(),horario.horaFim(),Optional.empty());
    }

    public void responderTreino(GrupoTreinoIdentificador id,long treino,long convite,boolean confirmar){var a=autenticado().getIdentificador().orElseThrow();executar(id,(g,t)->g.responderTreino(treino,convite,a,confirmar,t));}
    public void cancelarPresenca(GrupoTreinoIdentificador id,long treino,long convite){var a=autenticado().getIdentificador().orElseThrow();executar(id,(g,t)->g.cancelarPresenca(treino,convite,a,t));}
    public void cancelarTreino(GrupoTreinoIdentificador id,long treino){var a=autenticado().getIdentificador().orElseThrow();executar(id,(g,t)->g.cancelarTreino(treino,a,t));}
    public void sair(GrupoTreinoIdentificador id){var a=autenticado().getIdentificador().orElseThrow();executar(id,(g,t)->g.sair(a,t));}
    public void encerrar(GrupoTreinoIdentificador id){var a=autenticado().getIdentificador().orElseThrow();executar(id,(g,t)->g.encerrar(a,t));}
}

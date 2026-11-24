package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.grupotreino;

import java.time.Instant;
import java.util.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.ProfissionalIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;
import org.apache.commons.lang3.Validate;
/** Treino de grupo. O professor é obrigatório nas novas propostas; treinos anteriores à regra podem não ter professor. */
public record TreinoGrupo(Optional<Long> identificador,Instant inicio,Instant prazo,Instant criacao,Optional<Instant> cancelamento,List<ConviteTreino> convites,Optional<ProfissionalIdentificador> professor) {
    public TreinoGrupo {Objects.requireNonNull(identificador);Objects.requireNonNull(inicio);Objects.requireNonNull(prazo);Objects.requireNonNull(criacao);Objects.requireNonNull(cancelamento);Objects.requireNonNull(professor);convites=List.copyOf(convites);Validate.isTrue(criacao.isBefore(prazo) && prazo.isBefore(inicio),"O prazo deve ser posterior ao agendamento e anterior ao treino");Validate.isTrue(convites.stream().map(ConviteTreino::aluno).distinct().count()==convites.size(),"Convites duplicados no treino");}
    /** Cria um convite por membro; o organizador, que propôs o treino, já fica com a presença confirmada. */
    public static TreinoGrupo propor(HorarioTreinoGrupo horario,ProfissionalIdentificador professor,AlunoIdentificador organizador,Instant agora,List<AlunoIdentificador> membros){
        Objects.requireNonNull(professor,"Escolha o professor do treino");
        Validate.isTrue(Objects.requireNonNull(horario).aceitaProposta(agora),"O treino deve começar daqui a mais de uma hora, pois a confirmação encerra uma hora antes");
        var inicio=horario.inicio();
        var prazo=inicio.minusSeconds(ParametrosAcademia.ANTECEDENCIA_CONFIRMACAO_SEGUNDOS);
        return new TreinoGrupo(Optional.empty(),inicio,prazo,agora,Optional.empty(),membros.stream().map(a->a.equals(organizador)?new ConviteTreino(Optional.empty(),a,SituacaoConviteTreino.CONFIRMADO,agora,Optional.of(agora),Optional.empty(),Optional.empty()):new ConviteTreino(Optional.empty(),a,SituacaoConviteTreino.PENDENTE,agora,Optional.empty(),Optional.empty(),Optional.empty())).toList(),Optional.of(professor));
    }
    public boolean cancelado(){return cancelamento.isPresent();}
    public void exigirAgendado(){if(cancelado())throw new IllegalStateException("Treino cancelado");}
    public TreinoGrupo atualizar(List<ConviteTreino> novos){return new TreinoGrupo(identificador,inicio,prazo,criacao,cancelamento,novos,professor);}
    public TreinoGrupo responder(long convite,AlunoIdentificador autor,boolean confirmar,Instant agora){exigirAgendado();var alvo=convite(convite);return atualizar(convites.stream().map(c->c==alvo?c.responder(autor,confirmar,prazo,agora):c).toList());}
    public TreinoGrupo cancelarPresenca(long convite,AlunoIdentificador autor,Instant agora){exigirAgendado();var alvo=convite(convite);return atualizar(convites.stream().map(c->c==alvo?c.cancelarPresenca(autor,inicio,agora):c).toList());}
    public ConviteTreino convite(long id){return convites.stream().filter(c->c.identificador().filter(v->v==id).isPresent()).findFirst().orElseThrow(()->new NoSuchElementException("Convite de treino inexistente"));}
    public TreinoGrupo cancelar(Instant agora){exigirAgendado();if(!agora.isBefore(inicio))throw new IllegalStateException("Só é possível cancelar um treino antes do início");return new TreinoGrupo(identificador,inicio,prazo,criacao,Optional.of(agora),convites.stream().map(c->c.expirar(prazo,agora).cancelar(agora,"TREINO_CANCELADO")).toList(),professor);}
    public TreinoGrupo expirar(Instant agora){return atualizar(convites.stream().map(c->c.expirar(prazo,agora)).toList());}
    public TreinoGrupo retirar(AlunoIdentificador aluno,Instant agora){return atualizar(convites.stream().map(c->c.aluno().equals(aluno) && c.situacaoEm(prazo,agora)==SituacaoConviteTreino.PENDENTE?c.cancelar(agora,"SAIDA_GRUPO"):c.expirar(prazo,agora)).toList());}
}

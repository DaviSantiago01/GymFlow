package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.treino;

import java.time.Instant;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.treino.TreinoProjecao;
public interface ITreinoRepositorioJpa extends JpaRepository<TreinoJpa,Long>{
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select t from TreinoJpa t where t.identificador=:id") Optional<TreinoJpa> bloquear(Long id);
    @Query(value="select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.treino.TreinoProjecao(t.identificador,t.professor.identificador,t.professor.nome,t.descricao,t.inicio,t.termino,t.demonstracao,a.identificador,(v.identificador is not null)) from TreinoJpa t left join AvaliacaoProfessorJpa a on a.treino=t left join TreinoAvaliadoJpa v on v.identificador=t.identificador where t.aluno.usuario=:usuario and t.concluido=true and t.termino<=:agora order by t.inicio desc,t.identificador desc",countQuery="select count(t) from TreinoJpa t where t.aluno.usuario=:usuario and t.concluido=true and t.termino<=:agora") Page<TreinoProjecao> historico(String usuario,Instant agora,Pageable pagina);
    @Query("select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.treino.TreinoProjecao(t.identificador,t.professor.identificador,t.professor.nome,t.descricao,t.inicio,t.termino,t.demonstracao,a.identificador,(v.identificador is not null)) from TreinoJpa t left join AvaliacaoProfessorJpa a on a.treino=t left join TreinoAvaliadoJpa v on v.identificador=t.identificador where t.identificador=:id and t.aluno.usuario=:usuario and t.concluido=true and t.termino<=:agora") Optional<TreinoProjecao> detalhe(Long id,String usuario,Instant agora);
}

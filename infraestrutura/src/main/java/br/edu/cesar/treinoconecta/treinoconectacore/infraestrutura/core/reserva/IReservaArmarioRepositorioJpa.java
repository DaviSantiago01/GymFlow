package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.reserva;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;

public interface IReservaArmarioRepositorioJpa extends JpaRepository<ReservaArmarioJpa,Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ReservaArmarioJpa r where r.identificador = :id")
    Optional<ReservaArmarioJpa> bloquear(Long id);
    @Query("select count(r) > 0 from ReservaArmarioJpa r where r.aluno.identificador = :aluno and r.situacao = 'ATIVA' and r.termino > :agora")
    boolean existePorAluno(Long aluno, Instant agora);
    @Query("select count(r) > 0 from ReservaArmarioJpa r where r.armario.identificador = :armario and r.situacao = 'ATIVA' and r.inicio < :termino and r.termino > :inicio")
    boolean existeSobreposicao(String armario, Instant inicio, Instant termino);
    @Query("select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.armario.PeriodoOcupado(r.inicio,r.termino) from ReservaArmarioJpa r where r.armario.identificador = :armario and r.situacao = 'ATIVA' and r.inicio < :termino and r.termino > :inicio")
    List<br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.armario.PeriodoOcupado> periodosOcupados(String armario, Instant inicio, Instant termino);
    @Modifying(flushAutomatically=true, clearAutomatically=true)
    @Query("update ReservaArmarioJpa r set r.situacao = 'VENCIDA', r.encerramento = r.termino where r.situacao = 'ATIVA' and r.termino <= :agora and (r.aluno.identificador = :aluno or r.armario.identificador = :armario)")
    int vencerRelacionadas(Long aluno, String armario, Instant agora);
    @Modifying(flushAutomatically=true, clearAutomatically=true)
    @Query("update ReservaArmarioJpa r set r.situacao = 'VENCIDA', r.encerramento = r.termino where r.situacao = 'ATIVA' and r.termino <= :agora")
    int vencerTodas(Instant agora);
    @Query("select r from ReservaArmarioJpa r join fetch r.aluno join fetch r.armario where r.aluno.usuario = :usuario and r.situacao = 'ATIVA' and r.termino > :agora")
    Optional<ReservaArmarioJpa> ativa(String usuario, Instant agora);
    @Query("select r from ReservaArmarioJpa r join fetch r.aluno join fetch r.armario where r.aluno.usuario = :usuario and (r.situacao <> 'ATIVA' or r.termino <= :agora) order by r.confirmacao desc, r.identificador desc")
    List<ReservaArmarioJpa> historico(String usuario, Instant agora, Pageable pagina);
    @Query("select count(r) from ReservaArmarioJpa r where r.aluno.usuario = :usuario and (r.situacao <> 'ATIVA' or r.termino <= :agora)")
    long contarHistorico(String usuario, Instant agora);
    @Query("select r from ReservaArmarioJpa r join fetch r.aluno join fetch r.armario where r.identificador = :id and r.aluno.usuario = :usuario")
    Optional<ReservaArmarioJpa> detalhe(Long id, String usuario);
}

package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.avaliacaoprofessor;

import java.util.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaoprofessor.AvaliacaoProfessorProjecao;
public interface IAvaliacaoProfessorRepositorioJpa extends JpaRepository<AvaliacaoProfessorJpa,Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select a from AvaliacaoProfessorJpa a left join a.treino t where a.identificador=:id")
    Optional<AvaliacaoProfessorJpa> bloquear(Long id);
    @Query(value="select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaoprofessor.AvaliacaoProfessorProjecao(a.identificador,a.professor.identificador,a.professor.nome,a.aluno.nome,a.auxilio,a.pontualidade,a.postura,a.didatica,a.adequacao,a.comentario,a.criacao,a.atualizacao,(a.aluno.usuario=:usuario),t.identificador,t.inicio,t.demonstracao) from AvaliacaoProfessorJpa a left join a.treino t where a.aluno.usuario=:usuario order by a.criacao desc,a.identificador desc",countQuery="select count(a) from AvaliacaoProfessorJpa a left join a.treino t where a.aluno.usuario=:usuario")
    Page<AvaliacaoProfessorProjecao> minhas(String usuario,Pageable pagina);
    @Query("select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaoprofessor.AvaliacaoProfessorProjecao(a.identificador,a.professor.identificador,a.professor.nome,a.aluno.nome,a.auxilio,a.pontualidade,a.postura,a.didatica,a.adequacao,a.comentario,a.criacao,a.atualizacao,(a.aluno.usuario=:usuario),t.identificador,t.inicio,t.demonstracao) from AvaliacaoProfessorJpa a left join a.treino t where a.identificador=:id and a.aluno.usuario=:usuario") Optional<AvaliacaoProfessorProjecao> detalhe(Long id,String usuario);
    @Query("select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaoprofessor.AvaliacaoProfessorProjecao(a.identificador,a.professor.identificador,a.professor.nome,a.aluno.nome,a.auxilio,a.pontualidade,a.postura,a.didatica,a.adequacao,a.comentario,a.criacao,a.atualizacao,(a.aluno.usuario=:usuario),t.identificador,t.inicio,t.demonstracao) from AvaliacaoProfessorJpa a left join a.treino t where a.treino.identificador=:treino and a.aluno.usuario=:usuario") Optional<AvaliacaoProfessorProjecao> minha(Long treino,String usuario);
    @Query("select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaoprofessor.AvaliacaoProfessorProjecao(a.identificador,a.professor.identificador,a.professor.nome,a.aluno.nome,a.auxilio,a.pontualidade,a.postura,a.didatica,a.adequacao,a.comentario,a.criacao,a.atualizacao,(a.aluno.usuario=:usuario),t.identificador,t.inicio,t.demonstracao) from AvaliacaoProfessorJpa a left join a.treino t where a.professor.identificador=:professor order by a.criacao desc,a.identificador desc") List<AvaliacaoProfessorProjecao> recentes(Long professor,String usuario,Pageable pagina);
}

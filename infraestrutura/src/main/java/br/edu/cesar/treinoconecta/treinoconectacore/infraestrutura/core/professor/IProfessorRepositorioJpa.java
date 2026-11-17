package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.professor;

import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.professor.ProfessorProjecao;
public interface IProfessorRepositorioJpa extends JpaRepository<ProfessorJpa,Long> {
    @Query(value="select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.professor.ProfessorProjecao(p.identificador,p.nome,p.especialidade,p.biografia,p.demonstracao,count(a.identificador),coalesce(sum(a.auxilio),0L),coalesce(sum(a.pontualidade),0L),coalesce(sum(a.postura),0L),coalesce(sum(a.didatica),0L),coalesce(sum(a.adequacao),0L)) from ProfessorJpa p left join AvaliacaoProfessorJpa a on a.professor=p where (:busca is null or lower(p.nome) like :busca escape '\\' or lower(p.especialidade) like :busca escape '\\') group by p.identificador,p.nome,p.especialidade,p.biografia,p.demonstracao",countQuery="select count(p) from ProfessorJpa p where (:busca is null or lower(p.nome) like :busca escape '\\' or lower(p.especialidade) like :busca escape '\\')")
    Page<ProfessorProjecao> pesquisar(@Nullable String busca,Pageable pagina);
    @Query("select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.professor.ProfessorProjecao(p.identificador,p.nome,p.especialidade,p.biografia,p.demonstracao,count(a.identificador),coalesce(sum(a.auxilio),0L),coalesce(sum(a.pontualidade),0L),coalesce(sum(a.postura),0L),coalesce(sum(a.didatica),0L),coalesce(sum(a.adequacao),0L)) from ProfessorJpa p left join AvaliacaoProfessorJpa a on a.professor=p where p.identificador=:id group by p.identificador,p.nome,p.especialidade,p.biografia,p.demonstracao")
    Optional<ProfessorProjecao> detalhe(Long id);
}

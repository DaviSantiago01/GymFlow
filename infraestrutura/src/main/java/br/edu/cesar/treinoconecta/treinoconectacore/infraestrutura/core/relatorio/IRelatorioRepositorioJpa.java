package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.relatorio;

import java.time.Instant;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.relatorio.RelatorioProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.TipoRelatorio;
import jakarta.persistence.LockModeType;

public interface IRelatorioRepositorioJpa extends JpaRepository<RelatorioJpa, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RelatorioJpa r where r.identificador = :identificador")
    Optional<RelatorioJpa> bloquear(Long identificador);

    @Query("""
            select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.relatorio.RelatorioProjecao(
                   r.identificador, r.tipo, r.inicio, r.fim, r.agrupamento, r.filtroPlano, r.filtroModalidade,
                   r.filtroPersonal, r.filtroAula, r.geracao, r.usuario, r.usuarioNome, r.perfil, r.versao, r.situacao)
              from RelatorioJpa r
             where (:tipo is null or r.tipo = :tipo)
               and (:usuario is null or r.usuario = :usuario)
               and (:restrito is null or r.usuario = :restrito)
               and (:desde is null or r.geracao >= :desde)
               and (:termo is null or lower(r.usuarioNome) like :termo or lower(r.usuario) like :termo)
            """)
    Page<RelatorioProjecao> pesquisar(@Nullable TipoRelatorio tipo, @Nullable String usuario, @Nullable String restrito,
            @Nullable Instant desde, @Nullable String termo, Pageable pageable);
}

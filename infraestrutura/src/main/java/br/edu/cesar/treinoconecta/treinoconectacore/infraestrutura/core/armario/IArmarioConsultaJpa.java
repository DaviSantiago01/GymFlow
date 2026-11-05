package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.armario;

import java.time.Instant;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.armario.ArmarioProjecao;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
public interface IArmarioConsultaJpa extends JpaRepository<ArmarioJpa,String> {
    @Query("select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.armario.ArmarioProjecao(a.identificador,a.localizacao,a.descricao, not exists(select r.identificador from ReservaArmarioJpa r where r.armario = a and r.situacao = 'ATIVA' and r.inicio <= :agora and r.termino > :agora)) from ArmarioJpa a")
    Page<ArmarioProjecao> pesquisar(Instant agora, Pageable pagina);
    @Query("select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.armario.ArmarioProjecao(a.identificador,a.localizacao,a.descricao, not exists(select r.identificador from ReservaArmarioJpa r where r.armario = a and r.situacao = 'ATIVA' and r.inicio <= :agora and r.termino > :agora)) from ArmarioJpa a where a.identificador = :codigo")
    Optional<ArmarioProjecao> detalhe(String codigo, Instant agora);
}

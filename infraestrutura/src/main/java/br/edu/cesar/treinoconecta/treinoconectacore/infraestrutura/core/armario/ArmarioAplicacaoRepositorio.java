package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.armario;

import java.time.*;
import java.util.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.armario.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.armario.spi.IArmarioAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.armario.ArmarioCodigo;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoAplicacaoRepositorioAbstrato;
import org.springframework.data.domain.*;
public final class ArmarioAplicacaoRepositorio extends AgregadoAplicacaoRepositorioAbstrato<ArmarioProjecao,ArmarioOrdenacao,ArmarioConsulta> implements IArmarioAplicacaoRepositorio {
    private final IArmarioConsultaJpa jpa;
    private final Clock relogio;
    private final br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.reserva.IReservaArmarioRepositorioJpa reservas;
    public ArmarioAplicacaoRepositorio(IArmarioConsultaJpa jpa, Clock relogio, br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.reserva.IReservaArmarioRepositorioJpa reservas) { this.reservas=Objects.requireNonNull(reservas); this.jpa=Objects.requireNonNull(jpa); this.relogio=Objects.requireNonNull(relogio); }
    protected Page<ArmarioProjecao> pesquisar(ArmarioConsulta consulta, Pageable pagina) {
        return jpa.pesquisar(relogio.instant(),PageRequest.of(pagina.getPageNumber(),pagina.getPageSize(),pagina.getSort().isSorted()?pagina.getSort():Sort.by("identificador")));
    }
    protected String paraPropriedadeOrdenacao(ArmarioOrdenacao ordenacao) { return "identificador"; }
    public ArmarioProjecao detalhe(ArmarioCodigo codigo, Instant agora) { return jpa.detalhe(codigo.toString(),agora).orElseThrow(() -> new NoSuchElementException("Armário inexistente")); }
    public List<PeriodoOcupado> periodosOcupados(ArmarioCodigo codigo, Instant inicio, Instant termino) { return reservas.periodosOcupados(codigo.toString(), inicio, termino); }
}

package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.reserva;

import java.time.*;
import java.util.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.reserva.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.reserva.spi.IReservaArmarioAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.reserva.*;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoAplicacaoRepositorioAbstrato;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
public final class ReservaArmarioAplicacaoRepositorio extends AgregadoAplicacaoRepositorioAbstrato<ReservaArmarioProjecao,ReservaArmarioOrdenacao,ReservaArmarioConsulta> implements IReservaArmarioAplicacaoRepositorio {
    private final IReservaArmarioRepositorioJpa jpa;
    private final Clock relogio;
    private final ISegurancaServico seguranca;
    public ReservaArmarioAplicacaoRepositorio(IReservaArmarioRepositorioJpa jpa, Clock relogio, ISegurancaServico seguranca) {
        this.jpa=Objects.requireNonNull(jpa); this.relogio=Objects.requireNonNull(relogio); this.seguranca=Objects.requireNonNull(seguranca);
    }
    protected Page<ReservaArmarioProjecao> pesquisar(ReservaArmarioConsulta consulta, Pageable pagina) {
        var agora=relogio.instant();
        var usuario=seguranca.getUsuario();
        var lista=jpa.historico(usuario,agora,pagina);
        return new PageImpl<>(lista.stream().map(r -> projetar(r,agora)).toList(),pagina,jpa.contarHistorico(usuario,agora));
    }
    protected String paraPropriedadeOrdenacao(ReservaArmarioOrdenacao ordenacao) { return "identificador"; }
    public Optional<ReservaArmarioProjecao> ativa(String usuario, Instant agora) { return jpa.ativa(usuario,agora).map(r -> projetar(r,agora)); }
    public ReservaArmarioProjecao detalhe(ReservaArmarioIdentificador id, String usuario, Instant agora) {
        return projetar(jpa.detalhe(Long.valueOf(id.toString()),usuario).orElseThrow(() -> new NoSuchElementException("Reserva não encontrada para este aluno")),agora);
    }
    private ReservaArmarioProjecao projetar(ReservaArmarioJpa r, Instant agora) {
        boolean vencida=r.getSituacao()==SituacaoReserva.ATIVA && !agora.isBefore(r.getTermino());
        return new ReservaArmarioProjecao(r.getIdentificador().toString(),r.getAluno().getNome(),r.getArmario().getIdentificador(),
                r.getArmario().getLocalizacao(),r.getConfirmacao(),r.getInicio(),r.getTermino(),
                Optional.ofNullable(vencida?r.getTermino():r.getEncerramento()),vencida?SituacaoReserva.VENCIDA:r.getSituacao());
    }
}

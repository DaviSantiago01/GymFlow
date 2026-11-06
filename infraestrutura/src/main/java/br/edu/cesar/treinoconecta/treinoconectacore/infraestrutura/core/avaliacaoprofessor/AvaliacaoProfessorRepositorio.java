package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.avaliacaoprofessor;

import java.util.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.treino.TreinoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.treino.ITreinoRepositorioJpa;
import org.springframework.jdbc.core.JdbcTemplate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaoprofessor.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaoprofessor.spi.IAvaliacaoProfessorRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.professor.ProfessorIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.IAlunoRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.professor.IProfessorRepositorioJpa;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoRepositorioAbstrato;
public final class AvaliacaoProfessorRepositorio extends AgregadoRepositorioAbstrato<AvaliacaoProfessorIdentificador,AvaliacaoProfessor,Long,AvaliacaoProfessorJpa> implements IAvaliacaoProfessorRepositorio {
    private final IAvaliacaoProfessorRepositorioJpa jpa;
    private final IAlunoRepositorioJpa alunos;
    private final IProfessorRepositorioJpa professores;
    private final JdbcTemplate banco;private final ITreinoRepositorioJpa treinos;
    public AvaliacaoProfessorRepositorio(IAvaliacaoProfessorRepositorioJpa jpa,IAlunoRepositorioJpa alunos,IProfessorRepositorioJpa professores,JdbcTemplate banco,ITreinoRepositorioJpa treinos) {
        super(jpa,"Avaliação %s inexistente");this.jpa=Objects.requireNonNull(jpa);this.alunos=Objects.requireNonNull(alunos);this.professores=Objects.requireNonNull(professores);this.banco=Objects.requireNonNull(banco);this.treinos=Objects.requireNonNull(treinos);
    }
    protected Long paraIdentificadorJpa(AvaliacaoProfessorIdentificador id) { return Long.valueOf(id.toString()); }
    protected AvaliacaoProfessorJpa paraJpa(AvaliacaoProfessor a) {
        var n=a.notas();return new AvaliacaoProfessorJpa(a.identificador().map(this::paraIdentificadorJpa).orElse(null),alunos.getReferenceById(Long.valueOf(a.aluno().toString())),professores.getReferenceById(Long.valueOf(a.professor().toString())),a.treino().map(t->treinos.getReferenceById(Long.valueOf(t.toString()))).orElse(null),n.auxilio(),n.pontualidade(),n.postura(),n.didatica(),n.adequacao(),a.comentario().orElse(null),a.criacao(),a.atualizacao());
    }
    protected AvaliacaoProfessor paraAgregado(AvaliacaoProfessorJpa a) {
        return new AvaliacaoProfessor(Optional.of(new AvaliacaoProfessorIdentificador(a.getIdentificador().toString())),new AlunoIdentificador(a.getAluno().getIdentificador().toString()),new ProfessorIdentificador(a.getProfessor().getIdentificador().toString()),Optional.ofNullable(a.getTreino()).map(t->new TreinoIdentificador(t.getIdentificador().toString())),new NotasAvaliacao(a.getAuxilio(),a.getPontualidade(),a.getPostura(),a.getDidatica(),a.getAdequacao()),Optional.ofNullable(a.getComentario()),a.getCriacao(),a.getAtualizacao());
    }
    public boolean jaAvaliou(TreinoIdentificador treino) { return Objects.requireNonNull(banco.queryForObject("select count(*) from VAT_TREINO_AVALIADO where TRN_ID=?",Long.class,Long.valueOf(treino.toString())))>0; }
    public void registrarParticipacao(TreinoIdentificador treino) { banco.update("insert into VAT_TREINO_AVALIADO(TRN_ID) values (?)",Long.valueOf(treino.toString())); }
    public AvaliacaoProfessor bloquear(AvaliacaoProfessorIdentificador id) { return paraAgregado(jpa.bloquear(paraIdentificadorJpa(id)).orElseThrow(()->new NoSuchElementException("Avaliação inexistente"))); }
}

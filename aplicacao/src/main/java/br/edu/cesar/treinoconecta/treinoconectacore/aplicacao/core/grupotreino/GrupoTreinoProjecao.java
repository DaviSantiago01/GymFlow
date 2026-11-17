package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.grupotreino;

import java.time.Instant;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.grupotreino.GrupoTreino;
import dev.sauloaraujo.d5.framework.aplicacao.IProjecao;
public record GrupoTreinoProjecao(String identificador,String nome,String organizador,String organizadorNome,String academia,boolean souOrganizador,int membros,Optional<Instant> proximo,Optional<Instant> encerramento) implements IProjecao<GrupoTreino> {}

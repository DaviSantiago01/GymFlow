package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.AlunoGestaoProjecao;

public interface IAlunoGestaoJpa extends JpaRepository<AlunoJpa, Long> {
    String PROJECAO = """
            select new br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.AlunoGestaoProjecao(
                   a.identificador, a.usuario, a.nome, a.cpf, a.email, a.telefone, a.ativo, a.bloqueioMotivo,
                   (select p.nome from AdesaoPlanoJpa d join d.plano p where d.aluno = a and d.situacao = 'ATIVA'),
                   case when exists (select m.identificador from MensalidadeJpa m
                                      where m.aluno = a and m.situacao = 'ABERTA' and m.vencimento < :hoje)
                        then true else false end)
              from AlunoJpa a
            """;

    @Query(PROJECAO + """
             where (:busca is null or lower(a.nome) like :busca escape '\\' or lower(a.usuario) like :busca escape '\\'
                    or a.cpf like :busca escape '\\' or a.email like :busca escape '\\')
             order by lower(a.nome), a.identificador
            """)
    List<AlunoGestaoProjecao> pesquisar(@Nullable String busca, LocalDate hoje);

    @Query(PROJECAO + " where a.identificador = :aluno")
    Optional<AlunoGestaoProjecao> projetar(Long aluno, LocalDate hoje);
}

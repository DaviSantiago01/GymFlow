package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.treino;

import java.time.*;
import java.util.*;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.treino.spi.ITreinoDemonstracaoRepositorio;
@Configuration @Profile("desenvolvimento & !producao")
public class TreinosDemonstracaoConfiguracao {
    @Bean ITreinoDemonstracaoRepositorio treinosDemonstracao(JdbcTemplate banco){
        return (aluno,agora)->{
            long id=Long.parseLong(aluno.toString());
            if(Objects.requireNonNull(banco.queryForObject("select count(*) from TRN_TREINO where ALU_ID=? and TRN_DEMONSTRACAO=true",Long.class,id))>0) return;
            var professores=banco.queryForList("select PRO_ID from PRO_PROFESSOR where PRO_DEMONSTRACAO=true order by PRO_ID",Long.class);
            if(professores.isEmpty()) throw new IllegalStateException("Nenhum professor de demonstração cadastrado");
            for(int i=0;i<3;i++){
                var inicio=agora.atZone(ZoneId.of("America/Sao_Paulo")).minusDays(2L*(i+1)).withHour(8+i).withMinute(0).withSecond(0).withNano(0).toInstant();
                banco.update("insert into TRN_TREINO(ALU_ID,PRO_ID,TRN_DESCRICAO,TRN_INICIO,TRN_TERMINO,TRN_CONCLUIDO,TRN_DEMONSTRACAO) values (?,?,?,?,?,true,true)",id,professores.get(i==2 && professores.size()>1?1:0),"Treino de demonstração "+(i+1),inicio,inicio.plusSeconds(3600));
            }
        };
    }
}

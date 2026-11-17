package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.seguranca;

import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.core.userdetails.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import dev.sauloaraujo.d5.framework.ui.seguranca.SegurancaConfiguracaoAbstrata;
@Configuration
public class SegurancaConfiguracao extends SegurancaConfiguracaoAbstrata {
    @Override protected void autorizar(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry autorizacao) {
        autorizacao.requestMatchers(org.springframework.http.HttpMethod.GET, "/backend/core/aluno/cadastro").permitAll();
        autorizacao.requestMatchers(org.springframework.http.HttpMethod.POST, "/backend/core/aluno/cadastrar").permitAll();
        autorizacao.requestMatchers(BACKEND).authenticated();
    }
    @Bean UserDetailsService usuarios(JdbcTemplate banco) {
        return usuario -> buscarAdministrador(banco,usuario).or(() -> buscarPersonal(banco,usuario)).orElseGet(() -> banco.query("select ALU_USUARIO,USU_SENHA,USU_ATIVO from USU_CREDENCIAL where ALU_USUARIO = ?",
            (r,n) -> User.withUsername(r.getString(1)).password(r.getString(2)).disabled(!r.getBoolean(3)).authorities("USUARIO").build(),usuario.strip().toLowerCase(java.util.Locale.ROOT))
            .stream().findFirst().orElseThrow(() -> new UsernameNotFoundException("Usuário ou senha inválidos")));
    }
    private java.util.Optional<UserDetails> buscarPersonal(JdbcTemplate banco, String usuario) {
        return banco.query("select p.PER_USUARIO,c.PCD_SENHA,c.PCD_ATIVO from PER_PERSONAL p join PCD_CREDENCIAL_PERSONAL c on c.PER_ID = p.PER_ID where p.PER_USUARIO = ?",
            (r,n) -> User.withUsername(r.getString(1)).password(r.getString(2)).disabled(!r.getBoolean(3)).authorities(Autorizacao.PERSONAL.name()).build(),usuario.strip().toLowerCase(java.util.Locale.ROOT))
            .stream().findFirst();
    }
    /** Administradores da academia gerenciam planos, mensalidades e profissionais. */
    private java.util.Optional<UserDetails> buscarAdministrador(JdbcTemplate banco, String usuario) {
        return banco.query("select ADM_USUARIO,ADM_SENHA,ADM_ATIVO from ADM_ADMINISTRADOR where ADM_USUARIO = ?",
            (r,n) -> User.withUsername(r.getString(1)).password(r.getString(2)).disabled(!r.getBoolean(3)).authorities(Autorizacao.ADMINISTRADOR.name()).build(),usuario.strip().toLowerCase(java.util.Locale.ROOT))
            .stream().findFirst();
    }
}

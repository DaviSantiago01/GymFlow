package com.example.exemplo.infraestrutura.geografia.unidadefederativa;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.exemplo.aplicacao.geografia.unidadefederativa.UnidadeFederativaProjecao;

public interface IUnidadeFederativaRepositorioJpa extends JpaRepository<UnidadeFederativaJpa, String> {
	@Query("""
			select count(u) > 0
			  from UnidadeFederativaJpa u
			 where u.sigla = :sigla
			   and (:identificadorIgnorado is null or u.identificador <> :identificadorIgnorado)
			""")
	boolean existePorSigla(String sigla, @Nullable String identificadorIgnorado);

	@Query("""
			select count(u) > 0
			  from UnidadeFederativaJpa u
			 where lower(u.nome) = lower(:nome)
			   and (:identificadorIgnorado is null or u.identificador <> :identificadorIgnorado)
			""")
	boolean existePorNome(String nome, @Nullable String identificadorIgnorado);

	@Query("""
			select count(m) > 0
			  from MunicipioJpa m
			 where m.unidadeFederativa.identificador = :identificador
			""")
	boolean existeReferencia(String identificador);

	@Query("""
			select new com.example.exemplo.aplicacao.geografia.unidadefederativa.UnidadeFederativaProjecao(u.identificador, u.sigla, u.nome)
			  from UnidadeFederativaJpa u
			 where (:sigla is null or lower(u.sigla) like :sigla escape '\\')
			   and (:nome is null or lower(u.nome) like :nome escape '\\')
			""")
	Page<UnidadeFederativaProjecao> pesquisar(@Nullable String sigla, @Nullable String nome, Pageable pageable);
}
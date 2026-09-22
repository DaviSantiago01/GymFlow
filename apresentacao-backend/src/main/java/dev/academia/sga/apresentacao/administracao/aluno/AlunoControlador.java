package dev.academia.sga.apresentacao.administracao.aluno;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.academia.sga.aplicacao.administracao.aluno.AlunoServicoAplicacao;

@RestController
@RequestMapping("backend/aluno")
class AlunoControlador {
	private @Autowired AlunoServicoAplicacao alunoServicoConsulta;

	@RequestMapping(method = GET, path = "pesquisa")
	List<? extends Object> pesquisar(@RequestParam(required = false, defaultValue = "false") boolean expandir) {
		if (expandir) {
			return alunoServicoConsulta.pesquisarResumosExpandidos();
		} else {
			return alunoServicoConsulta.pesquisarResumos();
		}
	}
}

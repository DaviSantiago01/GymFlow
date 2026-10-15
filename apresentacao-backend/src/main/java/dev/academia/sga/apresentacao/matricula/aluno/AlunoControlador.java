package dev.academia.sga.apresentacao.matricula.aluno;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.academia.sga.aplicacao.matricula.aluno.AlunoResumo;
import dev.academia.sga.aplicacao.matricula.aluno.AlunoServicoAplicacao;

@RestController
@RequestMapping("backend/aluno")
class AlunoControlador {
	private @Autowired AlunoServicoAplicacao alunoServicoConsulta;

	@RequestMapping(method = GET, path = "pesquisa")
	List<AlunoResumo> pesquisar() {
		return alunoServicoConsulta.pesquisarResumos();
	}
}

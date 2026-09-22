package dev.academia.sga.apresentacao.treinamento.exercicio;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.academia.sga.aplicacao.treinamento.exercicio.ExercicioResumo;
import dev.academia.sga.aplicacao.treinamento.exercicio.ExercicioServicoAplicacao;

@RestController
@RequestMapping("backend/exercicio")
class ExercicioControlador {
	private @Autowired ExercicioServicoAplicacao exercicioServicoConsulta;

	@RequestMapping(method = GET, path = "pesquisa")
	List<ExercicioResumo> pesquisar() {
		return exercicioServicoConsulta.pesquisarResumos();
	}

	@RequestMapping(method = GET, path = "treino/{treino}/pesquisa")
	List<ExercicioResumo> pesquisarPorTreino(@PathVariable int treino) {
		return exercicioServicoConsulta.pesquisarResumosPorTreino(treino);
	}
}

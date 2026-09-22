package dev.academia.sga.apresentacao.treinamento.treino;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.academia.sga.aplicacao.treinamento.treino.TreinoServicoAplicacao;

@RestController
@RequestMapping("backend/treino")
class TreinoControlador {
	private @Autowired TreinoServicoAplicacao treinoServicoConsulta;

	@RequestMapping(method = GET, path = "pesquisa")
	List<? extends Object> pesquisar(@RequestParam(required = false, defaultValue = "false") boolean expandir) {
		if (expandir) {
			return treinoServicoConsulta.pesquisarResumosExpandidos();
		} else {
			return treinoServicoConsulta.pesquisarResumos();
		}
	}
}

package dev.academia.sga.apresentacao.administracao.instrutor;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.academia.sga.aplicacao.administracao.instrutor.InstrutorResumo;
import dev.academia.sga.aplicacao.administracao.instrutor.InstrutorServicoAplicacao;

@RestController
@RequestMapping("backend/instrutor")
class InstrutorControlador {
	private @Autowired InstrutorServicoAplicacao instrutorServicoConsulta;

	@RequestMapping(method = GET, path = "pesquisa")
	List<InstrutorResumo> pesquisar() {
		return instrutorServicoConsulta.pesquisarResumos();
	}
}

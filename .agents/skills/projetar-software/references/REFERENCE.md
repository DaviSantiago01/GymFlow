# docs/compartilhado/projeto.cml
```
Domain dominio {
    Subdomain subdominioA {
        type = CORE_DOMAIN
    }

    Subdomain subdominioB {
        type = SUPPORTING_DOMAIN
    }

    Subdomain subdominioC {
        type = GENERIC_SUBDOMAIN
    }
}

BoundedContext compartilhado {
    Module compartilhado {
    	Aggregate compartilhado {
    		enum Autorizacao {
    			ADMINISTRADOR, USUARIO
    		}
    	}
    }
}
```

# docs/subdominioA/projeto.cml
```
import "../compartilhado/projeto.cml"

ContextMap {
	contains subdominioA, compartilhado

	subdominioA Downstream-Upstream compartilhado
}

BoundedContext subdominioA implements subdominioA {
    Module subdominioA {
    	Aggregate EntidadeA {
    		Entity EntidadeA {
    			aggregateRoot
    			
    			-EntidadeAIdentificador identificador key
    			int atributoInt
    			long atributoLong
    			double atributoDouble
    			Date atributoDate
    			DateTime atributoDateTime
    			
	    		Repository EntidadeARepository {
	    			@EntidadeA incluir(@EntidadeA agregado);
	    			boolean existePorIdentificador(@EntidadeAIdentificador identificador);
	    			@EntidadeA buscarPorIdentificador(@EntidadeAIdentificador identificador);
	    			void alterar(@EntidadeA agregado);
	    			void excluir(@EntidadeAIdentificador identificador);
				}
    		}

    		// artificial
    		ValueObject EntidadeAIdentificador {
    			String valor
    		}

    		Service EntidadeAServico {
    			@EntidadeA incluir(@EntidadeA agregado);
    			@EntidadeA buscar(@EntidadeAIdentificador identificador);
    			void alterar(@EntidadeA agregado);
    			void excluir(@EntidadeAIdentificador identificador);
    		}
    	}
    }
}
```

# docs/subdominioB/projeto.cml
```
import "../compartilhado/projeto.cml"
import "../subdominioA/projeto.cml"

ContextMap {
	contains subdominioB, subdominioA, compartilhado

	subdominioB Downstream-Upstream compartilhado
	subdominioB Downstream-Upstream subdominioA
}

BoundedContext subdominioB implements subdominioB {
    Module subdominioB {
    	Aggregate EntidadeB {
    		Entity EntidadeB {
    			aggregateRoot
    			
    			-EntidadeBCodigo codigo key
    			String nome

				// Associações
    			-List<EntidadeAIdentificador> entidadesA
    			
	    		Repository EntidadeBRepository {
	    			@EntidadeB incluir(@EntidadeB agregado);
	    			boolean existePorIdentificador(@EntidadeBCodigo identificador);
	    			@EntidadeB buscarPorIdentificador(@EntidadeBCodigo identificador);
	    			void alterar(@EntidadeB agregado);
	    			void excluir(@EntidadeBCodigo identificador);
				}
    		}
    		
    		// natural
    		ValueObject EntidadeBCodigo {
    			String valor
    		}

    		Service EntidadeBServico {
    			@EntidadeB incluir(@EntidadeB agregado);
    			@EntidadeB buscar(@EntidadeBCodigo identificador);
    			void alterar(@EntidadeB agregado);
    			void excluir(@EntidadeBCodigo identificador);
    		}
    	}
    }
}
```

# docs/subdominioC/projeto.cml
```
import "../compartilhado/projeto.cml"

ContextMap {
	contains subdominioC, compartilhado

	subdominioC Downstream-Upstream compartilhado
}

BoundedContext subdominioC implements subdominioC {
    Module subdominioC {
    }
}
```

# docs/projeto.cml
```
import "compartilhado/projeto.cml"
import "subdominioA/projeto.cml"
import "subdominioB/projeto.cml"
import "subdominioC/projeto.cml"
```
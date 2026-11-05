---
name: validar-modelo
description: Valida com o Context Mapper a sintaxe de cada arquivo .cml do modelo de projeto de software. Use ao final de projetar-software e sempre que um arquivo .cml for alterado.
---
O validador do Context Mapper não valida os arquivos importados pelo arquivo que recebe. Por isso, cada arquivo do modelo de projeto de software, docs/compartilhado/projeto.cml e docs/**S**/projeto.cml de cada subdomínio **S**, é validado separadamente, com o comando `mvn -q exec:java -f .agents/skills/validar-modelo/scripts/pom.xml -Dexec.args="validate --input docs/S/projeto.cml"`, executado no diretório raiz do projeto.

docs/projeto.cml, que apenas importa os demais arquivos, também é validado com o mesmo comando. Essa validação verifica apenas a sintaxe do próprio arquivo: um import de um arquivo inexistente não é detectado, e os arquivos importados não são validados.

O comando termina com sucesso mesmo quando o arquivo é inválido ou inexistente: o arquivo é válido apenas se a saída contiver `has been validated without errors`.

O validador verifica apenas a sintaxe. Referências a tipos inexistentes ou de subdomínios sem a relação `Downstream-Upstream` correspondente no `ContextMap` não são detectadas e devem ser conferidas.
---
name: programar-defensivamente
description: Regras de programação defensiva do código principal, como nulidade com JSpecify e Optional, atributos final e validação de argumentos e de estado. Use ao escrever ou revisar o código Java de produção do projeto.
---
Estas regras valem para o código principal, e não para o de teste, que precisa provocar situações inválidas e guardar estado mutável entre passos e testes.

Entidades JPA são exceção às regras de Optional, de atributos final e de validação de argumentos: o JPA exige atributos não final e um construtor sem argumentos, e um atributo opcional da entidade é null, anotado com org.jspecify.annotations.Nullable.

Pacotes são anotados com org.jspecify.annotations.NullMarked.

Atributos e parâmetros opcionais, por exemplo, aqueles declarados como nullable nos modelos de projeto de software, são Optional. A exceção são os contratos de bibliotecas e frameworks que não admitem Optional, como o parâmetro de equals e os parâmetros das consultas Spring Data JPA declaradas com @Query: nesses casos, o valor ausente é null, anotado com org.jspecify.annotations.Nullable.

Métodos que pesquisam um único objeto não retornam Optional, lançam NoSuchElementException.

Validações de que parâmetros não são nulos são feitas com Objects.requireNonNull.

Atributos imutáveis são final.

Construtores e métodos validam argumentos e, em caso negativo, lançam IllegalArgumentException. As validações são feitas com os métodos da classe org.apache.commons.lang3.Validate, por exemplo, Validate.notBlank e Validate.isTrue.

Métodos lançam NoSuchElementException quando um argumento identifica um objeto inexistente.

Métodos validam se o estado do sistema permite suas execuções e, em caso negativo, lançam IllegalStateException, por exemplo, com Validate.validState.

Os argumentos são validados antes do estado, para que a exceção lançada aponte o motivo real da rejeição. A exceção é a existência do objeto sobre o qual o método age: se ele não existe, essa ausência é o motivo real da rejeição, ainda que os argumentos também sejam inválidos.
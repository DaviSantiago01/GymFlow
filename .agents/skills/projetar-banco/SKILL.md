---
name: projetar-banco
description: Projeta em DBML, em docs/S/projeto.dbml, as tabelas e colunas que persistem os agregados de um subdomínio. Use depois de desenvolver os testes e antes de desenvolver a camada de infraestrutura.
---
O arquivo docs/**S**/projeto.dbml contém o modelo do projeto de banco de dados do subdomínio **S**. O projeto é feito a partir dos agregados, entidades, objetos de valor e associações contidos em docs/**S**/projeto.cml.

Os nomes das tabelas e colunas são maiúsculos.

Toda tabela possui um prefixo formado por 3 letras seguido de um sublinhado.

Os prefixos são únicos.

Todas as colunas de uma tabela **T** são prefixadas com prefixo de **T**, exceto chaves estrangeiras, que possuem o mesmo nome da chave primária que elas referenciam.

Identificadores sintéticos de entidades correspondem a colunas autoincrementadas.

Tabelas que armazenam listas devem possuir uma coluna para armazenar o índice dos elementos da lista.
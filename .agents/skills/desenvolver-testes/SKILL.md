---
name: desenvolver-testes
description: Automatiza com Cucumber e Mockito os cenários de teste de uma funcionalidade, nas step definitions de seu agregado. Use depois de desenvolver a camada de domínio.
---
Desenvolver os testes de uma funcionalidade **F** associada a um agregado **A** de um subdomínio **S** consiste em desenvolver, no pacote dominio.**S**.**A** do módulo dominio-**S**, a classe **A**StepDefinitions, cujos métodos, anotados com as anotações do pacote io.cucumber.java.pt, automatizam a execução dos cenários de teste contidos em features/**C**.feature, onde **C** é o nome de **A** em minúsculas e com as palavras separadas por hífen.

A suíte CucumberTests, já existente no pacote dominio.**S**, executa os cenários de todos os arquivos .feature do módulo e não é alterada.

Mock os repositórios e outras SPIs com Mockito.

As regras de [programação defensiva](../programar-defensivamente/SKILL.md) não se aplicam ao código de teste.
# language: pt
Funcionalidade: Metas e desafios fitness

  Cenário: Aceitar vigência mínima de sete dias
    Quando criar um desafio com início "2026-11-01" e término "2026-11-08"
    Então o desafio é válido

  Cenário: Rejeitar vigência menor que sete dias
    Quando criar um desafio com início "2026-11-01" e término "2026-11-07"
    Então o desafio é rejeitado

  Cenário: Permitir inscrição até cinco dias após o início
    Dado um desafio de "2026-11-01" a "2026-11-15"
    Quando verificar inscrição em "2026-11-06"
    Então a inscrição está aberta

  Cenário: Bloquear inscrição após o quinto dia
    Dado um desafio de "2026-11-01" a "2026-11-15"
    Quando verificar inscrição em "2026-11-07"
    Então a inscrição está encerrada

  Cenário: Trancar avaliação depois de quarenta e oito horas
    Dado avaliação registrada em "2026-11-01T10:00:00-03:00"
    Quando verificar às "2026-11-03T10:00:01-03:00"
    Então a avaliação está bloqueada

# language: pt
Funcionalidade: Reservar horários de aula

  Cenário: Detectar choque de horário do professor
    Dado uma aula das "09:00" às "10:00"
    Quando comparar com uma aula das "09:30" às "10:30"
    Então os horários se sobrepõem

  Cenário: Permitir aulas em sequência
    Dado uma aula das "09:00" às "10:00"
    Quando comparar com uma aula das "10:00" às "11:00"
    Então os horários não se sobrepõem

  Cenário: Rejeitar intervalo sem duração
    Quando criar uma aula das "10:00" às "10:00"
    Então o intervalo é inválido

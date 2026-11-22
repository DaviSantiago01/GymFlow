-- Movimento fictício da academia para os relatórios de desempenho e para as sugestões de treino do personal.
-- Os alunos "demo01" a "demo48" não têm senha: servem apenas para compor matrículas, mensalidades, entradas e planos.
SET @hoje = CURRENT_DATE;
SET @mensal = (SELECT MIN(PLA_ID) FROM PLA_PLANO WHERE PLA_NOME = 'Mensal' AND PLA_SITUACAO = 'ATIVO');
SET @trimestral = (SELECT MIN(PLA_ID) FROM PLA_PLANO WHERE PLA_NOME = 'Trimestral' AND PLA_SITUACAO = 'ATIVO');
SET @anual = (SELECT MIN(PLA_ID) FROM PLA_PLANO WHERE PLA_NOME = 'Anual' AND PLA_SITUACAO = 'ATIVO');
SET @professor = (SELECT PER_ID FROM PER_PERSONAL WHERE PER_USUARIO = 'professor');
SET @rafael = (SELECT PER_ID FROM PER_PERSONAL WHERE PER_USUARIO = 'p');
SET @camila = (SELECT PER_ID FROM PER_PERSONAL WHERE PER_USUARIO = 'q');
SET @aluno = (SELECT ALU_ID FROM ALU_ALUNO WHERE ALU_USUARIO = 'aluno');
SET @segunda = DATEADD('DAY', 1 - ISO_DAY_OF_WEEK(@hoje), @hoje);

INSERT INTO ALU_ALUNO (ALU_USUARIO, ALU_NOME)
SELECT 'demo' || LPAD(CAST(aluno.n AS VARCHAR), 2, '0'), aluno.nome
FROM (VALUES (1, 'Thiago Ferreira'), (2, 'Camila Ribeiro'), (3, 'Lucas Oliveira'), (4, 'Larissa Carvalho'),
             (5, 'Felipe Nogueira'), (6, 'Beatriz Lima'), (7, 'Gabriel Souza'), (8, 'Ana Clara Gomes'),
             (9, 'Rodrigo Pereira'), (10, 'Bruna Teixeira'), (11, 'Pedro Henrique Alves'), (12, 'Juliana Costa'),
             (13, 'Marcos Vinícius Rocha'), (14, 'Patrícia Souza'), (15, 'Eduardo Martins'), (16, 'Fernanda Rocha'),
             (17, 'Rafael Barbosa'), (18, 'Isabela Freitas'), (19, 'Gustavo Lopes'), (20, 'Carolina Mendes'),
             (21, 'Vinícius Araújo'), (22, 'Letícia Moura'), (23, 'Diego Cardoso'), (24, 'Amanda Pires'),
             (25, 'Leonardo Dias'), (26, 'Mariana Duarte'), (27, 'Bruno Cavalcanti'), (28, 'Gabriela Nunes'),
             (29, 'Matheus Batista'), (30, 'Natália Ramos'), (31, 'André Monteiro'), (32, 'Sofia Andrade'),
             (33, 'Caio Vasconcelos'), (34, 'Renata Campos'), (35, 'João Pedro Farias'), (36, 'Aline Correia'),
             (37, 'Ricardo Moreira'), (38, 'Vanessa Azevedo'), (39, 'Henrique Peixoto'), (40, 'Lívia Barros'),
             (41, 'Otávio Siqueira'), (42, 'Daniela Fonseca'), (43, 'Samuel Pacheco'), (44, 'Tatiane Lacerda'),
             (45, 'Igor Brandão'), (46, 'Priscila Valente'), (47, 'Fábio Guimarães'), (48, 'Helena Prado'))
    aluno(n, nome)
WHERE NOT EXISTS (SELECT 1 FROM ALU_ALUNO WHERE ALU_USUARIO = 'demo' || LPAD(CAST(aluno.n AS VARCHAR), 2, '0'));

-- Matrículas: canceladas (n ≡ 0 mod 8), trocadas por outro plano (≡ 1), canceladas e reativadas (≡ 2) e ativas.
INSERT INTO ADP_ADESAO_PLANO (ALU_ID, PLA_ID, ADP_PERIODICIDADE_MESES, ADP_INICIO, ADP_ENCERRAMENTO, ADP_SITUACAO)
SELECT demo.id,
       CASE WHEN MOD(demo.n, 8) IN (0, 1, 2) OR MOD(demo.n, 3) = 0 THEN @mensal
            WHEN MOD(demo.n, 3) = 1 THEN @trimestral ELSE @anual END,
       CASE WHEN MOD(demo.n, 8) IN (0, 1, 2) OR MOD(demo.n, 3) = 0 THEN 1 WHEN MOD(demo.n, 3) = 1 THEN 3 ELSE 12 END,
       CASE MOD(demo.n, 8) WHEN 0 THEN DATEADD('DAY', -(100 + demo.n), @hoje)
            WHEN 1 THEN DATEADD('DAY', -(180 + demo.n), @hoje)
            WHEN 2 THEN DATEADD('DAY', -(260 + demo.n), @hoje)
            WHEN 7 THEN DATEADD('DAY', -(MOD(demo.n, 16) * 2 + 1), @hoje)
            ELSE DATEADD('DAY', -(3 + MOD(demo.n * 29, 230)), @hoje) END,
       CASE MOD(demo.n, 8) WHEN 0 THEN DATEADD('DAY', -(MOD(demo.n, 40) + 3), @hoje)
            WHEN 1 THEN DATEADD('DAY', -(MOD(demo.n * 7, 50) + 8), @hoje)
            WHEN 2 THEN DATEADD('DAY', -(170 + demo.n), @hoje) END,
       CASE MOD(demo.n, 8) WHEN 0 THEN 'CANCELADA' WHEN 1 THEN 'ENCERRADA' WHEN 2 THEN 'CANCELADA' ELSE 'ATIVA' END
FROM (SELECT ALU_ID AS id, CAST(SUBSTRING(ALU_USUARIO, 5) AS INT) AS n FROM ALU_ALUNO WHERE ALU_USUARIO LIKE 'demo__') demo
WHERE @mensal IS NOT NULL AND @trimestral IS NOT NULL AND @anual IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM ADP_ADESAO_PLANO WHERE ALU_ID = demo.id);

INSERT INTO ADP_ADESAO_PLANO (ALU_ID, PLA_ID, ADP_PERIODICIDADE_MESES, ADP_INICIO, ADP_SITUACAO)
SELECT demo.id,
       CASE WHEN MOD(demo.n, 8) = 1 THEN @trimestral WHEN MOD(demo.n, 16) = 2 THEN @anual ELSE @mensal END,
       CASE WHEN MOD(demo.n, 8) = 1 THEN 3 WHEN MOD(demo.n, 16) = 2 THEN 12 ELSE 1 END,
       CASE WHEN MOD(demo.n, 8) = 1 THEN anterior.ADP_ENCERRAMENTO ELSE DATEADD('DAY', -(MOD(demo.n * 3, 25) + 2), @hoje) END,
       'ATIVA'
FROM (SELECT ALU_ID AS id, CAST(SUBSTRING(ALU_USUARIO, 5) AS INT) AS n FROM ALU_ALUNO WHERE ALU_USUARIO LIKE 'demo__') demo
JOIN ADP_ADESAO_PLANO anterior ON anterior.ALU_ID = demo.id
WHERE MOD(demo.n, 8) IN (1, 2) AND (SELECT COUNT(*) FROM ADP_ADESAO_PLANO WHERE ALU_ID = demo.id) = 1;

INSERT INTO TRC_TRANCAMENTO (ADP_ID, TRC_INICIO, TRC_FIM)
SELECT adesao.ADP_ID, DATEADD('DAY', trancamento.inicio, @hoje), DATEADD('DAY', trancamento.fim, @hoje)
FROM (VALUES ('demo05', -25, -6), ('demo13', -40, -20), ('demo21', -9, 12)) trancamento(usuario, inicio, fim)
JOIN ALU_ALUNO aluno ON aluno.ALU_USUARIO = trancamento.usuario
JOIN ADP_ADESAO_PLANO adesao ON adesao.ALU_ID = aluno.ALU_ID AND adesao.ADP_SITUACAO = 'ATIVA'
WHERE NOT EXISTS (SELECT 1 FROM TRC_TRANCAMENTO);

-- Mensalidades de cada período iniciado. Algumas ficam em aberto além da carência e três tiveram o pagamento estornado.
INSERT INTO MEN_MENSALIDADE (ADP_ID, ALU_ID, MEN_PERIODO_INICIO, MEN_PERIODO_FIM, MEN_VENCIMENTO, MEN_VALOR, MEN_SITUACAO)
SELECT periodo.ADP_ID, periodo.ALU_ID, periodo.inicio, periodo.fim, periodo.vencimento, periodo.valor,
       CASE WHEN periodo.vencimento > @hoje
              OR (MOD(periodo.n, 6) = 4 AND periodo.vencimento >= DATEADD('DAY', -50, @hoje))
              OR (MOD(periodo.n, 7) = 3 AND periodo.vencimento >= DATEADD('DAY', -20, @hoje))
              OR (MOD(periodo.n, 10) = 7 AND periodo.vencimento BETWEEN DATEADD('DAY', -40, @hoje) AND DATEADD('DAY', -10, @hoje))
            THEN 'ABERTA' ELSE 'PAGA' END
FROM (SELECT adesao.ADP_ID, adesao.ALU_ID, demo.n, plano.PLA_VALOR AS valor,
             DATEADD('MONTH', ciclo.X * adesao.ADP_PERIODICIDADE_MESES, adesao.ADP_INICIO) AS inicio,
             DATEADD('DAY', -1, DATEADD('MONTH', (ciclo.X + 1) * adesao.ADP_PERIODICIDADE_MESES, adesao.ADP_INICIO)) AS fim,
             DATEADD('DAY', 5, DATEADD('MONTH', ciclo.X * adesao.ADP_PERIODICIDADE_MESES, adesao.ADP_INICIO)) AS vencimento,
             adesao.ADP_ENCERRAMENTO AS encerramento
      FROM ADP_ADESAO_PLANO adesao
      JOIN (SELECT ALU_ID AS id, CAST(SUBSTRING(ALU_USUARIO, 5) AS INT) AS n FROM ALU_ALUNO WHERE ALU_USUARIO LIKE 'demo__') demo
        ON demo.id = adesao.ALU_ID
      JOIN PLA_PLANO plano ON plano.PLA_ID = adesao.PLA_ID
      CROSS JOIN SYSTEM_RANGE(0, 12) ciclo) periodo
WHERE periodo.inicio <= @hoje AND (periodo.encerramento IS NULL OR periodo.inicio < periodo.encerramento)
  AND NOT EXISTS (SELECT 1 FROM MEN_MENSALIDADE WHERE ADP_ID = periodo.ADP_ID);

INSERT INTO PAG_PAGAMENTO (MEN_ID, PAG_VALOR, PAG_FORMA, PAG_REGISTRO, PAG_SITUACAO, PAG_ESTORNO, PAG_MOTIVO_ESTORNO)
SELECT mensalidade.MEN_ID, mensalidade.MEN_VALOR,
       CASE MOD(demo.n, 5) WHEN 0 THEN 'PIX' WHEN 1 THEN 'CARTAO_CREDITO' WHEN 2 THEN 'BOLETO' WHEN 3 THEN 'CARTAO_DEBITO' ELSE 'DINHEIRO' END,
       CAST(CAST(DATEADD('DAY', -MOD(demo.n, 4), mensalidade.MEN_VENCIMENTO) AS VARCHAR) || ' 10:00:00-03:00' AS TIMESTAMP WITH TIME ZONE),
       CASE WHEN mensalidade.MEN_SITUACAO = 'PAGA' THEN 'CONFIRMADO' ELSE 'ESTORNADO' END,
       CASE WHEN mensalidade.MEN_SITUACAO = 'PAGA' THEN NULL
            ELSE CAST(CAST(DATEADD('DAY', 2 - MOD(demo.n, 4), mensalidade.MEN_VENCIMENTO) AS VARCHAR) || ' 15:00:00-03:00' AS TIMESTAMP WITH TIME ZONE) END,
       CASE WHEN mensalidade.MEN_SITUACAO = 'PAGA' THEN NULL ELSE 'Cobrança em duplicidade no cartão' END
FROM MEN_MENSALIDADE mensalidade
JOIN (SELECT ALU_ID AS id, CAST(SUBSTRING(ALU_USUARIO, 5) AS INT) AS n FROM ALU_ALUNO WHERE ALU_USUARIO LIKE 'demo__') demo
  ON demo.id = mensalidade.ALU_ID
WHERE (mensalidade.MEN_SITUACAO = 'PAGA'
       OR (MOD(demo.n, 10) = 7 AND mensalidade.MEN_VENCIMENTO BETWEEN DATEADD('DAY', -40, @hoje) AND DATEADD('DAY', -10, @hoje)
           AND NOT (MOD(demo.n, 6) = 4 AND mensalidade.MEN_VENCIMENTO >= DATEADD('DAY', -50, @hoje))))
  AND NOT EXISTS (SELECT 1 FROM PAG_PAGAMENTO WHERE MEN_ID = mensalidade.MEN_ID);

-- Entradas e saídas dos últimos 120 dias no controle de acesso, apenas nos dias em que o aluno estava ativo e não
-- trancado. A sala lota entre 18h e 20h.
INSERT INTO RAC_REGISTRO_ACESSO (ALU_ID, RAC_ENTRADA, RAC_SAIDA)
SELECT dia.id, CAST(CAST(dia.instante AS VARCHAR) || '-03:00' AS TIMESTAMP WITH TIME ZONE),
       CAST(CAST(DATEADD('MINUTE', 55 + MOD(dia.n * 7, 40), dia.instante) AS VARCHAR) || '-03:00' AS TIMESTAMP WITH TIME ZONE)
FROM (SELECT candidato.id, candidato.n, candidato.j, candidato.data,
             DATEADD('MINUTE', MOD(candidato.n * 11 + candidato.j * 7, 50),
                 DATEADD('HOUR', CASE
                     WHEN ISO_DAY_OF_WEEK(candidato.data) >= 6 THEN 9 + MOD(candidato.n, 2)
                     WHEN MOD(candidato.n * 7 + candidato.j * 3, 20) < 2 THEN 6
                     WHEN MOD(candidato.n * 7 + candidato.j * 3, 20) < 4 THEN 8
                     WHEN MOD(candidato.n * 7 + candidato.j * 3, 20) < 5 THEN 10
                     WHEN MOD(candidato.n * 7 + candidato.j * 3, 20) < 6 THEN 12
                     WHEN MOD(candidato.n * 7 + candidato.j * 3, 20) < 7 THEN 14
                     WHEN MOD(candidato.n * 7 + candidato.j * 3, 20) < 9 THEN 16
                     WHEN MOD(candidato.n * 7 + candidato.j * 3, 20) < 16 THEN 18
                     WHEN MOD(candidato.n * 7 + candidato.j * 3, 20) < 18 THEN 19
                     ELSE 20 END, CAST(candidato.data AS TIMESTAMP))) AS instante
      FROM (SELECT demo.id, demo.n, faixa.X AS j, DATEADD('DAY', -faixa.X, @hoje) AS data
            FROM (SELECT ALU_ID AS id, CAST(SUBSTRING(ALU_USUARIO, 5) AS INT) AS n FROM ALU_ALUNO WHERE ALU_USUARIO LIKE 'demo__') demo
            CROSS JOIN SYSTEM_RANGE(1, 120) faixa) candidato
      WHERE MOD(candidato.n * 13 + candidato.j * 5 + candidato.j / 7 * 3, 7) < MOD(candidato.n, 4) + 2
        AND (ISO_DAY_OF_WEEK(candidato.data) < 6 OR MOD(candidato.n, 3) = 0)
        AND (ISO_DAY_OF_WEEK(candidato.data) < 7 OR MOD(candidato.n, 5) = 0)
        AND NOT (MOD(candidato.n, 10) = 6 AND candidato.j < 19)
        AND NOT (MOD(candidato.n, 10) = 9 AND candidato.j < 30 AND MOD(candidato.j, 7) <> 1)
        AND EXISTS (SELECT 1 FROM ADP_ADESAO_PLANO adesao WHERE adesao.ALU_ID = candidato.id
                     AND adesao.ADP_INICIO <= candidato.data
                     AND (adesao.ADP_ENCERRAMENTO IS NULL OR candidato.data < adesao.ADP_ENCERRAMENTO))
        AND NOT EXISTS (SELECT 1 FROM TRC_TRANCAMENTO trancamento
                         JOIN ADP_ADESAO_PLANO adesao ON adesao.ADP_ID = trancamento.ADP_ID
                        WHERE adesao.ALU_ID = candidato.id
                          AND candidato.data BETWEEN trancamento.TRC_INICIO AND trancamento.TRC_FIM)) dia
WHERE NOT EXISTS (SELECT 1 FROM RAC_REGISTRO_ACESSO entrada JOIN ALU_ALUNO aluno ON aluno.ALU_ID = entrada.ALU_ID
                   WHERE aluno.ALU_USUARIO LIKE 'demo__');

INSERT INTO EST_USO_ESTACIONAMENTO (ALU_ID, EST_ENTRADA, EST_SAIDA)
SELECT entrada.ALU_ID, DATEADD('MINUTE', -5, entrada.horario),
       DATEADD('MINUTE', 60 + MOD(entrada.ALU_ID * 7, 40), entrada.horario)
FROM (SELECT ALU_ID, RAC_ENTRADA AS registro, CAST(RAC_ENTRADA AT TIME ZONE '-03:00' AS TIMESTAMP) AS horario
      FROM RAC_REGISTRO_ACESSO) entrada
JOIN (SELECT ALU_ID AS id, CAST(SUBSTRING(ALU_USUARIO, 5) AS INT) AS n FROM ALU_ALUNO WHERE ALU_USUARIO LIKE 'demo__') demo
  ON demo.id = entrada.ALU_ID
WHERE MOD(demo.n, 3) = 0 AND NOT EXISTS (SELECT 1 FROM EST_USO_ESTACIONAMENTO);

-- Planos de treino: ativos para quem está matriculado e encerrados para quem cancelou.
INSERT INTO PLT_PLANO_TREINO (ALU_ID, PER_ID, PLT_NOME, PLT_FREQUENCIA_SEMANAL, PLT_SITUACAO, PLT_VERSAO, PLT_CRIACAO, PLT_ENCERRAMENTO)
SELECT demo.id, CASE MOD(demo.n, 3) WHEN 0 THEN @professor WHEN 1 THEN @rafael ELSE @camila END,
       CASE MOD(demo.n, 4) WHEN 0 THEN 'Hipertrofia fase 1' WHEN 1 THEN 'Emagrecimento fase 1'
            WHEN 2 THEN 'Condicionamento geral' ELSE 'Hipertrofia fase 2' END,
       CASE WHEN demo.n IN (3, 6, 12, 15) THEN 2 ELSE MOD(demo.n, 3) + 2 END,
       CASE WHEN adesao.ADP_SITUACAO = 'ATIVA' THEN 'ATIVO' ELSE 'ENCERRADO' END, 1,
       CAST(CAST(adesao.ADP_INICIO AS VARCHAR) || ' 12:00:00-03:00' AS TIMESTAMP WITH TIME ZONE),
       CASE WHEN adesao.ADP_SITUACAO = 'ATIVA' THEN NULL
            ELSE CAST(CAST(adesao.ADP_ENCERRAMENTO AS VARCHAR) || ' 12:00:00-03:00' AS TIMESTAMP WITH TIME ZONE) END
FROM (SELECT ALU_ID AS id, CAST(SUBSTRING(ALU_USUARIO, 5) AS INT) AS n FROM ALU_ALUNO WHERE ALU_USUARIO LIKE 'demo__') demo
JOIN ADP_ADESAO_PLANO adesao ON adesao.ALU_ID = demo.id
 AND adesao.ADP_INICIO = (SELECT MAX(ADP_INICIO) FROM ADP_ADESAO_PLANO WHERE ALU_ID = demo.id)
WHERE @professor IS NOT NULL AND @rafael IS NOT NULL AND @camila IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM PLT_PLANO_TREINO WHERE ALU_ID = demo.id);

INSERT INTO TRE_TREINO (PLT_ID, TRE_INDICE, TRE_NOME, TRE_DIA, TRE_OBJETIVO)
SELECT plano.PLT_ID, treino.indice, treino.nome, treino.dia, 'HIPERTROFIA'
FROM PLT_PLANO_TREINO plano
JOIN ALU_ALUNO aluno ON aluno.ALU_ID = plano.ALU_ID AND aluno.ALU_USUARIO IN ('demo03', 'demo06', 'demo12', 'demo15')
CROSS JOIN (VALUES (0, 'Treino A · peito e tríceps', 'SEGUNDA'), (1, 'Treino B · costas e pernas', 'QUINTA')) treino(indice, nome, dia)
WHERE plano.PLT_SITUACAO = 'ATIVO' AND NOT EXISTS (SELECT 1 FROM TRE_TREINO WHERE PLT_ID = plano.PLT_ID);

INSERT INTO SRE_SERIE (TRE_ID, SRE_INDICE, EXE_ID, SRE_QUANTIDADE, SRE_REPETICOES_MINIMAS, SRE_REPETICOES_MAXIMAS,
                       SRE_CARGA, SRE_INTERVALO_SEGUNDOS)
SELECT treino.TRE_ID, serie.indice, exercicio.EXE_ID, serie.series, serie.minimas, serie.maximas, serie.carga, serie.intervalo
FROM TRE_TREINO treino
JOIN PLT_PLANO_TREINO plano ON plano.PLT_ID = treino.PLT_ID
JOIN ALU_ALUNO aluno ON aluno.ALU_ID = plano.ALU_ID AND aluno.ALU_USUARIO IN ('demo03', 'demo06', 'demo12', 'demo15')
JOIN (VALUES ('SEGUNDA', 0, 'Supino reto', 4, 8, 10, 40.00, 90), ('SEGUNDA', 1, 'Tríceps na polia', 3, 10, 12, 25.00, 60),
             ('QUINTA', 0, 'Remada curvada', 4, 8, 10, 35.00, 90), ('QUINTA', 1, 'Agachamento livre', 4, 8, 10, 60.00, 120))
    serie(dia, indice, exercicio, series, minimas, maximas, carga, intervalo) ON serie.dia = treino.TRE_DIA
JOIN EXE_EXERCICIO exercicio ON exercicio.EXE_NOME = serie.exercicio
WHERE NOT EXISTS (SELECT 1 FROM SRE_SERIE WHERE TRE_ID = treino.TRE_ID);

-- Execuções registradas em parte dos dias de entrada, para a adesão aos treinos de cada personal.
INSERT INTO EXC_EXECUCAO_EXERCICIO (ALU_ID, PLT_ID, EXE_ID, EXC_DIA, EXC_DATA, EXC_SERIES, EXC_REPETICOES, EXC_CARGA)
SELECT entrada.ALU_ID, plano.PLT_ID, exercicio.EXE_ID,
       CASE ISO_DAY_OF_WEEK(CAST(entrada.horario AS DATE)) WHEN 1 THEN 'SEGUNDA' WHEN 2 THEN 'TERCA' WHEN 3 THEN 'QUARTA'
            WHEN 4 THEN 'QUINTA' WHEN 5 THEN 'SEXTA' WHEN 6 THEN 'SABADO' ELSE 'DOMINGO' END,
       DATEADD('MINUTE', 40, entrada.registro), 3, 12, 30.00 + MOD(DATEDIFF('DAY', plano.PLT_CRIACAO, entrada.horario), 60) / 10 * 2.5
FROM (SELECT ALU_ID, RAC_ENTRADA AS registro, CAST(RAC_ENTRADA AT TIME ZONE '-03:00' AS TIMESTAMP) AS horario
      FROM RAC_REGISTRO_ACESSO) entrada
JOIN (SELECT ALU_ID AS id, CAST(SUBSTRING(ALU_USUARIO, 5) AS INT) AS n FROM ALU_ALUNO WHERE ALU_USUARIO LIKE 'demo__') demo
  ON demo.id = entrada.ALU_ID
JOIN PLT_PLANO_TREINO plano ON plano.ALU_ID = entrada.ALU_ID AND plano.PLT_CRIACAO <= entrada.registro
 AND (plano.PLT_ENCERRAMENTO IS NULL OR entrada.registro < plano.PLT_ENCERRAMENTO)
JOIN EXE_EXERCICIO exercicio ON exercicio.EXE_NOME = 'Puxada frontal'
WHERE MOD(demo.n + DATEDIFF('DAY', DATE '2000-01-03', entrada.horario), 4 + MOD(demo.n, 3)) <> 0
  AND NOT EXISTS (SELECT 1 FROM EXC_EXECUCAO_EXERCICIO execucao JOIN ALU_ALUNO aluno ON aluno.ALU_ID = execucao.ALU_ID
                   WHERE aluno.ALU_USUARIO LIKE 'demo__');

-- Exercícios sem evolução nas últimas quatro execuções, que originam as sugestões de troca.
INSERT INTO EXC_EXECUCAO_EXERCICIO (ALU_ID, PLT_ID, EXE_ID, EXC_DIA, EXC_DATA, EXC_SERIES, EXC_REPETICOES, EXC_CARGA)
SELECT plano.ALU_ID, plano.PLT_ID, exercicio.EXE_ID, estagnado.dia,
       CAST(CAST(DATEADD('DAY', estagnado.desvio - 7 * estagnado.semana, @segunda) AS VARCHAR) || ' 19:10:00-03:00' AS TIMESTAMP WITH TIME ZONE),
       4, estagnado.repeticoes, estagnado.carga
FROM (VALUES ('demo03', 'Supino reto', 'SEGUNDA', 0, 4, 10, 40.00), ('demo03', 'Supino reto', 'SEGUNDA', 0, 3, 9, 40.00),
             ('demo03', 'Supino reto', 'SEGUNDA', 0, 2, 8, 40.00), ('demo03', 'Supino reto', 'SEGUNDA', 0, 1, 8, 40.00),
             ('demo06', 'Remada curvada', 'QUINTA', 3, 5, 10, 35.00), ('demo06', 'Remada curvada', 'QUINTA', 3, 4, 10, 35.00),
             ('demo06', 'Remada curvada', 'QUINTA', 3, 3, 9, 35.00), ('demo06', 'Remada curvada', 'QUINTA', 3, 2, 9, 35.00),
             ('demo12', 'Agachamento livre', 'QUINTA', 3, 5, 10, 60.00), ('demo12', 'Agachamento livre', 'QUINTA', 3, 4, 10, 60.00),
             ('demo12', 'Agachamento livre', 'QUINTA', 3, 3, 10, 60.00), ('demo12', 'Agachamento livre', 'QUINTA', 3, 2, 9, 60.00))
    estagnado(usuario, exercicio, dia, desvio, semana, repeticoes, carga)
JOIN ALU_ALUNO aluno ON aluno.ALU_USUARIO = estagnado.usuario
JOIN PLT_PLANO_TREINO plano ON plano.ALU_ID = aluno.ALU_ID AND plano.PLT_SITUACAO = 'ATIVO'
JOIN EXE_EXERCICIO exercicio ON exercicio.EXE_NOME = estagnado.exercicio
WHERE NOT EXISTS (SELECT 1 FROM EXC_EXECUCAO_EXERCICIO WHERE ALU_ID = aluno.ALU_ID AND EXE_ID = exercicio.EXE_ID);

-- Remada curvada da conta "aluno" sem evolução nas quatro últimas segundas-feiras em que ele treinou.
SET @plano_aluno = (SELECT PLT_ID FROM PLT_PLANO_TREINO WHERE ALU_ID = @aluno AND PLT_SITUACAO = 'ATIVO');
SET @remada = (SELECT EXE_ID FROM EXE_EXERCICIO WHERE EXE_NOME = 'Remada curvada');
INSERT INTO EXC_EXECUCAO_EXERCICIO (ALU_ID, PLT_ID, EXE_ID, EXC_DIA, EXC_DATA, EXC_SERIES, EXC_REPETICOES, EXC_CARGA)
SELECT @aluno, @plano_aluno, @remada, 'SEGUNDA',
       CAST(CAST(DATEADD('DAY', -7 * execucao.semana, @segunda) AS VARCHAR) || ' 19:05:00-03:00' AS TIMESTAMP WITH TIME ZONE),
       3, execucao.repeticoes, 20.00
FROM (VALUES (7, 10), (6, 10), (5, 9), (4, 9)) execucao(semana, repeticoes)
WHERE @plano_aluno IS NOT NULL AND @remada IS NOT NULL
  AND EXISTS (SELECT 1 FROM TRE_TREINO treino JOIN SRE_SERIE serie ON serie.TRE_ID = treino.TRE_ID
               WHERE treino.PLT_ID = @plano_aluno AND serie.EXE_ID = @remada)
  AND NOT EXISTS (SELECT 1 FROM EXC_EXECUCAO_EXERCICIO WHERE ALU_ID = @aluno AND EXE_ID = @remada);

-- Avaliações físicas e metas atingidas no período por parte dos alunos de cada personal.
INSERT INTO AVF_AVALIACAO_FISICA (ALU_ID, PER_ID, AVF_DATA, AVF_PESO, AVF_ALTURA, AVF_IMC, AVF_SITUACAO, AVF_VERSAO, AVF_REGISTRO)
SELECT plano.ALU_ID, plano.PER_ID, DATEADD('DAY', -70, @hoje), 80.00 - MOD(demo.n, 7), 175.00,
       ROUND((80.00 - MOD(demo.n, 7)) / (1.75 * 1.75), 2), 'VALIDA', 1,
       CAST(CAST(DATEADD('DAY', -70, @hoje) AS VARCHAR) || ' 09:00:00-03:00' AS TIMESTAMP WITH TIME ZONE)
FROM PLT_PLANO_TREINO plano
JOIN (SELECT ALU_ID AS id, CAST(SUBSTRING(ALU_USUARIO, 5) AS INT) AS n FROM ALU_ALUNO WHERE ALU_USUARIO LIKE 'demo__') demo
  ON demo.id = plano.ALU_ID
WHERE plano.PLT_SITUACAO = 'ATIVO' AND MOD(demo.n, 4) = 0
  AND NOT EXISTS (SELECT 1 FROM AVF_AVALIACAO_FISICA WHERE ALU_ID = plano.ALU_ID);

INSERT INTO MET_META (ALU_ID, PER_ID, MET_INDICADOR, MET_VALOR_INICIAL, MET_VALOR_DESEJADO, MET_INICIO, MET_FIM, AVF_ID,
                      MET_SITUACAO, MET_DATA_ATINGIDA)
SELECT avaliacao.ALU_ID, avaliacao.PER_ID, 'PESO', avaliacao.AVF_PESO, avaliacao.AVF_PESO - 2, avaliacao.AVF_DATA,
       DATEADD('DAY', 60, @hoje), avaliacao.AVF_ID, 'ATINGIDA', DATEADD('DAY', -(1 + MOD(demo.n, 25)), @hoje)
FROM AVF_AVALIACAO_FISICA avaliacao
JOIN (SELECT ALU_ID AS id, CAST(SUBSTRING(ALU_USUARIO, 5) AS INT) AS n FROM ALU_ALUNO WHERE ALU_USUARIO LIKE 'demo__') demo
  ON demo.id = avaliacao.ALU_ID
WHERE NOT EXISTS (SELECT 1 FROM MET_META WHERE ALU_ID = avaliacao.ALU_ID);

-- Peso subindo em avaliações seguidas, contra a meta de redução: origina a sugestão de revisão do plano.
SET @eduardo = (SELECT ALU_ID FROM ALU_ALUNO WHERE ALU_USUARIO = 'demo15');
INSERT INTO AVF_AVALIACAO_FISICA (ALU_ID, PER_ID, AVF_DATA, AVF_PESO, AVF_ALTURA, AVF_IMC, AVF_SITUACAO, AVF_VERSAO, AVF_REGISTRO)
SELECT @eduardo, @professor, DATEADD('DAY', -avaliacao.dias, @hoje), avaliacao.peso, 178.00,
       ROUND(avaliacao.peso / (1.78 * 1.78), 2), 'VALIDA', 1,
       CAST(CAST(DATEADD('DAY', -avaliacao.dias, @hoje) AS VARCHAR) || ' 09:00:00-03:00' AS TIMESTAMP WITH TIME ZONE)
FROM (VALUES (60, 88.00), (32, 88.60), (6, 89.40)) avaliacao(dias, peso)
WHERE @eduardo IS NOT NULL AND NOT EXISTS (SELECT 1 FROM AVF_AVALIACAO_FISICA WHERE ALU_ID = @eduardo);

INSERT INTO MET_META (ALU_ID, PER_ID, MET_INDICADOR, MET_VALOR_INICIAL, MET_VALOR_DESEJADO, MET_INICIO, MET_FIM, AVF_ID, MET_SITUACAO)
SELECT @eduardo, @professor, 'PESO', 88.00, 82.00, avaliacao.AVF_DATA, DATEADD('DAY', 45, @hoje), avaliacao.AVF_ID, 'ATIVA'
FROM AVF_AVALIACAO_FISICA avaliacao
WHERE avaliacao.ALU_ID = @eduardo AND avaliacao.AVF_DATA = DATEADD('DAY', -60, @hoje)
  AND NOT EXISTS (SELECT 1 FROM MET_META WHERE ALU_ID = @eduardo);

-- Inscrições nas aulas coletivas. Spinning e Pilates de terça ficam lotadas.
INSERT INTO IAC_INSCRICAO_AULA (AUC_ID, ALU_ID, IAC_DATA)
SELECT ordem.AUC_ID, ordem.id, CAST(CAST(DATEADD('DAY', -MOD(ordem.id, 60) - 1, @hoje) AS VARCHAR) || ' 12:00:00-03:00' AS TIMESTAMP WITH TIME ZONE)
FROM (SELECT aula.AUC_ID, demo.id,
             ROW_NUMBER() OVER (PARTITION BY aula.AUC_ID ORDER BY MOD(demo.id * 31 + aula.AUC_ID * 17, 97), demo.id) AS posicao,
             CASE WHEN aula.AUC_NOME = 'Pilates solo' AND aula.AUC_DIA = 'QUARTA' THEN 11
                  WHEN aula.AUC_NOME = 'Pilates solo' THEN 15
                  WHEN aula.AUC_NOME = 'Alongamento e mobilidade' AND aula.AUC_DIA = 'SEGUNDA' THEN 11
                  WHEN aula.AUC_NOME = 'Alongamento e mobilidade' THEN 6
                  WHEN aula.AUC_NOME = 'Core e abdômen' THEN 18
                  WHEN aula.AUC_NOME = 'Spinning' THEN 20
                  WHEN aula.AUC_NOME = 'Jump' THEN 14
                  WHEN aula.AUC_NOME = 'Funcional' THEN 12
                  WHEN aula.AUC_NOME = 'Levantamento olímpico' THEN 7
                  ELSE 9 END AS alvo
      FROM AUC_AULA_COLETIVA aula
      CROSS JOIN (SELECT ALU_ID AS id FROM ALU_ALUNO WHERE ALU_USUARIO LIKE 'demo__') demo) ordem
WHERE ordem.posicao <= ordem.alvo
  AND NOT EXISTS (SELECT 1 FROM IAC_INSCRICAO_AULA inscricao JOIN ALU_ALUNO aluno ON aluno.ALU_ID = inscricao.ALU_ID
                   WHERE aluno.ALU_USUARIO LIKE 'demo__');

-- Sugestões de treino aguardando a análise do personal "professor" e decisões anteriores dele.
INSERT INTO REC_RECOMENDACAO (ALU_ID, REC_CATEGORIA, REC_CRITERIO, REC_ITEM, REC_TITULO, REC_DESCRICAO, REC_JUSTIFICATIVA,
    PER_ID, PLT_ID, EXE_ATUAL, EXE_SUGERIDO, REC_SERIES, REC_REPETICOES_MINIMAS, REC_REPETICOES_MAXIMAS, REC_CARGA,
    REC_INTERVALO_SEGUNDOS, REC_GERACAO, REC_VALIDADE, REC_SITUACAO, REC_RESPOSTA, REC_RESPONDENTE, REC_MOTIVO, REC_AJUSTE)
SELECT plano.ALU_ID, 'AJUSTES_TREINO', sugestao.criterio,
       CASE WHEN sugestao.criterio = 'SUBSTITUICAO_EXERCICIO' THEN 'EXERCICIO:' || atual.EXE_ID
            ELSE COALESCE('META:' || CAST((SELECT MIN(MET_ID) FROM MET_META WHERE ALU_ID = plano.ALU_ID AND MET_SITUACAO = 'ATIVA') AS VARCHAR),
                          'PLANO_TREINO:' || plano.PLT_ID) END,
       sugestao.titulo, sugestao.descricao, sugestao.justificativa, plano.PER_ID, plano.PLT_ID,
       atual.EXE_ID, substituto.EXE_ID, sugestao.series, sugestao.minimas, sugestao.maximas, sugestao.carga, sugestao.intervalo,
       CAST(CAST(DATEADD('DAY', -sugestao.dias, @hoje) AS VARCHAR) || ' 06:00:00-03:00' AS TIMESTAMP WITH TIME ZONE),
       DATEADD('DAY', 14 - sugestao.dias, @hoje), sugestao.situacao,
       CASE WHEN sugestao.situacao = 'AGUARDANDO_APROVACAO' THEN NULL
            ELSE CAST(CAST(DATEADD('DAY', 2 - sugestao.dias, @hoje) AS VARCHAR) || ' 18:00:00-03:00' AS TIMESTAMP WITH TIME ZONE) END,
       CASE WHEN sugestao.situacao = 'AGUARDANDO_APROVACAO' THEN NULL ELSE 'Professor' END,
       sugestao.motivo, sugestao.ajuste
FROM (VALUES
        ('aluno', 'SUBSTITUICAO_EXERCICIO', 'Remada curvada', 'Remada baixa na polia', 'Substituir remada curvada',
         'Treino A · superiores: remada curvada → remada baixa na polia. Sem evolução de carga ou repetições em 4 treinos seguidos.',
         'A carga e as repetições não aumentaram em 4 execuções seguidas. O limite definido pela academia é de 4 execuções.',
         3, 8, 12, 20.00, 90, 1, 'AGUARDANDO_APROVACAO', CAST(NULL AS VARCHAR(250)), CAST(NULL AS VARCHAR(250))),
        ('demo03', 'SUBSTITUICAO_EXERCICIO', 'Supino reto', 'Supino inclinado com halteres', 'Substituir supino reto',
         'Treino A · peito e tríceps: supino reto → supino inclinado com halteres. Sem evolução de carga ou repetições em 4 treinos seguidos.',
         'A carga e as repetições não aumentaram em 4 execuções seguidas. O limite definido pela academia é de 4 execuções.',
         4, 8, 10, 14.00, 90, 14, 'AGUARDANDO_APROVACAO', CAST(NULL AS VARCHAR(250)), CAST(NULL AS VARCHAR(250))),
        ('demo15', 'REVISAO_PLANO', 'Supino reto', 'Supino reto', 'Revisar o plano de treino',
         'Peso subiu em 2 avaliações seguidas; a meta é chegar a 82 kg até o fim do período.',
         'Tendência contrária à meta de peso: 88 → 88,6 → 89,4 kg nas últimas avaliações',
         CAST(NULL AS INT), CAST(NULL AS INT), CAST(NULL AS INT), CAST(NULL AS DECIMAL(7,2)), CAST(NULL AS INT), 6,
         'AGUARDANDO_APROVACAO', CAST(NULL AS VARCHAR(250)), CAST(NULL AS VARCHAR(250))),
        ('demo06', 'SUBSTITUICAO_EXERCICIO', 'Remada curvada', 'Remada baixa na polia', 'Substituir remada curvada',
         'Treino B · costas e pernas: remada curvada → remada baixa na polia. Sem evolução de carga ou repetições em 4 treinos seguidos.',
         'A carga e as repetições não aumentaram em 4 execuções seguidas. O limite definido pela academia é de 4 execuções.',
         4, 8, 10, 35.00, 90, 4, 'AGUARDANDO_APROVACAO', CAST(NULL AS VARCHAR(250)), CAST(NULL AS VARCHAR(250))),
        ('demo12', 'SUBSTITUICAO_EXERCICIO', 'Agachamento livre', 'Elevação pélvica', 'Substituir agachamento livre',
         'Treino B · costas e pernas: agachamento livre → elevação pélvica. Sem evolução de carga ou repetições em 4 treinos seguidos.',
         'A carga e as repetições não aumentaram em 4 execuções seguidas. O limite definido pela academia é de 4 execuções.',
         4, 8, 10, 60.00, 120, 2, 'AGUARDANDO_APROVACAO', CAST(NULL AS VARCHAR(250)), CAST(NULL AS VARCHAR(250))),
        ('demo09', 'REVISAO_PLANO', 'Supino reto', 'Supino reto', 'Revisar o plano de treino',
         'Cintura subiu em 2 avaliações seguidas; a meta é reduzir a medida até o fim do período.',
         'Tendência contrária à meta de cintura nas últimas avaliações',
         CAST(NULL AS INT), CAST(NULL AS INT), CAST(NULL AS INT), CAST(NULL AS DECIMAL(7,2)), CAST(NULL AS INT), 30,
         'REJEITADA', 'Aluno voltou de lesão; vamos reavaliar antes de mudar o plano', CAST(NULL AS VARCHAR(250))),
        ('demo18', 'REVISAO_PLANO', 'Supino reto', 'Supino reto', 'Revisar o plano de treino',
         'Peso caiu em 2 avaliações seguidas; a meta é ganhar massa até o fim do período.',
         'Tendência contrária à meta de peso nas últimas avaliações',
         CAST(NULL AS INT), CAST(NULL AS INT), CAST(NULL AS INT), CAST(NULL AS DECIMAL(7,2)), CAST(NULL AS INT), 25,
         'APROVADA', CAST(NULL AS VARCHAR(250)), 'Volume do treino B aumentado e orientação para procurar o nutricionista.'))
    sugestao(usuario, criterio, exercicio, substituto, titulo, descricao, justificativa, series, minimas, maximas, carga,
             intervalo, dias, situacao, motivo, ajuste)
JOIN ALU_ALUNO aluno ON aluno.ALU_USUARIO = sugestao.usuario
JOIN PLT_PLANO_TREINO plano ON plano.ALU_ID = aluno.ALU_ID AND plano.PLT_SITUACAO = 'ATIVO' AND plano.PER_ID = @professor
JOIN EXE_EXERCICIO atual ON atual.EXE_NOME = sugestao.exercicio
JOIN EXE_EXERCICIO substituto ON substituto.EXE_NOME = sugestao.substituto
WHERE (sugestao.usuario <> 'aluno' OR EXISTS (SELECT 1 FROM EXC_EXECUCAO_EXERCICIO WHERE ALU_ID = @aluno AND EXE_ID = @remada))
  AND NOT EXISTS (SELECT 1 FROM REC_RECOMENDACAO WHERE ALU_ID = aluno.ALU_ID AND REC_CATEGORIA = 'AJUSTES_TREINO');

UPDATE REC_RECOMENDACAO SET EXE_ATUAL = NULL, EXE_SUGERIDO = NULL WHERE REC_CRITERIO = 'REVISAO_PLANO';

-- Dados que fundamentam as sugestões: as execuções sem evolução e as avaliações com tendência contrária à meta.
INSERT INTO REV_EVIDENCIA (REC_ID, REV_INDICE, REV_ROTULO, REV_VALOR, REV_DESTAQUE)
SELECT recomendacao.REC_ID, ROW_NUMBER() OVER (PARTITION BY recomendacao.REC_ID ORDER BY execucao.EXC_DATA) - 1,
       FORMATDATETIME(execucao.EXC_DATA, 'dd/MM/yyyy', 'pt', 'America/Sao_Paulo') || ' · ' || execucao.EXC_SERIES || 'x' || execucao.EXC_REPETICOES,
       execucao.EXC_CARGA, FALSE
FROM REC_RECOMENDACAO recomendacao
JOIN EXC_EXECUCAO_EXERCICIO execucao ON execucao.ALU_ID = recomendacao.ALU_ID AND execucao.EXE_ID = recomendacao.EXE_ATUAL
WHERE recomendacao.REC_CRITERIO = 'SUBSTITUICAO_EXERCICIO'
  AND NOT EXISTS (SELECT 1 FROM REV_EVIDENCIA WHERE REC_ID = recomendacao.REC_ID);

INSERT INTO REV_EVIDENCIA (REC_ID, REV_INDICE, REV_ROTULO, REV_VALOR, REV_DESTAQUE)
SELECT recomendacao.REC_ID, ROW_NUMBER() OVER (PARTITION BY recomendacao.REC_ID ORDER BY avaliacao.AVF_DATA) - 1,
       FORMATDATETIME(avaliacao.AVF_DATA, 'dd/MM/yyyy'), avaliacao.AVF_PESO, TRUE
FROM REC_RECOMENDACAO recomendacao
JOIN AVF_AVALIACAO_FISICA avaliacao ON avaliacao.ALU_ID = recomendacao.ALU_ID
WHERE recomendacao.REC_CRITERIO = 'REVISAO_PLANO' AND recomendacao.ALU_ID = @eduardo
  AND NOT EXISTS (SELECT 1 FROM REV_EVIDENCIA WHERE REC_ID = recomendacao.REC_ID);

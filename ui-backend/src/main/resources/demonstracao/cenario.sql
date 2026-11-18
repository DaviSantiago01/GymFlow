-- Cenário exclusivo de desenvolvimento. Os exemplos pertencem às contas de demonstração.
SET @aluno = (SELECT ALU_ID FROM ALU_ALUNO WHERE ALU_USUARIO = 'aluno');
SET @professor = (SELECT PER_ID FROM PER_PERSONAL WHERE PER_USUARIO = 'professor');
SET @mensal = (SELECT MIN(PLA_ID) FROM PLA_PLANO WHERE PLA_NOME = 'Mensal' AND PLA_SITUACAO = 'ATIVO');
SET @hoje = CURRENT_DATE;
SET @agora = CURRENT_TIMESTAMP;
SET @inicio = DATEADD('MONTH', -2, @hoje);
SET @passado = DATEADD('DAY', -14, @agora);

-- Prescrição e histórico de cargas.
INSERT INTO PLT_PLANO_TREINO (ALU_ID, PER_ID, PLT_NOME, PLT_FREQUENCIA_SEMANAL, PLT_SITUACAO, PLT_VERSAO, PLT_CRIACAO)
SELECT @aluno, @professor, 'Força e técnica · demonstração', 2, 'ATIVO', 1, DATEADD('DAY', -35, @agora)
WHERE NOT EXISTS (SELECT 1 FROM PLT_PLANO_TREINO WHERE ALU_ID = @aluno);

SET @plano_treino = (SELECT MIN(PLT_ID) FROM PLT_PLANO_TREINO
    WHERE ALU_ID = @aluno AND PER_ID = @professor AND PLT_NOME = 'Força e técnica · demonstração' AND PLT_VERSAO = 1);

INSERT INTO TRE_TREINO (PLT_ID, TRE_INDICE, TRE_NOME, TRE_DIA, TRE_OBJETIVO)
SELECT @plano_treino, dia.indice, dia.nome, dia.dia, 'HIPERTROFIA'
FROM (VALUES (0, 'Treino A · superiores', 'SEGUNDA'), (1, 'Treino B · inferiores', 'QUINTA')) dia(indice, nome, dia)
WHERE @plano_treino IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM TRE_TREINO WHERE PLT_ID = @plano_treino AND TRE_DIA = dia.dia);

INSERT INTO SRE_SERIE (TRE_ID, SRE_INDICE, EXE_ID, SRE_QUANTIDADE, SRE_REPETICOES_MINIMAS,
                      SRE_REPETICOES_MAXIMAS, SRE_CARGA, SRE_INTERVALO_SEGUNDOS)
SELECT treino.TRE_ID, serie.indice, exercicio.EXE_ID, 3, 8, 12, serie.carga, 90
FROM (VALUES ('SEGUNDA', 0, 'Supino reto', 22.50), ('SEGUNDA', 1, 'Remada curvada', 20.00),
             ('QUINTA', 0, 'Agachamento livre', 30.00), ('QUINTA', 1, 'Leg press', 55.00))
    serie(dia, indice, exercicio, carga)
JOIN TRE_TREINO treino ON treino.PLT_ID = @plano_treino AND treino.TRE_DIA = serie.dia
JOIN EXE_EXERCICIO exercicio ON exercicio.EXE_NOME = serie.exercicio
WHERE NOT EXISTS (SELECT 1 FROM SRE_SERIE WHERE TRE_ID = treino.TRE_ID AND SRE_INDICE = serie.indice);

INSERT INTO EXC_EXECUCAO_EXERCICIO (ALU_ID, PLT_ID, EXE_ID, EXC_DIA, EXC_DATA, EXC_SERIES, EXC_REPETICOES, EXC_CARGA)
SELECT @aluno, @plano_treino, exercicio.EXE_ID, 'SEGUNDA',
       DATEADD('DAY', -(MOD(DAY_OF_WEEK(@hoje) + 5, 7) + 7 * amostra.semanas), @agora), 3, 10, amostra.carga
FROM (VALUES (3, 17.50), (2, 20.00), (1, 22.50)) amostra(semanas, carga)
JOIN EXE_EXERCICIO exercicio ON exercicio.EXE_NOME = 'Supino reto'
WHERE @plano_treino IS NOT NULL AND NOT EXISTS (SELECT 1 FROM EXC_EXECUCAO_EXERCICIO WHERE PLT_ID = @plano_treino);

-- Avaliações físicas e meta. As massas e o IMC derivam da mesma altura.
INSERT INTO AVF_AVALIACAO_FISICA (ALU_ID, PER_ID, AVF_DATA, AVF_PESO, AVF_ALTURA, AVF_IMC,
                                AVF_SITUACAO, AVF_VERSAO, AVF_REGISTRO)
SELECT @aluno, @professor, DATEADD('DAY', -amostra.dias, @hoje), amostra.peso, 172.00,
       ROUND(amostra.peso / (1.72 * 1.72), 2), 'VALIDA', 1, DATEADD('DAY', -amostra.dias, @agora)
FROM (VALUES (42, 75.00), (21, 74.20), (2, 73.40)) amostra(dias, peso)
WHERE NOT EXISTS (SELECT 1 FROM AVF_AVALIACAO_FISICA WHERE ALU_ID = @aluno);

INSERT INTO MDC_MEDIDA_CORPORAL (AVF_ID, MDC_INDICE, MDC_INDICADOR, MDC_VALOR)
SELECT avaliacao.AVF_ID, 0, 'CINTURA', 80.00 + (avaliacao.AVF_PESO - 73.40)
FROM AVF_AVALIACAO_FISICA avaliacao
WHERE avaliacao.ALU_ID = @aluno AND avaliacao.PER_ID = @professor
  AND avaliacao.AVF_REGISTRO IN (DATEADD('DAY', -42, @agora), DATEADD('DAY', -21, @agora), DATEADD('DAY', -2, @agora))
  AND NOT EXISTS (SELECT 1 FROM MDC_MEDIDA_CORPORAL WHERE AVF_ID = avaliacao.AVF_ID);

INSERT INTO MET_META (ALU_ID, PER_ID, MET_INDICADOR, MET_VALOR_INICIAL, MET_VALOR_DESEJADO,
                      MET_INICIO, MET_FIM, AVF_ID, MET_SITUACAO)
SELECT @aluno, @professor, 'CINTURA', medida.MDC_VALOR, 79.00, avaliacao.AVF_DATA,
       DATEADD('DAY', 75, avaliacao.AVF_DATA), avaliacao.AVF_ID, 'ATIVA'
FROM AVF_AVALIACAO_FISICA avaliacao
JOIN MDC_MEDIDA_CORPORAL medida ON medida.AVF_ID = avaliacao.AVF_ID AND medida.MDC_INDICADOR = 'CINTURA'
WHERE avaliacao.AVF_ID = (SELECT MIN(AVF_ID) FROM AVF_AVALIACAO_FISICA
    WHERE ALU_ID = @aluno AND PER_ID = @professor AND AVF_SITUACAO = 'VALIDA')
  AND avaliacao.AVF_REGISTRO = DATEADD('DAY', -42, @agora)
  AND NOT EXISTS (SELECT 1 FROM MET_META WHERE ALU_ID = @aluno);

-- Adesão e mensalidades pagas e em aberto.
INSERT INTO ADP_ADESAO_PLANO (ALU_ID, PLA_ID, ADP_PERIODICIDADE_MESES, ADP_INICIO, ADP_SITUACAO)
SELECT @aluno, @mensal, 1, @inicio, 'ATIVA'
WHERE @mensal IS NOT NULL AND NOT EXISTS (SELECT 1 FROM ADP_ADESAO_PLANO WHERE ALU_ID = @aluno);

SET @adesao = (SELECT MIN(ADP_ID) FROM ADP_ADESAO_PLANO
    WHERE ALU_ID = @aluno AND PLA_ID = @mensal AND ADP_INICIO = @inicio);

INSERT INTO MEN_MENSALIDADE (ADP_ID, ALU_ID, MEN_PERIODO_INICIO, MEN_PERIODO_FIM, MEN_VENCIMENTO, MEN_VALOR, MEN_SITUACAO)
SELECT @adesao, @aluno, DATEADD('MONTH', periodo.mes, @inicio),
       DATEADD('DAY', -1, DATEADD('MONTH', periodo.mes + 1, @inicio)),
       DATEADD('MONTH', periodo.mes, @inicio), plano.PLA_VALOR, periodo.situacao
FROM (VALUES (0, 'PAGA'), (1, 'ABERTA'), (2, 'ABERTA')) periodo(mes, situacao)
JOIN PLA_PLANO plano ON plano.PLA_ID = @mensal
WHERE @adesao IS NOT NULL AND NOT EXISTS (SELECT 1 FROM MEN_MENSALIDADE WHERE ADP_ID = @adesao);

INSERT INTO PAG_PAGAMENTO (MEN_ID, PAG_VALOR, PAG_FORMA, PAG_REGISTRO, PAG_SITUACAO)
SELECT mensalidade.MEN_ID, mensalidade.MEN_VALOR, 'PIX',
       CAST(mensalidade.MEN_VENCIMENTO AS TIMESTAMP WITH TIME ZONE), 'CONFIRMADO'
FROM MEN_MENSALIDADE mensalidade
WHERE mensalidade.ADP_ID = @adesao AND mensalidade.MEN_PERIODO_INICIO = @inicio
  AND mensalidade.MEN_SITUACAO = 'PAGA'
  AND NOT EXISTS (SELECT 1 FROM PAG_PAGAMENTO WHERE MEN_ID = mensalidade.MEN_ID);

-- Especialidade, horário e vínculo do profissional de exemplo.
SET @profissional = (SELECT PRF_ID FROM PRF_PROFISSIONAL WHERE PRF_CREF = '012345-G/PE' AND PRF_SITUACAO = 'ATIVO');
INSERT INTO PES_PROFISSIONAL_ESPECIALIDADE (PRF_ID, PES_CHAVE, PES_NOME)
SELECT @profissional, 'treinamento de força', 'Treinamento de força'
WHERE @profissional IS NOT NULL AND NOT EXISTS (SELECT 1 FROM PES_PROFISSIONAL_ESPECIALIDADE WHERE PRF_ID = @profissional);

INSERT INTO DSP_DISPONIBILIDADE_PROFISSIONAL (PRF_ID, DSP_DIA, DSP_INICIO, DSP_FIM)
SELECT @profissional, 'MONDAY', TIME '08:00', TIME '12:00'
WHERE @profissional IS NOT NULL AND NOT EXISTS (SELECT 1 FROM DSP_DISPONIBILIDADE_PROFISSIONAL WHERE PRF_ID = @profissional);

INSERT INTO VPA_VINCULO_PERSONAL_ALUNO (PRF_ID, ALU_ID, VPA_INICIO, VPA_SITUACAO)
SELECT @profissional, @aluno, @inicio, 'ATIVO'
WHERE @profissional IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM VPA_VINCULO_PERSONAL_ALUNO WHERE ALU_ID = @aluno);

-- Grupo com colegas e treino futuro.
SET @academia = (SELECT ACD_ID FROM ALU_ALUNO WHERE ALU_ID = @aluno);
SET @colega = (SELECT ALU_ID FROM ALU_ALUNO WHERE ALU_USUARIO = 'u' AND ACD_ID = @academia);
INSERT INTO GRT_GRUPO_TREINO (GRT_NOME, ALU_ORGANIZADOR, ACD_ID, GRT_CRIACAO)
SELECT 'Treino em equipe · demonstração', @aluno, @academia, @passado
WHERE NOT EXISTS (SELECT 1 FROM GRT_GRUPO_TREINO WHERE ALU_ORGANIZADOR = @aluno);
SET @grupo = (SELECT MIN(GRT_ID) FROM GRT_GRUPO_TREINO
    WHERE ALU_ORGANIZADOR = @aluno AND GRT_NOME = 'Treino em equipe · demonstração' AND GRT_ENCERRAMENTO IS NULL);

INSERT INTO PGR_PARTICIPACAO_GRUPO (GRT_ID, ALU_ID, ACD_ID, PGR_ENTRADA)
SELECT @grupo, aluno.ALU_ID, @academia, @passado
FROM ALU_ALUNO aluno
WHERE @grupo IS NOT NULL AND aluno.ACD_ID = @academia AND aluno.ALU_USUARIO IN ('aluno','u')
  AND NOT EXISTS (SELECT 1 FROM PGR_PARTICIPACAO_GRUPO WHERE GRT_ID = @grupo AND ALU_ID = aluno.ALU_ID);

INSERT INTO TGR_TREINO_GRUPO (GRT_ID, TGR_INICIO, TGR_PRAZO, TGR_CRIACAO)
SELECT @grupo, DATEADD('DAY', 3, @agora), DATEADD('DAY', 2, @agora), @agora
WHERE @grupo IS NOT NULL AND NOT EXISTS (SELECT 1 FROM TGR_TREINO_GRUPO WHERE GRT_ID = @grupo);

SET @treino_grupo = (SELECT MIN(TGR_ID) FROM TGR_TREINO_GRUPO WHERE GRT_ID = @grupo AND TGR_CRIACAO = @agora);
INSERT INTO CTR_CONVITE_TREINO (TGR_ID, ALU_ID, CTR_SITUACAO, CTR_CRIACAO, CTR_RESPOSTA)
SELECT @treino_grupo, @aluno, 'CONFIRMADO', treino.TGR_CRIACAO, treino.TGR_CRIACAO
FROM TGR_TREINO_GRUPO treino
WHERE treino.TGR_ID = @treino_grupo AND NOT EXISTS (SELECT 1 FROM CTR_CONVITE_TREINO WHERE TGR_ID = @treino_grupo);

INSERT INTO CTR_CONVITE_TREINO (TGR_ID, ALU_ID, CTR_SITUACAO, CTR_CRIACAO)
SELECT @treino_grupo, @colega, 'PENDENTE', treino.TGR_CRIACAO
FROM TGR_TREINO_GRUPO treino
WHERE treino.TGR_ID = @treino_grupo AND @colega IS NOT NULL
  AND EXISTS (SELECT 1 FROM PGR_PARTICIPACAO_GRUPO WHERE GRT_ID = @grupo AND ALU_ID = @colega AND PGR_SAIDA IS NULL)
  AND NOT EXISTS (SELECT 1 FROM CTR_CONVITE_TREINO WHERE TGR_ID = @treino_grupo AND ALU_ID = @colega);

-- Histórico de armário e uma avaliação de professor vinculada a treino concluído.
SET @reserva_inicio = CAST((CAST(CAST(DATEADD('DAY', -3, @hoje) AS DATE) AS VARCHAR) || ' 10:00:00-03:00') AS TIMESTAMP WITH TIME ZONE);
INSERT INTO RSV_RESERVA_ARMARIO (ALU_ID, ARM_CODIGO, RSV_CONFIRMACAO, RSV_INICIO, RSV_TERMINO, RSV_ENCERRAMENTO, RSV_SITUACAO)
SELECT @aluno, 'A-02', DATEADD('MINUTE', -15, @reserva_inicio), @reserva_inicio,
       DATEADD('HOUR', 1, @reserva_inicio), DATEADD('MINUTE', 50, @reserva_inicio), 'ENCERRADA'
WHERE NOT EXISTS (SELECT 1 FROM RSV_RESERVA_ARMARIO WHERE ALU_ID = @aluno);

SET @treino_avaliado = (SELECT MIN(TRN_ID) FROM TRN_TREINO
    WHERE ALU_ID = @aluno AND TRN_DEMONSTRACAO = TRUE AND TRN_CONCLUIDO = TRUE);
INSERT INTO VAT_TREINO_AVALIADO (TRN_ID)
SELECT @treino_avaliado
WHERE @treino_avaliado IS NOT NULL AND NOT EXISTS (SELECT 1 FROM VAT_TREINO_AVALIADO WHERE TRN_ID = @treino_avaliado);

INSERT INTO AVP_AVALIACAO_PROFESSOR (ALU_ID, PRO_ID, TRN_ID, AVP_AUXILIO, AVP_PONTUALIDADE, AVP_POSTURA,
    AVP_DIDATICA, AVP_ADEQUACAO, AVP_COMENTARIO, AVP_CRIACAO, AVP_ATUALIZACAO)
SELECT @aluno, treino.PRO_ID, treino.TRN_ID, 5, 5, 5, 4, 5,
       'Explicou a execução e adaptou a carga durante o treino. Exemplo fictício.', @agora, @agora
FROM TRN_TREINO treino
WHERE treino.TRN_ID = @treino_avaliado
  AND NOT EXISTS (SELECT 1 FROM AVP_AVALIACAO_PROFESSOR WHERE TRN_ID = @treino_avaliado)
  AND EXISTS (SELECT 1 FROM VAT_TREINO_AVALIADO WHERE TRN_ID = @treino_avaliado);

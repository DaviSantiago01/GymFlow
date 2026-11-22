-- Catálogo usado pelas recomendações (classificação de exercícios e grade de aulas coletivas) e exemplos da conta
-- de demonstração "aluno": perfil, restrição física, entradas recentes e histórico de recomendações respondidas.
SET @aluno = (SELECT ALU_ID FROM ALU_ALUNO WHERE ALU_USUARIO = 'aluno');
SET @hoje = CURRENT_DATE;
SET @segunda = DATEADD('DAY', 1 - ISO_DAY_OF_WEEK(@hoje), @hoje);

-- Exercícios que servem de substitutos para os do catálogo atual.
INSERT INTO EXE_EXERCICIO (EXE_NOME, EXE_CATEGORIA, EXE_CARGA_MAXIMA, EXE_INCREMENTO_CARGA)
SELECT novo.nome, novo.categoria, novo.carga, novo.incremento
FROM (VALUES ('Supino inclinado com halteres', 'PEITO', 60.00, CAST(2.00 AS DECIMAL(7,2))),
             ('Supino na máquina', 'PEITO', 150.00, CAST(NULL AS DECIMAL(7,2))),
             ('Remada baixa na polia', 'COSTAS', 150.00, CAST(NULL AS DECIMAL(7,2))),
             ('Cadeira extensora', 'PERNAS', 120.00, CAST(NULL AS DECIMAL(7,2))),
             ('Elevação pélvica', 'PERNAS', 200.00, CAST(5.00 AS DECIMAL(7,2))),
             ('Desenvolvimento com halteres', 'OMBROS', 50.00, CAST(NULL AS DECIMAL(7,2))),
             ('Rosca alternada', 'BRACOS', 40.00, CAST(1.00 AS DECIMAL(7,2))),
             ('Prancha com carga', 'ABDOMEN', 40.00, CAST(NULL AS DECIMAL(7,2))))
    novo(nome, categoria, carga, incremento)
WHERE NOT EXISTS (SELECT 1 FROM EXE_EXERCICIO WHERE EXE_NOME = novo.nome);

INSERT INTO CEX_CLASSIFICACAO_EXERCICIO (EXE_ID, CEX_NIVEL, CEX_OBJETIVOS, CEX_CONTRAINDICACOES)
SELECT exercicio.EXE_ID, classificacao.nivel, classificacao.objetivos, classificacao.contraindicacoes
FROM (VALUES ('Supino reto', 'INTERMEDIARIO', 'HIPERTROFIA', 'OMBRO'),
             ('Crucifixo', 'INICIANTE', 'HIPERTROFIA', 'OMBRO'),
             ('Remada curvada', 'INTERMEDIARIO', 'HIPERTROFIA,CONDICIONAMENTO', 'COLUNA'),
             ('Puxada frontal', 'INICIANTE', 'HIPERTROFIA,CONDICIONAMENTO', ''),
             ('Agachamento livre', 'INTERMEDIARIO', 'HIPERTROFIA,EMAGRECIMENTO,CONDICIONAMENTO', 'JOELHO,COLUNA'),
             ('Leg press', 'INICIANTE', 'HIPERTROFIA,EMAGRECIMENTO', 'JOELHO'),
             ('Desenvolvimento', 'INTERMEDIARIO', 'HIPERTROFIA', 'OMBRO'),
             ('Rosca direta', 'INICIANTE', 'HIPERTROFIA', ''),
             ('Tríceps na polia', 'INICIANTE', 'HIPERTROFIA,CONDICIONAMENTO', ''),
             ('Abdominal na máquina', 'INICIANTE', 'HIPERTROFIA,EMAGRECIMENTO,CONDICIONAMENTO', 'COLUNA'),
             ('Supino inclinado com halteres', 'INTERMEDIARIO', 'HIPERTROFIA', ''),
             ('Supino na máquina', 'INICIANTE', 'HIPERTROFIA,CONDICIONAMENTO', ''),
             ('Remada baixa na polia', 'INICIANTE', 'HIPERTROFIA,CONDICIONAMENTO', ''),
             ('Cadeira extensora', 'INICIANTE', 'HIPERTROFIA', 'JOELHO'),
             ('Elevação pélvica', 'INICIANTE', 'HIPERTROFIA,EMAGRECIMENTO', ''),
             ('Desenvolvimento com halteres', 'INTERMEDIARIO', 'HIPERTROFIA', 'OMBRO'),
             ('Rosca alternada', 'INICIANTE', 'HIPERTROFIA', ''),
             ('Prancha com carga', 'INTERMEDIARIO', 'HIPERTROFIA,EMAGRECIMENTO,CONDICIONAMENTO', 'COLUNA'))
    classificacao(nome, nivel, objetivos, contraindicacoes)
JOIN EXE_EXERCICIO exercicio ON exercicio.EXE_NOME = classificacao.nome
WHERE NOT EXISTS (SELECT 1 FROM CEX_CLASSIFICACAO_EXERCICIO WHERE EXE_ID = exercicio.EXE_ID);

-- Grade semanal de aulas coletivas.
INSERT INTO AUC_AULA_COLETIVA (AUC_NOME, AUC_MODALIDADE, AUC_INSTRUTOR, AUC_SALA, AUC_DIA, AUC_INICIO, AUC_FIM,
    AUC_CAPACIDADE, AUC_NIVEL, AUC_IMPACTO, AUC_OBJETIVOS, AUC_CONTRAINDICACOES)
SELECT aula.nome, aula.modalidade, aula.instrutor, aula.sala, aula.dia, aula.inicio, aula.fim,
       aula.capacidade, aula.nivel, aula.impacto, aula.objetivos, aula.contraindicacoes
FROM (VALUES ('Pilates solo', 'Pilates', 'Paula Nunes', 'Sala 2', 'QUARTA', '19:00:00', '19:50:00', 15, 'INTERMEDIARIO', 'BAIXO', 'HIPERTROFIA,EMAGRECIMENTO,CONDICIONAMENTO', ''),
             ('Pilates solo', 'Pilates', 'Paula Nunes', 'Sala 2', 'TERCA', '18:00:00', '18:50:00', 15, 'INTERMEDIARIO', 'BAIXO', 'HIPERTROFIA,EMAGRECIMENTO,CONDICIONAMENTO', ''),
             ('Alongamento e mobilidade', 'Alongamento', 'Caio Moreira', 'Sala 1', 'SEGUNDA', '20:00:00', '20:45:00', 20, 'INICIANTE', 'BAIXO', 'HIPERTROFIA,EMAGRECIMENTO,CONDICIONAMENTO', ''),
             ('Alongamento e mobilidade', 'Alongamento', 'Caio Moreira', 'Sala 1', 'SEXTA', '20:00:00', '20:45:00', 20, 'INICIANTE', 'BAIXO', 'HIPERTROFIA,EMAGRECIMENTO,CONDICIONAMENTO', ''),
             ('Core e abdômen', 'Funcional', 'Paula Nunes', 'Sala 1', 'SEXTA', '18:30:00', '19:15:00', 20, 'INTERMEDIARIO', 'MODERADO', 'HIPERTROFIA,CONDICIONAMENTO', ''),
             ('Spinning', 'Ciclismo indoor', 'Diego Martins', 'Sala de bikes', 'SEGUNDA', '19:00:00', '19:45:00', 20, 'INICIANTE', 'MODERADO', 'HIPERTROFIA,EMAGRECIMENTO,CONDICIONAMENTO', ''),
             ('Jump', 'Jump', 'Diego Martins', 'Sala 1', 'QUARTA', '18:00:00', '18:45:00', 20, 'INICIANTE', 'ALTO', 'HIPERTROFIA,EMAGRECIMENTO,CONDICIONAMENTO', 'JOELHO,TORNOZELO'),
             ('Funcional', 'Funcional', 'Caio Moreira', 'Área externa', 'TERCA', '07:00:00', '07:50:00', 20, 'INTERMEDIARIO', 'MODERADO', 'HIPERTROFIA,EMAGRECIMENTO,CONDICIONAMENTO', ''),
             ('Levantamento olímpico', 'Força', 'Diego Martins', 'Área de força', 'SEXTA', '18:00:00', '19:00:00', 10, 'AVANCADO', 'ALTO', 'HIPERTROFIA', 'OMBRO,PUNHO'),
             ('Yoga', 'Yoga', 'Paula Nunes', 'Sala 2', 'SEXTA', '20:00:00', '21:00:00', 20, 'INICIANTE', 'BAIXO', 'HIPERTROFIA,EMAGRECIMENTO,CONDICIONAMENTO', ''))
    aula(nome, modalidade, instrutor, sala, dia, inicio, fim, capacidade, nivel, impacto, objetivos, contraindicacoes)
WHERE NOT EXISTS (SELECT 1 FROM AUC_AULA_COLETIVA);

SET @yoga = (SELECT MIN(AUC_ID) FROM AUC_AULA_COLETIVA WHERE AUC_NOME = 'Yoga');
SET @spinning = (SELECT MIN(AUC_ID) FROM AUC_AULA_COLETIVA WHERE AUC_NOME = 'Spinning');
SET @core = (SELECT MIN(AUC_ID) FROM AUC_AULA_COLETIVA WHERE AUC_NOME = 'Core e abdômen');
SET @anual = (SELECT MIN(PLA_ID) FROM PLA_PLANO WHERE PLA_NOME = 'Anual' AND PLA_SITUACAO = 'ATIVO');
SET @trimestral = (SELECT MIN(PLA_ID) FROM PLA_PLANO WHERE PLA_NOME = 'Trimestral' AND PLA_SITUACAO = 'ATIVO');

-- Perfil da conta "aluno", com restrição no joelho registrada na primeira avaliação física.
INSERT INTO PFA_PERFIL_ALUNO (ALU_ID, PFA_OBJETIVO, PFA_NIVEL, PFA_DIAS, PFA_HORA_INICIO, PFA_HORA_FIM,
    PFA_CATEGORIAS_DESATIVADAS, PFA_ATUALIZACAO)
SELECT @aluno, 'HIPERTROFIA', 'INTERMEDIARIO', 'SEGUNDA,QUARTA,SEXTA', '18:00:00', '21:00:00',
       'PLANOS_SERVICOS', CURRENT_TIMESTAMP
WHERE @aluno IS NOT NULL AND NOT EXISTS (SELECT 1 FROM PFA_PERFIL_ALUNO WHERE ALU_ID = @aluno);

INSERT INTO RFI_RESTRICAO_FISICA (ALU_ID, RFI_INDICE, RFI_REGIAO, RFI_DESCRICAO, RFI_REGISTRO)
SELECT @aluno, 0, 'JOELHO', 'Dor ao agachar. Evitar impacto e flexão profunda do joelho.', DATEADD('DAY', -42, @hoje)
WHERE EXISTS (SELECT 1 FROM PFA_PERFIL_ALUNO WHERE ALU_ID = @aluno)
  AND NOT EXISTS (SELECT 1 FROM RFI_RESTRICAO_FISICA WHERE ALU_ID = @aluno);

INSERT INTO IAC_INSCRICAO_AULA (AUC_ID, ALU_ID, IAC_DATA)
SELECT @yoga, @aluno, CAST(CAST(DATEADD('DAY', -120, @hoje) AS VARCHAR) || ' 12:00:00-03:00' AS TIMESTAMP WITH TIME ZONE)
WHERE @aluno IS NOT NULL AND @yoga IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM IAC_INSCRICAO_AULA WHERE AUC_ID = @yoga AND ALU_ID = @aluno);

-- Entradas das últimas semanas: o aluno vinha duas ou três vezes por semana e parou de vir nas três últimas.
INSERT INTO RAC_REGISTRO_ACESSO (ALU_ID, RAC_ENTRADA, RAC_SAIDA)
SELECT @aluno,
       CAST(CAST(DATEADD('MINUTE', entrada.minuto, DATEADD('DAY', entrada.dia - 7 * entrada.semana, CAST(@segunda AS TIMESTAMP)))
            AS VARCHAR) || '-03:00' AS TIMESTAMP WITH TIME ZONE),
       CAST(CAST(DATEADD('MINUTE', entrada.minuto + 70, DATEADD('DAY', entrada.dia - 7 * entrada.semana, CAST(@segunda AS TIMESTAMP)))
            AS VARCHAR) || '-03:00' AS TIMESTAMP WITH TIME ZONE)
FROM (VALUES (7, 0, 1115), (7, 3, 1120), (6, 0, 1110), (6, 2, 1125), (6, 3, 1115), (5, 0, 1110), (5, 2, 1120), (5, 4, 1105),
             (4, 0, 1115), (4, 3, 1130)) entrada(semana, dia, minuto)
WHERE @aluno IS NOT NULL;

-- Recomendações já respondidas ou expiradas, que formam o histórico e a taxa de aceitação por categoria.
INSERT INTO REC_RECOMENDACAO (ALU_ID, REC_CATEGORIA, REC_CRITERIO, REC_ITEM, REC_TITULO, REC_DESCRICAO, REC_JUSTIFICATIVA,
    AUC_ID, PLA_ID, REC_GERACAO, REC_VALIDADE, REC_SITUACAO, REC_RESPOSTA, REC_RESPONDENTE, REC_MOTIVO)
SELECT @aluno, historico.categoria, historico.criterio,
       CASE historico.item WHEN 'YOGA' THEN 'AULA:' || @yoga WHEN 'SPINNING' THEN 'AULA:' || @spinning
            WHEN 'CORE' THEN 'AULA:' || @core WHEN 'ANUAL' THEN 'PLANO:' || @anual
            WHEN 'TRIMESTRAL' THEN 'PLANO:' || @trimestral ELSE historico.item END,
       historico.titulo, historico.descricao, historico.justificativa,
       CASE historico.item WHEN 'YOGA' THEN @yoga WHEN 'SPINNING' THEN @spinning WHEN 'CORE' THEN @core END,
       CASE historico.item WHEN 'ANUAL' THEN @anual WHEN 'TRIMESTRAL' THEN @trimestral END,
       CAST(CAST(DATEADD('DAY', -historico.dias, @hoje) AS VARCHAR) || ' 08:00:00-03:00' AS TIMESTAMP WITH TIME ZONE),
       DATEADD('DAY', 14 - historico.dias, @hoje), historico.situacao,
       CASE WHEN historico.situacao = 'EXPIRADA' THEN NULL
            ELSE CAST(CAST(DATEADD('DAY', 1 - historico.dias, @hoje) AS VARCHAR) || ' 19:00:00-03:00' AS TIMESTAMP WITH TIME ZONE) END,
       CASE WHEN historico.situacao = 'EXPIRADA' THEN NULL ELSE 'Aluno' END,
       historico.motivo
FROM (VALUES
        ('AULAS_COLETIVAS', 'AULA_COMPATIVEL', 'YOGA', 'Yoga às sextas, 20h',
         'Aula de baixo impacto, compatível com a restrição no seu joelho e sua disponibilidade. Restam 12 vagas.',
         'Aula de nível iniciante adequada ao objetivo de hipertrofia, sem contraindicação para as suas restrições, dentro da sua disponibilidade e com 12 vagas',
         121, 'ACEITA', CAST(NULL AS VARCHAR(250))),
        ('AULAS_COLETIVAS', 'AULA_COMPATIVEL', 'CORE', 'Core e abdômen às sextas, 18h30',
         'Aula de impacto moderado, compatível com a restrição no seu joelho e sua disponibilidade. Restam 5 vagas.',
         'Aula de nível intermediário adequada ao objetivo de hipertrofia, sem contraindicação para as suas restrições, dentro da sua disponibilidade e com 5 vagas',
         90, 'EXPIRADA', CAST(NULL AS VARCHAR(250))),
        ('PLANOS_SERVICOS', 'OFERTA_PLANO', 'ANUAL', 'Plano anual com 20% de desconto',
         'No plano anual, a mensalidade equivalente cai de R$ 99,90 para R$ 79,99.',
         'Aluno há 1 mês no plano mensal, e o plano anual custa 20% menos por mês', 82, 'RECUSADA', 'Não tenho interesse'),
        ('AULAS_COLETIVAS', 'AULA_COMPATIVEL', 'SPINNING', 'Spinning às segundas, 19h',
         'Aula de impacto moderado, compatível com a restrição no seu joelho e sua disponibilidade. Restam 3 vagas.',
         'Aula de nível iniciante adequada ao objetivo de hipertrofia, sem contraindicação para as suas restrições, dentro da sua disponibilidade e com 3 vagas',
         75, 'RECUSADA', 'Esse horário não funciona para mim'),
        ('ACOMPANHAMENTO_FISICO', 'REAVALIACAO_FISICA', 'AVALIACAO', 'Agende sua avaliação física',
         'Você ainda não tem avaliação física registrada. Ela orienta seu treino e o acompanhamento das suas metas.',
         'Nenhuma avaliação física registrada', 50, 'ACEITA', CAST(NULL AS VARCHAR(250))),
        ('PLANOS_SERVICOS', 'OFERTA_PLANO', 'TRIMESTRAL', 'Plano trimestral com 10% de desconto',
         'No plano trimestral, a mensalidade equivalente cai de R$ 99,90 para R$ 89,97.',
         'Aluno há 1 mês no plano mensal, e o plano trimestral custa 10% menos por mês', 45, 'EXPIRADA', CAST(NULL AS VARCHAR(250))))
    historico(categoria, criterio, item, titulo, descricao, justificativa, dias, situacao, motivo)
WHERE @aluno IS NOT NULL AND NOT EXISTS (SELECT 1 FROM REC_RECOMENDACAO WHERE ALU_ID = @aluno);

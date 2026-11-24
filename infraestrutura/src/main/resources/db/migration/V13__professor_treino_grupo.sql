-- Treinos de grupo passam a ter professor responsável. Treinos anteriores a esta versão ficam sem professor.
ALTER TABLE TGR_TREINO_GRUPO ADD PRF_ID BIGINT REFERENCES PRF_PROFISSIONAL(PRF_ID);
-- Consulta de conflitos do professor e de vagas por horário.
CREATE INDEX IX_TGR_PROFESSOR ON TGR_TREINO_GRUPO(PRF_ID,TGR_INICIO);
CREATE INDEX IX_TGR_HORARIO ON TGR_TREINO_GRUPO(TGR_INICIO);

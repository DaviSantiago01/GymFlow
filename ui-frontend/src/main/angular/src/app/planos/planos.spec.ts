import {
  FORMAS_PAGAMENTO,
  ROTULOS_SITUACAO_MENSALIDADE,
  ROTULOS_SITUACAO_PARCELA,
  duracao,
  moeda,
  recorrencia,
} from './planos.service';

describe('Recorrência da adesão', () => {
  it('descreve a adesão sem recorrência como contínua', () =>
    expect(recorrencia(null, 1)).toBe('Contínua, até o cancelamento'));
  it('mostra cobranças e duração total do plano mensal', () =>
    expect(recorrencia(5, 1)).toBe('5 cobranças (5 meses)'));
  it('multiplica a duração pelas cobranças do plano trimestral', () =>
    expect(recorrencia(2, 3)).toBe('2 cobranças (6 meses)'));
  it('usa o singular para uma única cobrança', () => expect(recorrencia(1, 1)).toBe('1 cobrança (1 mês)'));
  it('rotula as parcelas que ainda vão vencer', () => expect(ROTULOS_SITUACAO_PARCELA.PREVISTA).toBe('A vencer'));
});
import { cpfFormatado, horario, horas } from '../profissionais/profissionais.service';

describe('Planos e mensalidades', () => {
  it('formata valores em reais', () => expect(moeda(102.23).replace(/\s/g, ' ')).toBe('R$ 102,23'));
  it('indica valor ausente com travessão', () => expect(moeda(null)).toBe('—'));
  it('descreve a duração do plano', () => {
    expect(duracao(1)).toBe('1 mês');
    expect(duracao(3)).toBe('3 meses');
  });
  it('rotula a situação calculada de atraso', () =>
    expect(ROTULOS_SITUACAO_MENSALIDADE.ATRASADA).toBe('Em atraso'));
  it('oferece todas as formas de pagamento aceitas pelo backend', () =>
    expect(FORMAS_PAGAMENTO).toEqual(['DINHEIRO', 'PIX', 'CARTAO_DEBITO', 'CARTAO_CREDITO', 'BOLETO']));
});

describe('Profissionais', () => {
  it('formata o CPF guardado só com dígitos', () => expect(cpfFormatado('52998224725')).toBe('529.982.247-25'));
  it('mostra horários sem segundos', () => expect(horario('08:00:00')).toBe('08:00'));
  it('descreve a carga semanal em horas e minutos', () => {
    expect(horas(2640)).toBe('44 h');
    expect(horas(525)).toBe('8 h 45 min');
  });
});

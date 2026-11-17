import { FORMAS_PAGAMENTO, ROTULOS_SITUACAO_MENSALIDADE, duracao, moeda } from './planos.service';
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

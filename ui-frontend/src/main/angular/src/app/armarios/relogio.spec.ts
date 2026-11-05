import { segundosRestantes, formatarDuracao } from './relogio';
describe('Relógio da reserva', () => {
  it('conta três horas a partir do horário do servidor', () =>
    expect(segundosRestantes('2026-11-04T15:00:00Z', '2026-11-04T12:00:00Z', 100, 100)).toBe(
      10800,
    ));
  it('desconta tempo monotônico mesmo com relógio local diferente', () => {
    vi.spyOn(Date, 'now').mockReturnValue(0);
    expect(segundosRestantes('2026-11-04T15:00:00Z', '2026-11-04T12:00:00Z', 100, 60100)).toBe(
      10740,
    );
    vi.restoreAllMocks();
  });
  it('não exibe tempo negativo após vencimento', () =>
    expect(segundosRestantes('2026-11-04T15:00:00Z', '2026-11-04T15:01:00Z', 0, 10)).toBe(0));
  it('formata horas, minutos e segundos', () => expect(formatarDuracao(7986)).toBe('02:13:06'));
});

import { describe, it, expect } from 'vitest';
import {
  validarNome,
  validarAgenda,
  instanteBrasil,
  podeResponder,
  podeCancelarPresenca,
  TreinoResumo,
} from './grupos.service';
const inicio = Date.parse('2026-11-06T16:00:00Z'),
  prazo = Date.parse('2026-11-06T14:00:00Z');
const t = {
  membro: true,
  cancelamento: null,
  inicio: new Date(inicio).toISOString(),
  prazo: new Date(prazo).toISOString(),
  meuConvite: { situacao: 'PENDENTE' },
} as TreinoResumo;
describe('Regras da interface de grupos', () => {
  it('normaliza espaços e valida o tamanho do nome', () => {
    expect(validarNome('   ')).not.toBe('');
    expect(validarNome('a'.repeat(61))).not.toBe('');
    expect(validarNome(' ' + 'a'.repeat(60) + ' ')).toBe('');
  });
  it('converte data de Brasília sem depender do fuso do navegador', () =>
    expect(instanteBrasil('2026-11-06', '13:00')).toBe('2026-11-06T16:00:00.000Z'));
  it('rejeita prazos inválidos e exige futuro', () => {
    expect(validarAgenda(t.inicio, t.prazo, prazo - 1)).toBe('');
    expect(validarAgenda(t.inicio, t.prazo, prazo)).not.toBe('');
    expect(validarAgenda(t.inicio, t.inicio, prazo - 1)).not.toBe('');
    expect(validarAgenda('', '', prazo)).not.toBe('');
  });
  it('encerra confirmação no instante do prazo', () => {
    expect(podeResponder(t, prazo - 1)).toBe(true);
    expect(podeResponder(t, prazo)).toBe(false);
  });
  it('bloqueia resposta de ex-membro ou em treino cancelado', () => {
    expect(podeResponder({ ...t, membro: false }, prazo - 1)).toBe(false);
    expect(podeResponder({ ...t, cancelamento: '2026-11-06T12:00:00Z' }, prazo - 1)).toBe(false);
  });
  it('permite cancelar exatamente uma hora antes e bloqueia depois', () => {
    const confirmado = { ...t, meuConvite: { ...t.meuConvite!, situacao: 'CONFIRMADO' } };
    expect(podeCancelarPresenca(confirmado, inicio - 3600000)).toBe(true);
    expect(podeCancelarPresenca(confirmado, inicio - 3599999)).toBe(false);
    expect(podeCancelarPresenca(t, inicio - 7200000)).toBe(false);
  });
});

import { describe, it, expect } from 'vitest';
import { escolhasVazias, validarAvaliacao } from './avaliacoes.service';
describe('Formulário de avaliações', () => {
  it('distingue zero de critérios ainda não preenchidos', () => {
    expect(Object.keys(validarAvaliacao(escolhasVazias(), ''))).toHaveLength(5);
    expect(
      validarAvaliacao({ auxilio: 0, pontualidade: 0, postura: 0, didatica: 0, adequacao: 0 }, ''),
    ).toEqual({});
  });
  it('rejeita decimal e notas fora do intervalo', () => {
    const erros = validarAvaliacao(
      { auxilio: -1, pontualidade: 6, postura: 2.5, didatica: 5, adequacao: 0 },
      '',
    );
    expect(Object.keys(erros)).toEqual(['auxilio', 'pontualidade', 'postura']);
  });
  it('aceita comentário opcional até 500 caracteres e rejeita 501', () => {
    const notas = { auxilio: 3, pontualidade: 3, postura: 3, didatica: 3, adequacao: 3 };
    for (const comentario of ['', 'a'.repeat(500)])
      expect(validarAvaliacao(notas, comentario)).toEqual({});
    expect(validarAvaliacao(notas, 'a'.repeat(501))['comentario']).toBeTruthy();
  });
});

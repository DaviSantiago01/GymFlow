const FUSO = 'America/Sao_Paulo';

export function numero(valor: number | null | undefined): string {
  if (valor === null || valor === undefined) return '—';
  return valor.toLocaleString('pt-BR', { maximumFractionDigits: 2 });
}

export function dataHora(valor: string): string {
  return new Date(valor).toLocaleString('pt-BR', {
    timeZone: FUSO,
    dateStyle: 'short',
    timeStyle: 'short',
  });
}

export function diaMes(valor: string): string {
  return new Date(valor).toLocaleDateString('pt-BR', {
    timeZone: FUSO,
    day: '2-digit',
    month: '2-digit',
  });
}

/** Datas sem horário chegam como AAAA-MM-DD e não sofrem conversão de fuso. */
export function data(valor: string): string {
  const [ano, mes, dia] = valor.split('-');
  return `${dia}/${mes}/${ano}`;
}

export function mensagemDeErro(falha: { error?: { mensagem?: string } }, padrao: string): string {
  return falha.error?.mensagem ?? padrao;
}

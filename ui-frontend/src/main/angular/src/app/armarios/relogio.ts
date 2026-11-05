/** Date.now não participa da contagem: uma alteração no relógio local não prolonga a reserva. */
export function segundosRestantes(
  termino: string,
  horarioServidor: string,
  recebidoEm: number,
  agoraMonotonico: number,
): number {
  return Math.max(
    0,
    Math.ceil(
      (Date.parse(termino) -
        Date.parse(horarioServidor) -
        Math.max(0, agoraMonotonico - recebidoEm)) /
        1000,
    ),
  );
}
export function formatarDuracao(segundos: number): string {
  return [Math.floor(segundos / 3600), Math.floor(segundos / 60) % 60, segundos % 60]
    .map((n) => String(n).padStart(2, '0'))
    .join(':');
}

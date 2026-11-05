import { expect, request } from '@playwright/test';

export default async function preparar() {
  const cliente = await request.newContext({ baseURL: 'http://127.0.0.1:4200' });
  try {
    await expect.poll(async () => {
      try {
        return (await cliente.get('/backend/cadastro', { timeout: 2000 })).status();
      } catch {
        return 0;
      }
    }, { timeout: 30000, intervals: [500], message: 'Aguardar frontend e backend prontos' }).toBe(200);
  } finally {
    await cliente.dispose();
  }
}

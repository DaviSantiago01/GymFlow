import { expect, FullConfig, request } from '@playwright/test';

export default async function preparar(config: FullConfig) {
  const cliente = await request.newContext({ baseURL: config.projects[0]?.use.baseURL });
  try {
    await expect.poll(async () => {
      try {
        return (await cliente.get('/backend/core/aluno/cadastro', { timeout: 2000 })).status();
      } catch {
        return 0;
      }
    }, { timeout: 30000, intervals: [500], message: 'Aguardar frontend e backend prontos' }).toBe(200);
  } finally {
    await cliente.dispose();
  }
}

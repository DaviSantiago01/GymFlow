import { expect, Page } from '@playwright/test';

export const senhaTeste = 'SenhaAgenda123!';
export async function entrar(page: Page) {
  const usuario = `agenda_${Date.now()}_${Math.random().toString(36).slice(2, 7)}`;
  await page.goto('/cadastro');
  await page.request.get('/backend/cadastro');
  const token = (await page.context().cookies()).find((c) => c.name === 'XSRF-TOKEN')!.value;
  const cadastro = await page.request.post('/backend/cadastro', {
    headers: { 'X-XSRF-TOKEN': decodeURIComponent(token) },
    data: {
      nome: 'Aluno de Teste Agenda',
      usuario,
      senha: senhaTeste,
      confirmacaoSenha: senhaTeste,
    },
  });
  expect(cadastro.status()).toBe(201);
  await page.goto('/login');
  await page.getByLabel('Usuário', { exact: true }).fill(usuario);
  await page.getByLabel('Senha', { exact: true }).fill(senhaTeste);
  await page.getByRole('button', { name: 'Entrar', exact: true }).click();
  await expect(page.getByRole('heading', { name: `Boas-vindas, ${usuario}.` })).toBeVisible();
  const abrir = page.getByRole('button', { name: 'Abrir menu' });
  if (await abrir.isVisible()) await abrir.click();
  const menu = page.getByRole('navigation', { name: 'Menu do aluno' });
  await expect(menu.getByRole('button', { name: /Marcar aula/ })).toBeDisabled();
  await expect(menu.getByRole('button', { name: /Grupos de treino/ })).toBeDisabled();
  await menu.getByRole('link', { name: 'Reservar armário', exact: true }).click();
  await expect(page.getByRole('heading', { name: 'Armários', exact: true })).toBeVisible();
  if (await abrir.isVisible()) await expect(menu).toBeHidden();
}
export async function escolherData(page: Page, dias = 1) {
  const campo = page.getByLabel('Data da reserva');
  const hoje = (await campo.getAttribute('min'))!;
  const data = new Date(`${hoje}T12:00:00Z`);
  data.setUTCDate(data.getUTCDate() + dias);
  const valor = data.toISOString().slice(0, 10);
  const resposta = page.waitForResponse((r) => r.url().includes(`/horarios?data=${valor}`));
  await campo.fill(valor);
  expect((await resposta).ok()).toBeTruthy();
  await expect(
    page.getByRole('group', { name: 'Horários disponíveis' }).getByRole('button'),
  ).toHaveCount(16);
  return valor;
}
export function horario(page: Page, hora: number) {
  return page.getByRole('group', { name: 'Horários disponíveis' }).getByRole('button', {
    name: new RegExp(`^${String(hora).padStart(2, '0')}:00`),
  });
}
export async function cancelarPendente(page: Page) {
  const resposta = await page.request.get('/backend/core/reserva-armario/ativa');
  const { dados } = await resposta.json();
  if (!dados) return;
  const token = (await page.context().cookies()).find((c) => c.name === 'XSRF-TOKEN')!.value;
  expect(
    (
      await page.request.post(`/backend/core/reserva-armario/${dados.identificador}/cancelar`, {
        headers: { 'X-XSRF-TOKEN': decodeURIComponent(token) },
      })
    ).ok(),
  ).toBeTruthy();
}

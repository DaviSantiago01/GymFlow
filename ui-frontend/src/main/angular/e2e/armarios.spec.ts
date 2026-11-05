import { test, expect } from '@playwright/test';
import { entrar, escolherData, horario, cancelarPendente } from './agenda-ajudantes';

test('escolher data, reservar três horas, consultar agendada, cancelar e ver histórico', async ({
  page,
}) => {
  const erros: string[] = [];
  page.on('pageerror', (e) => erros.push(e.message));
  page.on('dialog', (d) => d.accept());
  await entrar(page);
  await page.getByRole('textbox', { name: 'Buscar armário' }).fill('A-01');
  await expect(page.locator('.cartao-armario')).toHaveCount(1);
  await page.locator('.cartao-armario').click();
  await expect(
    page.getByRole('button', { name: 'Reservar armário A-01', exact: true }),
  ).toBeDisabled();
  const data = await escolherData(page);
  await horario(page, 6).click();
  await page
    .getByRole('group', { name: 'Duração da reserva' })
    .getByRole('button', { name: '3 horas', exact: true })
    .click();
  await page.evaluate(() => window.scrollTo(0, 0));
  await page.screenshot({ path: '../../../../docs/validacao/10-agenda.png', fullPage: true });
  await page.getByRole('button', { name: 'Reservar armário A-01', exact: true }).click();
  await expect(page.getByRole('heading', { name: 'Reserva confirmada!' })).toBeVisible();
  const { dados } = await (await page.request.get('/backend/core/reserva-armario/ativa')).json();
  expect(dados.inicio).toBe(`${data}T09:00:00Z`);
  expect(dados.termino).toBe(`${data}T12:00:00Z`);
  await page.getByRole('link', { name: 'Acessar minha reserva ativa →' }).click();
  await expect(page.locator('.resumo-reserva')).toContainText('Agendada');
  await expect(page.locator('.tempo')).toContainText('COMEÇA EM');
  await expect(page.getByRole('button', { name: 'Encerrar uso do armário' })).toBeDisabled();
  await page.reload();
  await expect(page.locator('.resumo-reserva')).toContainText('Agendada');
  await page.screenshot({
    path: '../../../../docs/validacao/11-reserva-agendada.png',
    fullPage: true,
  });
  await page.getByRole('button', { name: 'Cancelar reserva', exact: true }).click();
  await expect(page.getByRole('status')).toContainText('Reserva cancelada');
  await page.getByRole('link', { name: 'Histórico ↗', exact: true }).click();
  await expect(page.locator('.item-historico').first()).toContainText('Cancelada');
  expect(erros).toEqual([]);
});

test('duas sessões: tela desatualizada rejeita conflito e permite intervalo adjacente', async ({
  page,
  browser,
}) => {
  await entrar(page);
  const outra = await browser.newContext({ baseURL: 'http://127.0.0.1:4200' });
  const visitante = await outra.newPage();
  try {
    await entrar(visitante);
    for (const tela of [page, visitante]) {
      await tela.goto('/core/armario/A-02/visualizacao');
      await escolherData(tela, 2);
      await horario(tela, 6).click();
      await tela.getByRole('button', { name: '2 horas', exact: true }).click();
    }
    await page.getByRole('button', { name: 'Reservar armário A-02', exact: true }).click();
    await expect(page.getByRole('heading', { name: 'Reserva confirmada!' })).toBeVisible();
    const id = page.url().split('/').at(-2)!;
    await visitante.getByRole('button', { name: 'Reservar armário A-02', exact: true }).click();
    await expect(visitante.getByRole('alert')).toContainText('não está mais disponível');
    await expect(horario(visitante, 6)).toBeDisabled();
    await expect(horario(visitante, 7)).toBeDisabled();
    await horario(visitante, 8).click();
    await visitante.getByRole('button', { name: 'Reservar armário A-02', exact: true }).click();
    await expect(visitante.getByRole('heading', { name: 'Reserva confirmada!' })).toBeVisible();
    const token = (await outra.cookies()).find((c) => c.name === 'XSRF-TOKEN')!.value;
    const proibida = await visitante.request.post(`/backend/core/reserva-armario/${id}/cancelar`, {
      headers: { 'X-XSRF-TOKEN': decodeURIComponent(token) },
    });
    expect(proibida.status()).toBe(403);
  } finally {
    await cancelarPendente(page);
    await cancelarPendente(visitante);
    await outra.close();
  }
});

test('fechamento às 22h limita duração e mudança de data limpa seleção', async ({ page }) => {
  await entrar(page);
  await page.goto('/core/armario/A-03/visualizacao');
  await escolherData(page, 3);
  await horario(page, 20).click();
  await expect(page.getByRole('button', { name: '2 horas', exact: true })).toBeEnabled();
  await expect(page.getByRole('button', { name: '3 horas', exact: true })).toBeDisabled();
  await horario(page, 21).click();
  await expect(page.getByRole('button', { name: '1 hora', exact: true })).toBeEnabled();
  await expect(page.getByRole('button', { name: '2 horas', exact: true })).toBeDisabled();
  await page.getByRole('button', { name: 'Reservar armário A-03', exact: true }).click();
  await expect(page.getByRole('heading', { name: 'Reserva confirmada!' })).toBeVisible();
  const { dados } = await (await page.request.get('/backend/core/reserva-armario/ativa')).json();
  expect(Date.parse(dados.termino) - Date.parse(dados.inicio)).toBe(3600000);
  await cancelarPendente(page);
  await page.goto('/core/armario/A-03/visualizacao');
  await escolherData(page, 3);
  await horario(page, 6).click();
  await escolherData(page, 4);
  await expect(page.getByRole('group', { name: 'Duração da reserva' })).toBeHidden();
  await expect(
    page.getByRole('button', { name: 'Reservar armário A-03', exact: true }),
  ).toBeDisabled();
});

test('agenda e menu no celular sem rolagem horizontal', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 });
  await entrar(page);
  await page.getByRole('button', { name: 'Abrir menu' }).click();
  await page.getByRole('button', { name: 'Fechar menu' }).click();
  await page.goto('/core/armario/A-04/visualizacao');
  await escolherData(page, 4);
  await horario(page, 8).click();
  expect(
    await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth),
  ).toBeTruthy();
  await page.evaluate(() => window.scrollTo(0, 0));
  await page.screenshot({
    path: '../../../../docs/validacao/12-agenda-celular.png',
    fullPage: true,
  });
});

test('troca rápida de datas descarta a resposta antiga', async ({ page }) => {
  await entrar(page);
  await page.goto('/core/armario/A-05/visualizacao');
  const hoje = (await page.getByLabel('Data da reserva').getAttribute('min'))!;
  const dia = (dias: number) => {
    const valor = new Date(`${hoje}T12:00:00Z`);
    valor.setUTCDate(valor.getUTCDate() + dias);
    return valor.toISOString().slice(0, 10);
  };
  const antiga = dia(6);
  const nova = dia(7);
  let liberar!: () => void;
  const aguardando = new Promise<void>((resolve) => {
    liberar = resolve;
  });
  await page.route(`**/horarios?data=${antiga}`, async (route) => {
    const resposta = await route.fetch();
    await aguardando;
    await route.fulfill({ response: resposta }).catch(() => {});
  });
  const solicitada = page.waitForRequest((r) => r.url().includes(`horarios?data=${antiga}`));
  await page.getByLabel('Data da reserva').fill(antiga);
  await solicitada;
  await escolherData(page, 7);
  liberar();
  await horario(page, 6).click();
  const requisicao = page.waitForRequest(
    (r) => r.method() === 'POST' && r.url().endsWith('/A-05/reservar'),
  );
  await page.getByRole('button', { name: 'Reservar armário A-05', exact: true }).click();
  expect((await requisicao).postDataJSON().data).toBe(nova);
  await expect(page.getByRole('heading', { name: 'Reserva confirmada!' })).toBeVisible();
  await cancelarPendente(page);
});

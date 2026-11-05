import { test, expect, Page } from '@playwright/test';

async function entrar(page: Page, usuario = 'u') {
  await page.goto('/login');
  await page.getByLabel('Usuário', { exact: true }).fill(usuario);
  await page.getByLabel('Senha', { exact: true }).fill(usuario);
  await page.getByRole('button', { name: 'Entrar', exact: true }).click();
  await expect(page.getByRole('heading', { name: `Boas-vindas, ${usuario}.` })).toBeVisible();
  const abrirMenu = page.getByRole('button', { name: 'Abrir menu' });
  if (await abrirMenu.isVisible()) await abrirMenu.click();
  const menu = page.getByRole('navigation', { name: 'Menu do aluno' });
  await expect(menu.getByRole('button', { name: /Marcar aula/ })).toBeDisabled();
  await expect(menu.getByRole('button', { name: /Grupos de treino/ })).toBeDisabled();
  await menu.getByRole('link', { name: 'Reservar armário', exact: true }).click();
  await expect(page.getByRole('heading', { name: 'Armários', exact: true })).toBeVisible();
  if (await abrirMenu.isVisible()) await expect(menu).toBeHidden();
}
async function limparAtiva(page: Page) {
  const resposta = await page.request.get('/backend/core/reserva-armario/ativa');
  const { dados } = await resposta.json();
  if (dados) {
    const token = (await page.context().cookies()).find((c) => c.name === 'XSRF-TOKEN')!.value;
    const cancelamento = await page.request.post(
      `/backend/core/reserva-armario/${dados.identificador}/cancelar`,
      { headers: { 'X-XSRF-TOKEN': decodeURIComponent(token) } },
    );
    expect(cancelamento.ok()).toBeTruthy();
  }
  await page.reload();
}
async function codigoDisponivel(page: Page) {
  return (
    await page
      .locator('.cartao-armario')
      .filter({ hasText: 'Disponível' })
      .first()
      .getByRole('heading')
      .innerText()
  ).trim();
}
async function reservar(page: Page, codigo: string) {
  await page
    .locator('.cartao-armario')
    .filter({ has: page.getByRole('heading', { name: codigo, exact: true }) })
    .click();
  await expect(
    page.getByRole('heading', { name: 'Detalhes do armário', exact: true }),
  ).toBeVisible();
  await page.getByRole('button', { name: `Reservar armário ${codigo}`, exact: true }).click();
  await expect(
    page.getByRole('heading', { name: 'Reserva confirmada!', exact: true }),
  ).toBeVisible();
}

test('fluxo completo: pesquisar, reservar, cancelar, consultar histórico e encerrar', async ({
  page,
}) => {
  const erros: string[] = [];
  page.on('pageerror', (e) => erros.push(e.message));
  page.on('dialog', (d) => d.accept());
  await entrar(page);
  await limparAtiva(page);
  await page
    .getByRole('navigation', { name: 'Menu do aluno' })
    .getByRole('link', { name: 'Início', exact: true })
    .click();
  await page.screenshot({ path: '../../../../docs/validacao/08-inicio.png', fullPage: true });
  await page
    .getByRole('navigation', { name: 'Menu do aluno' })
    .getByRole('link', { name: 'Reservar armário', exact: true })
    .click();
  await page.screenshot({ path: '../../../../docs/validacao/01-lista.png', fullPage: true });
  const codigo = await codigoDisponivel(page);
  await page.getByRole('textbox', { name: 'Buscar armário' }).fill(codigo);
  await expect(page.locator('.cartao-armario')).toHaveCount(1);
  await page.locator('.cartao-armario').click();
  await page.screenshot({ path: '../../../../docs/validacao/02-detalhe.png', fullPage: true });
  await page.getByRole('button', { name: `Reservar armário ${codigo}`, exact: true }).click();
  await expect(
    page.getByRole('heading', { name: 'Reserva confirmada!', exact: true }),
  ).toBeVisible();
  await page.screenshot({ path: '../../../../docs/validacao/03-confirmacao.png', fullPage: true });
  await page.getByRole('link', { name: 'Acessar minha reserva ativa →', exact: true }).click();
  await expect(page.locator('.tempo strong')).toHaveText(/0[23]:[0-5][0-9]:[0-5][0-9]/);
  await page.screenshot({ path: '../../../../docs/validacao/04-reserva.png', fullPage: true });
  await page.getByRole('button', { name: 'Cancelar reserva', exact: true }).click();
  await expect(page.getByRole('status')).toContainText('Reserva cancelada');
  await page.getByRole('link', { name: 'Histórico ↗', exact: true }).click();
  await expect(page.locator('.item-historico').first()).toContainText('Cancelada');
  await page.screenshot({ path: '../../../../docs/validacao/05-historico.png', fullPage: true });
  await page.goto('/core/armario/pesquisa');
  await reservar(page, await codigoDisponivel(page));
  await page.getByRole('link', { name: 'Acessar minha reserva ativa →', exact: true }).click();
  await page.getByRole('button', { name: 'Encerrar uso do armário', exact: true }).click();
  await expect(page.getByRole('status')).toContainText('Uso encerrado');
  await page.getByRole('link', { name: 'Histórico ↗', exact: true }).click();
  await expect(page.locator('.item-historico').first()).toContainText('Uso encerrado');
  expect(erros).toEqual([]);
});

test('duas sessões respeitam ocupação e propriedade da reserva', async ({ page, browser }) => {
  await entrar(page);
  await limparAtiva(page);
  const codigo = await codigoDisponivel(page);
  await reservar(page, codigo);
  const id = page.url().split('/').at(-2)!;
  const outra = await browser.newContext();
  const visitante = await outra.newPage();
  await entrar(visitante, 'v');
  await limparAtiva(visitante);
  const cartao = visitante
    .locator('.cartao-armario')
    .filter({ has: visitante.getByRole('heading', { name: codigo, exact: true }) });
  await expect(cartao).toContainText('Ocupado');
  await cartao.click();
  await expect(
    visitante.getByRole('button', { name: `Reservar armário ${codigo}`, exact: true }),
  ).toBeDisabled();
  const token = (await outra.cookies()).find((c) => c.name === 'XSRF-TOKEN')!.value;
  const proibida = await visitante.request.post(`/backend/core/reserva-armario/${id}/cancelar`, {
    headers: { 'X-XSRF-TOKEN': decodeURIComponent(token) },
  });
  expect(proibida.status()).toBe(403);
  await outra.close();
  await limparAtiva(page);
});

test('telas se adaptam ao celular sem rolagem horizontal', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 });
  await entrar(page);
  await limparAtiva(page);
  expect(
    await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth),
  ).toBeTruthy();
  await page.getByRole('button', { name: 'Abrir menu' }).click();
  await page.screenshot({ path: '../../../../docs/validacao/09-menu-celular.png', fullPage: true });
  expect(
    await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth),
  ).toBeTruthy();
  await page.getByRole('button', { name: 'Fechar menu' }).click();
  await page.screenshot({ path: '../../../../docs/validacao/06-celular.png', fullPage: true });
  await page.locator('.cartao-armario').first().click();
  expect(
    await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth),
  ).toBeTruthy();
  await expect(
    page.getByRole('button', { name: 'Reservar armário A-01', exact: true }),
  ).toBeVisible();
});

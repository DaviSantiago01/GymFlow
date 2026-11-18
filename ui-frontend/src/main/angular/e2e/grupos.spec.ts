import { test, expect, Page } from '@playwright/test';
import path from 'node:path';

const senha = 'GruposTeste123!';
const base = '/backend/core/grupo-treino';
async function cadastrar(page: Page, sufixo: string) {
  const usuario = `grupo_${Date.now()}_${Math.random().toString(36).slice(2, 7)}`;
  const nome = `${usuario} ${sufixo}`;
  await page.goto('/cadastro');
  await page.request.get('/backend/core/aluno/cadastro');
  const token = (await page.context().cookies()).find((c) => c.name === 'XSRF-TOKEN')!.value;
  const r = await page.request.post('/backend/core/aluno/cadastrar', {
    headers: { 'X-XSRF-TOKEN': decodeURIComponent(token) },
    data: { nome, usuario, senha, confirmacaoSenha: senha },
  });
  expect(r.status()).toBe(201);
  await page.goto('/login');
  await page.getByLabel('Usuário', { exact: true }).fill(usuario);
  await page.getByLabel('Senha', { exact: true }).fill(senha);
  await page.getByRole('button', { name: 'Entrar', exact: true }).click();
  await expect(page.getByRole('heading', { name: `Boas-vindas, ${usuario}.` })).toBeVisible();
  return { usuario, nome };
}
async function comando(page: Page, rota: string, data: unknown = {}) {
  await page.request.get(`${base}/criacao`);
  const token = (await page.context().cookies()).find((c) => c.name === 'XSRF-TOKEN')!.value;
  return page.request.post(`${base}/${rota}`, {
    headers: { 'X-XSRF-TOKEN': decodeURIComponent(token) },
    data,
  });
}
async function abrirGrupos(page: Page) {
  const abrir = page.getByRole('button', { name: 'Abrir menu' });
  if (await abrir.isVisible()) await abrir.click();
  await page
    .getByRole('navigation', { name: 'Menu do aluno' })
    .getByRole('link', { name: 'Grupos de treino', exact: true })
    .click();
  await expect(
    page.getByRole('heading', { name: 'Meus grupos de treino', exact: true }),
  ).toBeVisible();
}
async function propor(page: Page) {
  await page.getByRole('link', { name: 'Propor treino', exact: true }).click();
  const campo = page.getByLabel('Data do treino', { exact: true });
  const data = new Date(`${await campo.getAttribute('min')}T12:00:00Z`);
  data.setUTCDate(data.getUTCDate() + 2);
  const dia = data.toISOString().slice(0, 10);
  await campo.fill(dia);
  await page.getByLabel('Horário do treino', { exact: true }).fill('18:00');
  await expect(page.getByText(/encerra automaticamente uma hora antes do treino, em/)).toContainText(
    '17:00',
  );
  await page.getByRole('button', { name: 'Propor treino e enviar convites' }).click();
  await expect(page.getByRole('heading', { name: 'Treinos agendados', exact: true })).toBeVisible();
  await expect(page.getByRole('button', { name: 'Confirmar presença', exact: true })).toBeVisible();
}
test('organizador e membro percorrem convites, presença, saída e encerramento', async ({
  page,
  browser,
  baseURL,
}) => {
  const contexto = await browser.newContext({ baseURL });
  const membro = await contexto.newPage();
  try {
    const colega = await cadastrar(membro, 'Colega');
    await cadastrar(page, 'Organizador');
    await abrirGrupos(page);
    await page.getByRole('link', { name: '+ Criar grupo', exact: true }).click();
    await page.getByRole('button', { name: 'Criar grupo e enviar convites' }).click();
    await expect(page.getByRole('alert')).toContainText('Informe um nome');
    await page.getByLabel('Nome do grupo', { exact: true }).fill('Grupo de teste integrado');
    await page.getByLabel('Nome de usuário', { exact: true }).fill(colega.usuario);
    await page.getByRole('button', { name: 'Buscar usuário', exact: true }).click();
    await page
      .getByRole('checkbox', { name: `${colega.usuario} · ${colega.nome}`, exact: true })
      .check();
    await page.getByRole('button', { name: 'Criar grupo e enviar convites' }).click();
    await expect(
      page.getByRole('heading', { name: 'Grupo de teste integrado', exact: true }),
    ).toBeVisible();
    const grupo = page.url().match(/grupo-treino\/(\d+)\//)![1];
    await abrirGrupos(membro);
    await membro.getByRole('button', { name: 'Aceitar convite', exact: true }).click();
    await expect(membro.getByRole('status')).toContainText('Você entrou no grupo');
    await page.reload();
    await expect(page.getByText('2 membros ativos', { exact: true })).toBeVisible();
    await page.getByRole('button', { name: 'Alterar nome', exact: true }).click();
    await page.getByLabel('Novo nome', { exact: true }).fill('Grupo de colegas');
    await page.getByRole('button', { name: 'Salvar nome', exact: true }).click();
    await expect(
      page.getByRole('heading', { name: 'Grupo de colegas', exact: true }),
    ).toBeVisible();
    await propor(page);
    await page.getByRole('button', { name: 'Confirmar presença', exact: true }).click();
    await expect(page.locator('.situacao')).toContainText('Presença confirmada');
    await membro.getByRole('link', { name: 'Convites para treinos', exact: true }).click();
    await membro.getByRole('button', { name: 'Confirmar presença', exact: true }).click();
    await expect(membro.locator('.situacao')).toContainText('Presença confirmada');
    await page.reload();
    await page.evaluate(() => window.scrollTo(0, 0));
    await page.screenshot({
      path: path.resolve('../../../../docs/validacao/22-grupo-treino.png'),
      fullPage: true,
    });
    await membro.goto(`/core/grupo-treino/${grupo}/visualizacao`);
    membro.on('dialog', (d) => d.accept());
    await membro.getByRole('button', { name: 'Sair do grupo', exact: true }).click();
    await expect(membro.getByRole('alert')).toContainText('Cancele sua presença');
    await membro.getByRole('button', { name: 'Cancelar minha presença', exact: true }).click();
    await expect(membro.locator('.situacao')).toContainText('Cancelado');
    await membro.getByRole('button', { name: 'Sair do grupo', exact: true }).click();
    await expect(
      membro.getByRole('heading', { name: 'Meus grupos de treino', exact: true }),
    ).toBeVisible();
    page.on('dialog', (d) => d.accept());
    await page.getByRole('button', { name: 'Encerrar grupo', exact: true }).click();
    await expect(page.getByText(/Grupo encerrado em/)).toBeVisible();
    await page.getByRole('button', { name: 'Histórico do grupo', exact: true }).click();
    await expect(page.getByText('Treino cancelado', { exact: true })).toBeVisible();
    await expect(page.getByText('Participação encerrada', { exact: true })).toHaveCount(2);
  } finally {
    await contexto.close();
  }
});
test('grupos funcionam no celular com confirmação e cancelamento de treino', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 });
  await cadastrar(page, 'Celular');
  await abrirGrupos(page);
  await page.getByRole('link', { name: '+ Criar grupo', exact: true }).click();
  await page.getByLabel('Nome do grupo', { exact: true }).fill('Treino no celular');
  await page.getByRole('button', { name: 'Criar grupo e enviar convites' }).click();
  await expect(page.getByRole('heading', { name: 'Treino no celular', exact: true })).toBeVisible();
  await propor(page);
  await page.getByRole('button', { name: 'Confirmar presença', exact: true }).click();
  await expect(page.locator('.situacao')).toContainText('Presença confirmada');
  await page.getByRole('link', { name: 'Ver detalhes do treino', exact: true }).click();
  await expect(
    page.getByRole('heading', { name: 'Participantes confirmados', exact: true }),
  ).toBeVisible();
  await page.evaluate(() => window.scrollTo(0, 0));
  await page.screenshot({
    path: path.resolve('../../../../docs/validacao/23-grupo-treino-celular.png'),
    fullPage: true,
  });
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(
    true,
  );
  page.on('dialog', (d) => d.accept());
  await page.getByRole('button', { name: 'Cancelar treino', exact: true }).click();
  await expect(page.getByText('Treino cancelado', { exact: true })).toBeVisible();
  await expect(page.getByText('Nenhuma presença confirmada.', { exact: true })).toBeVisible();
  await page.getByRole('link', { name: '← Voltar ao grupo', exact: true }).click();
  await page.getByRole('button', { name: 'Encerrar grupo', exact: true }).click();
  await expect(page.getByText(/Grupo encerrado em/)).toBeVisible();
});
test('tela deixa de permitir confirmação quando o prazo termina', async ({ page }) => {
  await cadastrar(page, 'Prazo');
  const criado = await comando(page, 'criar', { nome: 'Grupo com prazo curto' });
  expect(criado.ok()).toBe(true);
  const { identificador: g } = await criado.json();
  const agora = Date.now();
  expect(
    (
      await comando(page, `${g}/propor`, {
        // início 1h + 10s à frente: o prazo (início - 1h) vence em ~10s
        inicio: new Date(agora + 3610000).toISOString(),
      })
    ).ok(),
  ).toBe(true);
  await page.goto('/core/grupo-treino/convites-treino');
  await expect(page.getByRole('button', { name: 'Confirmar presença', exact: true })).toBeVisible();
  await expect(page.getByRole('button', { name: 'Confirmar presença', exact: true })).toBeHidden({
    timeout: 20000,
  });
  await expect(page.locator('.situacao')).toContainText('Prazo encerrado');
  await page.reload();
  await expect(page.locator('.situacao')).toContainText('Prazo encerrado');
  expect((await comando(page, `${g}/encerrar`)).ok()).toBe(true);
});

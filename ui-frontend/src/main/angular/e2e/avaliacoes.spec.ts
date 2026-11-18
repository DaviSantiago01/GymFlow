import { test, expect, Page } from '@playwright/test';

async function cadastrarEEntrar(page: Page) {
  const usuario = `avaliacao_${Date.now()}`;
  await page.goto('/cadastro');
  await page.getByLabel('Nome completo', { exact: true }).fill('Aluno de Teste de Avaliações');
  await page.getByLabel('Nome de usuário', { exact: true }).fill(usuario);
  await page.getByLabel('Senha', { exact: true }).fill('SenhaDeTeste123!');
  await page.getByLabel('Confirmar senha', { exact: true }).fill('SenhaDeTeste123!');
  await page.getByRole('button', { name: 'Criar conta', exact: true }).click();
  await page.getByRole('link', { name: 'Entrar com minha conta' }).click();
  await page.getByLabel('Senha', { exact: true }).fill('SenhaDeTeste123!');
  await page.getByRole('button', { name: 'Entrar', exact: true }).click();
  await expect(page.getByRole('heading', { name: `Boas-vindas, ${usuario}.` })).toBeVisible();
}

async function escolherNotas(page: Page, notas: number[]) {
  const campos = page.locator('app-criterios fieldset');
  for (let i = 0; i < notas.length; i++)
    await campos
      .nth(i)
      .getByRole('radio', { name: String(notas[i]), exact: true })
      .check();
}

async function abrirHistorico(page: Page) {
  const abrir = page.getByRole('button', { name: 'Abrir menu' });
  if (await abrir.isVisible()) await abrir.click();
  const menu = page.getByRole('navigation', { name: 'Menu do aluno' });
  await expect(menu.getByRole('link', { name: 'Histórico de reservas', exact: true })).toBeVisible();
  await menu.getByRole('link', { name: 'Avaliar treinos', exact: true }).click();
  await expect(
    page.getByRole('heading', { name: 'Histórico de treinos', exact: true }),
  ).toBeVisible();
}
async function capturar(page: Page, nome: string) {
  await page.evaluate(() => window.scrollTo(0, 0));
  await page.screenshot({ path: '../../../../docs/validacao/' + nome, fullPage: true });
}

test('histórico permite avaliar dois treinos do mesmo professor, editar e excluir', async ({
  page,
}) => {
  await cadastrarEEntrar(page);
  await abrirHistorico(page);
  await expect(page.getByRole('heading', { name: 'Nenhum treino concluído' })).toBeVisible();
  await page.getByRole('button', { name: 'Carregar treinos de demonstração' }).click();
  const treinos = page.locator('.treino');
  await expect(treinos).toHaveCount(3);
  await expect(treinos.first()).toContainText('Treino de demonstração');
  await expect(treinos.first().locator('dd').nth(0)).toHaveText(/\d{2}\/\d{2}\/\d{4}/);
  await expect(treinos.first().locator('dd').nth(1)).toHaveText(/\d{2}:\d{2} – \d{2}:\d{2}/);
  await expect(treinos.nth(1).locator('dd').nth(2)).toHaveText(
    await treinos.first().locator('dd').nth(2).innerText(),
  );
  const treinoId = await treinos.first().getAttribute('data-treino');
  await capturar(page, '19-historico-treinos.png');
  await treinos.first().getByRole('link', { name: 'Avaliar treino' }).click();
  await expect(page).toHaveURL(new RegExp('/core/treino/' + treinoId + '/avaliacao$'));
  await expect(page.getByRole('heading', { name: 'Avaliar treino', exact: true })).toBeVisible();
  await page.getByRole('button', { name: 'Salvar avaliação' }).click();
  await expect(page.getByRole('alert')).toContainText('Preencha as cinco notas');
  await escolherNotas(page, [0, 2, 3, 4, 5]);
  await expect(page.getByRole('alert')).toHaveCount(0);
  await page.getByLabel('Comentário (opcional)').fill('Avaliação do treino selecionado.');
  await capturar(page, '20-avaliar-treino.png');
  await page.getByRole('button', { name: 'Salvar avaliação' }).click();
  await expect(page.getByRole('heading', { name: 'Minha avaliação', exact: true })).toBeVisible();
  await page.reload();
  await expect(page.locator('.comentario')).toHaveText('Avaliação do treino selecionado.');
  await page.getByRole('link', { name: 'Editar avaliação' }).click();
  await escolherNotas(page, [5, 5, 5, 5, 5]);
  await page.getByLabel('Comentário (opcional)').fill('Treino avaliado e comentário atualizado.');
  await page.getByRole('button', { name: 'Salvar avaliação' }).click();
  await expect(page.locator('.comentario')).toHaveText('Treino avaliado e comentário atualizado.');
  await page.getByRole('link', { name: 'Histórico de treinos', exact: true }).click();
  await expect(treinos.first().getByRole('link', { name: 'Ver avaliação' })).toBeVisible();
  await treinos.nth(1).getByRole('link', { name: 'Avaliar treino' }).click();
  await escolherNotas(page, [1, 1, 1, 1, 1]);
  await page.getByRole('button', { name: 'Salvar avaliação' }).click();
  await expect(page.getByRole('heading', { name: 'Minha avaliação', exact: true })).toBeVisible();
  await page.getByRole('link', { name: 'Ver perfil', exact: true }).click();
  await expect(page.locator('.perfil-cabecalho')).toBeVisible();
  await expect(page.getByRole('link', { name: 'Avaliar professor', exact: true })).toHaveCount(0);
  await expect(page.getByRole('link', { name: 'Avaliar treino', exact: true })).toHaveCount(0);
  await page.getByRole('link', { name: 'Ver meus treinos', exact: true }).click();
  await treinos.first().getByRole('link', { name: 'Ver avaliação' }).click();
  page.once('dialog', (d) => d.dismiss());
  await page.getByRole('button', { name: 'Excluir avaliação' }).click();
  await expect(page.getByRole('heading', { name: 'Minha avaliação', exact: true })).toBeVisible();
  page.once('dialog', (d) => d.accept());
  await page.getByRole('button', { name: 'Excluir avaliação' }).click();
  await expect(treinos.first()).toContainText('Avaliação excluída');
  await expect(treinos.first().getByRole('link', { name: 'Avaliar treino' })).toHaveCount(0);
  await treinos.nth(1).getByRole('link', { name: 'Ver avaliação' }).click();
  page.once('dialog', (d) => d.accept());
  await page.getByRole('button', { name: 'Excluir avaliação' }).click();
  await expect(treinos.nth(1)).toContainText('Avaliação excluída');
});

test('histórico e avaliação de treino no celular com nota zero e comentário vazio', async ({
  page,
}) => {
  await page.setViewportSize({ width: 390, height: 844 });
  await cadastrarEEntrar(page);
  await abrirHistorico(page);
  await page.getByRole('button', { name: 'Carregar treinos de demonstração' }).click();
  await expect(page.locator('.treino')).toHaveCount(3);
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(
    true,
  );
  await capturar(page, '21-historico-treinos-mobile.png');
  await page.locator('.treino').first().getByRole('link', { name: 'Avaliar treino' }).click();
  await escolherNotas(page, [0, 0, 0, 0, 0]);
  await page.getByRole('button', { name: 'Salvar avaliação' }).click();
  await expect(page.locator('.comentario')).toHaveText('Sem comentário.');
  await expect(page.locator('app-criterios .rotulo strong').first()).toHaveText('0,0 / 5');
  page.once('dialog', (d) => d.accept());
  await page.getByRole('button', { name: 'Excluir avaliação' }).click();
  await expect(page.locator('.treino').first()).toContainText('Avaliação excluída');
});

import { expect, Page, test } from '@playwright/test';

async function entrar(page: Page, usuario: string) {
  await page.getByLabel('Usuário', { exact: true }).fill(usuario);
  await page.getByLabel('Senha', { exact: true }).fill(usuario);
  await page.getByRole('button', { name: 'Entrar', exact: true }).click();
  await expect(page.getByRole('heading', { name: 'Boas-vindas, ' + usuario + '.' })).toBeVisible();
}

test('organiza os atalhos pelo papel da sessão, inclusive após recarregar e trocar de conta', async ({
  page,
}) => {
  await page.goto('/login');

    await entrar(page, 'gestor');
  const gestor = page.getByRole('navigation', { name: 'Menu do gestor' });
  await expect(gestor.getByRole('link', { name: 'Equipamentos', exact: true })).toBeVisible();
  await expect(gestor.getByRole('link', { name: 'Mensalidades', exact: true })).toBeVisible();
  await expect(gestor.getByRole('link', { name: 'Alunos', exact: true })).toHaveCount(0);
  await expect(page.getByRole('main').getByRole('link', { name: /Equipamentos/ })).toBeVisible();
  await page.reload();
  await expect(gestor).toBeVisible();
  await page.getByRole('button', { name: 'Sair', exact: true }).click();

    await entrar(page, 'professor');
  const professor = page.getByRole('navigation', { name: 'Menu do professor' });
  await expect(professor.getByRole('link', { name: 'Alunos', exact: true })).toBeVisible();
  await expect(
    professor.getByRole('link', { name: 'Planos de treino', exact: true }),
  ).toBeVisible();
  await expect(
    professor.getByRole('link', { name: 'Avaliações físicas', exact: true }),
  ).toBeVisible();
  await expect(professor.getByRole('link', { name: 'Equipamentos', exact: true })).toHaveCount(0);
  await expect(
    professor.getByRole('link', { name: 'Registrar execução', exact: true }),
  ).toHaveCount(0);
  await professor.getByRole('link', { name: 'Alunos', exact: true }).click();
  await expect(page).toHaveURL(/\/core\/aluno\/pesquisa$/);
  await page.getByRole('button', { name: 'Sair', exact: true }).click();

    await entrar(page, 'aluno');
  const aluno = page.getByRole('navigation', { name: 'Menu do aluno' });
  await expect(aluno.getByRole('link', { name: 'Registrar execução', exact: true })).toBeVisible();
  await expect(aluno.getByRole('link', { name: 'Meu plano', exact: true })).toBeVisible();
  await expect(aluno.getByRole('link', { name: 'Reservar armário', exact: true })).toBeVisible();
  await expect(aluno.getByRole('link', { name: 'Alunos', exact: true })).toHaveCount(0);
  await expect(aluno.getByRole('link', { name: 'Equipamentos', exact: true })).toHaveCount(0);
  expect((await page.request.get('/backend/core/equipamento/pesquisa')).status()).toBe(403);

    await page.setViewportSize({ width: 390, height: 844 });
    await page.getByRole('button', { name: 'Abrir menu', exact: true }).click();
    await aluno.getByRole('link', { name: 'Meu plano', exact: true }).click();
    await expect(page).toHaveURL(/\/core\/adesao-plano\/meu-plano$/);
    await expect(aluno).toBeHidden();
});

import { test, expect } from '@playwright/test';

test('criar conta, entrar com senha acentuada e reservar como o novo aluno', async ({ page }) => {
  const usuario = `aluno_${Date.now()}`;
  const senha = 'MinhaSenhaÁgil123!';
  await page.goto('/login');
  await page.getByRole('link', { name: 'Criar minha conta' }).click();
  await page.getByLabel('Nome completo', { exact: true }).fill('Aluno Cadastrado no Navegador');
  await page.getByLabel('Nome de usuário', { exact: true }).fill(usuario);
  await page.getByLabel('Senha', { exact: true }).fill(senha);
  await page.getByLabel('Confirmar senha', { exact: true }).fill('OutraSenha123!');
  await page.getByRole('button', { name: 'Criar conta', exact: true }).click();
  await expect(page.getByRole('alert')).toContainText('As senhas não conferem');
  await page.getByLabel('Confirmar senha', { exact: true }).fill(senha);
  await page.screenshot({ path: '../../../../docs/validacao/07-cadastro.png', fullPage: true });
  await page.getByRole('button', { name: 'Criar conta', exact: true }).click();
  await expect(page.getByRole('heading', { name: 'Conta criada com sucesso!' })).toBeVisible();
  await page.getByRole('link', { name: 'Entrar com minha conta' }).click();
  await expect(page.getByLabel('Usuário', { exact: true })).toHaveValue(usuario);
  await page.getByLabel('Senha', { exact: true }).fill(senha);
  await page.getByRole('button', { name: 'Entrar', exact: true }).click();
  await expect(page.getByRole('heading', { name: `Boas-vindas, ${usuario}.` })).toBeVisible();
  await page
    .getByRole('navigation', { name: 'Menu do aluno' })
    .getByRole('link', { name: 'Reservar armário', exact: true })
    .click();
  await expect(page.getByRole('heading', { name: 'Armários', exact: true })).toBeVisible();
  await page.locator('.cartao-armario').filter({ hasText: 'Disponível' }).first().click();
  await page.getByRole('button', { name: /Reservar armário/ }).click();
  await expect(page.getByRole('heading', { name: 'Reserva confirmada!' })).toBeVisible();
  await expect(page.locator('dd').first()).toHaveText('Aluno Cadastrado no Navegador');
  await page.getByRole('link', { name: 'Acessar minha reserva ativa →' }).click();
  page.on('dialog', (dialog) => dialog.accept());
  await page.getByRole('button', { name: 'Encerrar uso do armário' }).click();
  await expect(page.getByRole('status')).toContainText('Uso encerrado');
  await page.getByRole('button', { name: 'Sair', exact: true }).click();
  await expect(page.getByRole('heading', { name: 'Entrar', exact: true })).toBeVisible();
  await page.getByLabel('Usuário', { exact: true }).fill(usuario);
  await page.getByLabel('Senha', { exact: true }).fill(senha);
  await page.getByRole('button', { name: 'Entrar', exact: true }).click();
  await page
    .getByRole('navigation', { name: 'Menu do aluno' })
    .getByRole('link', { name: 'Histórico de reservas', exact: true })
    .click();
  await expect(page.locator('.item-historico').first()).toContainText('Uso encerrado');
});

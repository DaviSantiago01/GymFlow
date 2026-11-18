import { expect, Page, test } from '@playwright/test';

async function entrar(page: Page) {
  await page.goto('/core/equipamento/pesquisa');
  await page.getByLabel('Usuário', { exact: true }).fill('admin');
  await page.getByLabel('Senha', { exact: true }).fill('admin');
  await page.getByRole('button', { name: 'Entrar', exact: true }).click();
  await expect(page.getByRole('heading', { name: 'Equipamentos', exact: true })).toBeVisible();
}

async function cadastrar(page: Page, nome: string) {
  await page.getByRole('link', { name: 'Cadastrar equipamento', exact: true }).click();
  await page.getByLabel('Nome', { exact: true }).fill(nome);
  await page.getByLabel('Patrimônio').fill('EQ-' + Date.now());
  await page.getByLabel('Tipo', { exact: true }).selectOption('CARDIO');
  await page.getByLabel('Marca').fill('GymFlow');
  await page.getByLabel('Modelo').fill('Cardio 1');
  await page.getByLabel('Área inicial').selectOption('CARDIO');
  await page.getByRole('button', { name: 'Salvar', exact: true }).click();
  await expect(page.getByRole('heading', { name: nome, exact: true })).toBeVisible();
}

test('cadastra, pesquisa, edita e exclui equipamento sem histórico', async ({ page }) => {
  const nome = 'Esteira E2E ' + Date.now();
  await entrar(page);
  await cadastrar(page, nome);
  await page.getByRole('button', { name: 'Editar cadastro' }).click();
  await page.getByLabel('Modelo').fill('Cardio 2');
  await page.getByRole('button', { name: 'Salvar', exact: true }).click();
  await expect(page.getByText('Cardio 2', { exact: true })).toBeVisible();

  await page.getByRole('link', { name: 'Voltar aos equipamentos' }).click();
  await page.getByLabel('Nome, patrimônio ou série').fill(nome);
  await page.getByRole('button', { name: 'Pesquisar', exact: true }).click();
  const linha = page.getByRole('row').filter({ hasText: nome });
  await expect(linha).toBeVisible();
  await linha.getByRole('link', { name: 'Ver detalhes', exact: false }).click();
  await expect(page.getByRole('heading', { name: nome, exact: true })).toBeVisible();
  await page.reload();
  await expect(page.getByRole('heading', { name: nome, exact: true })).toBeVisible();

  await page.getByRole('button', { name: 'Excluir equipamento' }).click();
  await page.getByRole('button', { name: 'Confirmar exclusão' }).click();
  await expect(page.getByRole('heading', { name: 'Equipamentos', exact: true })).toBeVisible();
  await page.getByLabel('Nome, patrimônio ou série').fill(nome);
  await page.getByRole('button', { name: 'Pesquisar', exact: true }).click();
  await expect(page.getByText('Nenhum registro encontrado para esta pesquisa.')).toBeVisible();
});

test('interdita, conclui manutenção, libera por inspeção e preserva o histórico', async ({
  page,
}) => {
  const nome = 'Bicicleta E2E ' + Date.now();
  await page.setViewportSize({ width: 390, height: 844 });
  await entrar(page);
  await cadastrar(page, nome);
  await page.getByRole('link', { name: 'Registrar problema' }).click();
  await page.getByLabel('Título').fill('Correia solta');
  await page.getByLabel('Gravidade').selectOption('CRITICA');
  await page.getByLabel('Relato', { exact: true }).fill('Correia solta durante o funcionamento.');
  await page.getByRole('button', { name: 'Salvar', exact: true }).click();
  await expect(page.getByRole('heading', { name: 'Correia solta' })).toBeVisible();
  await page.getByRole('link', { name: nome, exact: true }).click();
  await expect(page.getByText(/Patrimônio .* Interditado/)).toBeVisible();
  await expect(page.getByRole('button', { name: 'Excluir equipamento' })).toHaveCount(0);

  await page.getByRole('link', { name: 'Consultar problemas' }).click();
  await page.getByRole('link', { name: 'Ver detalhes Correia solta' }).click();
  await page.getByRole('link', { name: 'Abrir ordem', exact: true }).click();
  await page.getByLabel('Técnico', { exact: true }).fill('Técnico E2E');
  await page.getByLabel('Diagnóstico').fill('Tensão fora do padrão.');
  await page.getByRole('button', { name: 'Salvar', exact: true }).click();
  await page.getByRole('button', { name: 'Iniciar ordem' }).click();
  await page.getByRole('button', { name: 'Salvar', exact: true }).click();
  await page.getByRole('button', { name: 'Concluir ordem' }).click();
  await expect(page.getByRole('button', { name: 'Salvar', exact: true })).toBeDisabled();
  await page.getByLabel('Serviço executado').fill('Correia substituída e ajustada.');
  await page.getByRole('button', { name: 'Salvar', exact: true }).click();
  await expect(page.getByText('Concluída · Correia solta')).toBeVisible();
  await page.getByRole('link', { name: 'Correia solta', exact: true }).click();
  await expect(page.getByText('Resolvido · ' + nome)).toBeVisible();

  await page.getByRole('link', { name: nome, exact: true }).click();
  await expect(page.getByText(/Patrimônio .* Em manutenção/)).toBeVisible();
  await page.getByRole('button', { name: 'Registrar inspeção' }).click();
  await page.getByLabel('Situação após o registro').selectOption('DISPONIVEL');
  await page.getByRole('button', { name: 'Salvar', exact: true }).click();
  await expect(page.getByText(/Patrimônio .* Disponível/)).toBeVisible();
  await page.getByRole('link', { name: 'Ver histórico' }).click();
  await expect(page.getByRole('row').filter({ hasText: 'Aprovado' })).toBeVisible();
  expect(await page.evaluate(() => document.documentElement.scrollWidth)).toBeLessThanOrEqual(390);

  await page
    .getByRole('row')
    .filter({ hasText: 'Aprovado' })
    .getByRole('link', { name: 'Ver detalhes', exact: false })
    .click();
  await page.getByRole('link', { name: 'Ver equipamento' }).click();
  await page.getByRole('button', { name: 'Inativar equipamento' }).click();
  await page.getByRole('button', { name: 'Confirmar inativação' }).click();
  await expect(page.getByText(/Patrimônio .* Inativo/)).toBeVisible();
});

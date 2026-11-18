const PERFIS = {
  ADMINISTRADOR: { nome: 'Gestor', area: 'Área do gestor', menu: 'Menu do gestor' },
  PERSONAL: { nome: 'Professor', area: 'Área do professor', menu: 'Menu do professor' },
  USUARIO: { nome: 'Aluno', area: 'Área do aluno', menu: 'Menu do aluno' },
} as const;

export function perfilDaSessao(autorizacoes: readonly string[]) {
  if (autorizacoes.includes('ADMINISTRADOR')) return PERFIS.ADMINISTRADOR;
  if (autorizacoes.includes('PERSONAL')) return PERFIS.PERSONAL;
  if (autorizacoes.includes('USUARIO')) return PERFIS.USUARIO;
  return null;
}

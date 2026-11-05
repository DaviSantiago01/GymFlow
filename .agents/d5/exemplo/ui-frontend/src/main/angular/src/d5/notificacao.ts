export type TipoNotificacao = 'erro' | 'sucesso'

export interface Notificacao {
  id: number
  tipo: TipoNotificacao
  mensagem: string
}

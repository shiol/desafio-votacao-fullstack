export type Sessao = {
  abertaEm: string;
  fechaEm: string;
  aberta: boolean;
};

export type Pauta = {
  id: number;
  titulo: string;
  descricao?: string | null;
  createdAt: string;
  sessao?: Sessao | null;
};

export type Resultado = {
  pautaId: number;
  totalVotos: number;
  votosSim: number;
  votosNao: number;
  status: string;
};

export type VotoValor = "SIM" | "NAO";

export type NotificationType = "info" | "success" | "error";

export type Notification = {
  message: string;
  type: NotificationType;
};

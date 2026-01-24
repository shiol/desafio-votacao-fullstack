import { fetchJson } from "./client";
import type { Pauta, Resultado, VotoValor } from "../types";

type CreatePautaPayload = {
  titulo: string;
  descricao?: string;
};

type OpenSessaoPayload = {
  duracaoMinutos?: number;
};

type VotoPayload = {
  associadoId: string;
  voto: VotoValor;
};

export function listPautas(): Promise<Pauta[]> {
  return fetchJson<Pauta[]>("/pautas");
}

export function getPauta(id: number): Promise<Pauta> {
  return fetchJson<Pauta>(`/pautas/${id}`);
}

export function createPauta(payload: CreatePautaPayload): Promise<Pauta> {
  return fetchJson<Pauta>("/pautas", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export function openSessao(id: number, payload: OpenSessaoPayload): Promise<void> {
  return fetchJson<void>(`/pautas/${id}/sessoes`, {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export function votar(id: number, payload: VotoPayload): Promise<void> {
  return fetchJson<void>(`/pautas/${id}/votos`, {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export function getResultado(id: number): Promise<Resultado> {
  return fetchJson<Resultado>(`/pautas/${id}/resultado`);
}

export function deletePauta(id: number): Promise<void> {
  return fetchJson<void>(`/pautas/${id}`, {
    method: "DELETE"
  });
}

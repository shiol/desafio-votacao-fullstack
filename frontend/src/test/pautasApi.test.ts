import { vi } from "vitest";
import { createPauta, deletePauta, getPauta, getResultado, listPautas, openSessao, votar } from "../api/pautas";
import { fetchJson } from "../api/client";

vi.mock("../api/client", () => ({
  fetchJson: vi.fn()
}));

const mockFetchJson = vi.mocked(fetchJson);

beforeEach(() => {
  mockFetchJson.mockReset();
});

it("lista pautas", async () => {
  mockFetchJson.mockResolvedValueOnce([]);
  await listPautas();
  expect(mockFetchJson).toHaveBeenCalledWith("/pautas");
});

it("busca pauta por id", async () => {
  mockFetchJson.mockResolvedValueOnce({});
  await getPauta(2);
  expect(mockFetchJson).toHaveBeenCalledWith("/pautas/2");
});

it("cria pauta", async () => {
  mockFetchJson.mockResolvedValueOnce({});
  await createPauta({ titulo: "Nova", descricao: "Descricao" });
  expect(mockFetchJson).toHaveBeenCalledWith("/pautas", expect.objectContaining({ method: "POST" }));
});

it("abre sessao", async () => {
  mockFetchJson.mockResolvedValueOnce(null);
  await openSessao(1, { duracaoMinutos: 1 });
  expect(mockFetchJson).toHaveBeenCalledWith("/pautas/1/sessoes", expect.objectContaining({ method: "POST" }));
});

it("vota", async () => {
  mockFetchJson.mockResolvedValueOnce(null);
  await votar(1, { associadoId: "123", voto: "SIM" });
  expect(mockFetchJson).toHaveBeenCalledWith("/pautas/1/votos", expect.objectContaining({ method: "POST" }));
});

it("carrega resultado", async () => {
  mockFetchJson.mockResolvedValueOnce({});
  await getResultado(3);
  expect(mockFetchJson).toHaveBeenCalledWith("/pautas/3/resultado");
});

it("remove pauta", async () => {
  mockFetchJson.mockResolvedValueOnce(null);
  await deletePauta(4);
  expect(mockFetchJson).toHaveBeenCalledWith("/pautas/4", expect.objectContaining({ method: "DELETE" }));
});

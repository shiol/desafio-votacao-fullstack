import { vi } from "vitest";
import { ApiError, fetchJson } from "../api/client";

const mockFetch = vi.fn();

beforeEach(() => {
  mockFetch.mockReset();
  globalThis.fetch = mockFetch as unknown as typeof fetch;
});

it("retorna JSON quando a resposta e valida", async () => {
  mockFetch.mockResolvedValue({
    ok: true,
    status: 200,
    text: vi.fn().mockResolvedValue(JSON.stringify({ ok: true }))
  });

  const result = await fetchJson<{ ok: boolean }>("/teste");

  expect(result.ok).toBe(true);
});

it("retorna null para status 204", async () => {
  mockFetch.mockResolvedValue({
    ok: true,
    status: 204,
    text: vi.fn()
  });

  const result = await fetchJson<null>("/teste");

  expect(result).toBeNull();
});

it("retorna null quando o corpo esta vazio", async () => {
  mockFetch.mockResolvedValue({
    ok: true,
    status: 200,
    text: vi.fn().mockResolvedValue("")
  });

  const result = await fetchJson<null>("/teste");

  expect(result).toBeNull();
});

it("usa mensagem do backend quando falha", async () => {
  mockFetch.mockResolvedValue({
    ok: false,
    status: 400,
    statusText: "Bad Request",
    json: vi.fn().mockResolvedValue({ message: "Erro de validacao" })
  });

  await expect(fetchJson("/teste")).rejects.toBeInstanceOf(ApiError);
  await expect(fetchJson("/teste")).rejects.toMatchObject({ message: "Erro de validacao", status: 400 });
});

it("mantem mensagem padrao quando JSON nao tem message", async () => {
  mockFetch.mockResolvedValue({
    ok: false,
    status: 422,
    statusText: "Unprocessable",
    json: vi.fn().mockResolvedValue({})
  });

  await expect(fetchJson("/teste")).rejects.toMatchObject({ message: "Request failed", status: 422 });
});

it("usa statusText quando nao retorna JSON", async () => {
  mockFetch.mockResolvedValue({
    ok: false,
    status: 500,
    statusText: "Server error",
    json: vi.fn().mockRejectedValue(new Error("invalid"))
  });

  await expect(fetchJson("/teste")).rejects.toMatchObject({ message: "Server error", status: 500 });
});

it("usa mensagem padrao quando statusText esta vazio", async () => {
  mockFetch.mockResolvedValue({
    ok: false,
    status: 502,
    statusText: "",
    json: vi.fn().mockRejectedValue(new Error("invalid"))
  });

  await expect(fetchJson("/teste")).rejects.toMatchObject({ message: "Request failed", status: 502 });
});

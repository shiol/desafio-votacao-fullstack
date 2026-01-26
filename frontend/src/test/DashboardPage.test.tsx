import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { vi } from "vitest";
import DashboardPage from "../pages/DashboardPage";
import { deletePauta, getResultado, listPautas, openSessao, votar } from "../api/pautas";
import type { Pauta } from "../types";

vi.mock("../api/pautas", () => ({
  deletePauta: vi.fn(),
  getResultado: vi.fn(),
  listPautas: vi.fn(),
  openSessao: vi.fn(),
  votar: vi.fn()
}));

const mockListPautas = vi.mocked(listPautas);
const mockGetResultado = vi.mocked(getResultado);
const mockDeletePauta = vi.mocked(deletePauta);
const mockOpenSessao = vi.mocked(openSessao);
const mockVotar = vi.mocked(votar);

beforeEach(() => {
  vi.clearAllMocks();
  mockGetResultado.mockResolvedValue({
    pautaId: 1,
    totalVotos: 0,
    votosSim: 0,
    votosNao: 0,
    status: "ABERTA"
  });
  mockDeletePauta.mockResolvedValue();
  mockOpenSessao.mockResolvedValue();
  mockVotar.mockResolvedValue();
});

const renderPage = () =>
  render(
    <MemoryRouter>
      <DashboardPage />
    </MemoryRouter>
  );

const buildPauta = (id: number, createdAt: string): Pauta => ({
  id,
  titulo: `Pauta ${id}`,
  descricao: "Descricao",
  createdAt,
  sessao: null
});

it("exibe estado vazio quando nao ha pautas", async () => {
  mockListPautas.mockResolvedValueOnce([]);

  renderPage();

  expect(await screen.findByText(/Sem pautas ainda/i)).toBeInTheDocument();
});

it("exibe mensagem de erro e botao de tentativa quando falha", async () => {
  mockListPautas.mockRejectedValueOnce(new Error("Falha ao carregar"));

  renderPage();

  const messages = await screen.findAllByText("Falha ao carregar");
  expect(messages.length).toBeGreaterThan(0);
  expect(screen.getByRole("button", { name: /Tentar novamente/i })).toBeInTheDocument();
});

it("ignora notificacao vazia quando erro nao tem mensagem", async () => {
  mockListPautas.mockRejectedValueOnce(new Error(""));

  renderPage();

  expect(await screen.findByText(/Sem pautas ainda/i)).toBeInTheDocument();
  expect(screen.queryByRole("status")).toBeNull();
});

it("exibe carregando enquanto busca e depois mostra a lista", async () => {
  let resolveList: (value: Pauta[]) => void = () => undefined;
  const pending = new Promise<Pauta[]>((resolve) => {
    resolveList = resolve;
  });
  mockListPautas.mockReturnValueOnce(pending);

  renderPage();

  expect(screen.getByText(/Carregando pautas/i)).toBeInTheDocument();
  expect(screen.getByRole("button", { name: /Atualizando/i })).toBeDisabled();

  resolveList([
    buildPauta(1, "2024-01-01T00:00:00.000Z"),
    buildPauta(2, "2024-02-01T00:00:00.000Z")
  ]);

  await waitFor(() => {
    expect(screen.getByText("Pauta 2")).toBeInTheDocument();
  });
});

it("ordena as pautas pela data mais recente e permite atualizar", async () => {
  mockListPautas.mockResolvedValueOnce([
    buildPauta(1, "2024-01-01T00:00:00.000Z"),
    buildPauta(2, "2024-03-01T00:00:00.000Z")
  ]);

  renderPage();

  const titles = await screen.findAllByRole("heading", { level: 3 });
  expect(titles[0]).toHaveTextContent("Pauta 2");
  expect(titles[1]).toHaveTextContent("Pauta 1");

  mockListPautas.mockResolvedValueOnce([buildPauta(3, "2024-04-01T00:00:00.000Z")]);

  const callsBefore = mockListPautas.mock.calls.length;

  await userEvent.click(screen.getByRole("button", { name: /Atualizar/i }));

  await waitFor(() => {
    expect(mockListPautas.mock.calls.length).toBe(callsBefore + 1);
  });
});

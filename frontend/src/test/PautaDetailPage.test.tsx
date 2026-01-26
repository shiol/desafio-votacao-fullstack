import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { vi } from "vitest";
import { ApiError } from "../api/client";
import { deletePauta, getPauta, getResultado, openSessao, votar } from "../api/pautas";
import PautaDetailPage from "../pages/PautaDetailPage";

vi.mock("../api/pautas", () => ({
  deletePauta: vi.fn(),
  getPauta: vi.fn(),
  getResultado: vi.fn(),
  openSessao: vi.fn(),
  votar: vi.fn()
}));

const mockGetPauta = vi.mocked(getPauta);
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

const renderWithRoute = (path: string) =>
  render(
    <MemoryRouter initialEntries={[path]}>
      <Routes>
        <Route path="/" element={<div>Lista</div>} />
        <Route path="/pautas/:id" element={<PautaDetailPage />} />
      </Routes>
    </MemoryRouter>
  );

it("exibe erro quando o id nao e valido", async () => {
  renderWithRoute("/pautas/abc");

  const invalidMessages = await screen.findAllByText(/Pauta inválida/i);
  expect(invalidMessages.length).toBeGreaterThan(0);
});

it("exibe mensagem quando pauta nao existe", async () => {
  mockGetPauta.mockRejectedValueOnce(new ApiError("Pauta não encontrada.", 404));

  renderWithRoute("/pautas/99");

  const notFoundMessages = await screen.findAllByText(/Pauta não encontrada/i);
  expect(notFoundMessages.length).toBeGreaterThan(0);
  expect(screen.getByRole("button", { name: /Voltar para pautas/i })).toBeInTheDocument();
});

it("ignora notificacao vazia quando erro nao tem mensagem", async () => {
  mockGetPauta.mockRejectedValueOnce(new Error(""));

  renderWithRoute("/pautas/77");

  expect(await screen.findByText(/Nenhuma pauta encontrada/i)).toBeInTheDocument();
  expect(screen.queryByRole("status")).toBeNull();
});

it("exibe card quando pauta existe", async () => {
  mockGetPauta.mockResolvedValueOnce({
    id: 1,
    titulo: "Pauta A",
    descricao: "Descricao",
    createdAt: new Date().toISOString(),
    sessao: null
  });

  renderWithRoute("/pautas/1");

  expect(await screen.findByText("Pauta A")).toBeInTheDocument();
});

it("exibe loading enquanto busca a pauta", async () => {
  let resolvePauta: (value: {
    id: number;
    titulo: string;
    descricao: string;
    createdAt: string;
    sessao: null;
  }) => void = () => undefined;
  const pending = new Promise<{
    id: number;
    titulo: string;
    descricao: string;
    createdAt: string;
    sessao: null;
  }>((resolve) => {
    resolvePauta = resolve;
  });
  mockGetPauta.mockReturnValueOnce(pending);

  renderWithRoute("/pautas/1");

  expect(screen.getByText(/Carregando detalhes da pauta/i)).toBeInTheDocument();

  resolvePauta({
    id: 1,
    titulo: "Pauta B",
    descricao: "Descricao",
    createdAt: new Date().toISOString(),
    sessao: null
  });

  await waitFor(() => {
    expect(screen.getByText("Pauta B")).toBeInTheDocument();
  });
});

it("permite atualizar a pauta", async () => {
  mockGetPauta
    .mockResolvedValueOnce({
      id: 1,
      titulo: "Pauta C",
      descricao: "Descricao",
      createdAt: new Date().toISOString(),
      sessao: null
    })
    .mockResolvedValueOnce({
      id: 1,
      titulo: "Pauta Atualizada",
      descricao: "Descricao",
      createdAt: new Date().toISOString(),
      sessao: null
    });

  renderWithRoute("/pautas/1");

  expect(await screen.findByText("Pauta C")).toBeInTheDocument();

  const callsBefore = mockGetPauta.mock.calls.length;

  await userEvent.click(screen.getByRole("button", { name: /Atualizar/i }));

  await waitFor(() => {
    expect(mockGetPauta.mock.calls.length).toBe(callsBefore + 1);
  });
});

it("remove a pauta e volta para a lista", async () => {
  mockGetPauta.mockResolvedValueOnce({
    id: 9,
    titulo: "Pauta Remocao",
    descricao: "Descricao",
    createdAt: new Date().toISOString(),
    sessao: null
  });
  const confirmSpy = vi.spyOn(window, "confirm").mockReturnValue(true);

  renderWithRoute("/pautas/9");

  await screen.findByText("Pauta Remocao");
  await userEvent.click(screen.getByRole("button", { name: /Excluir/i }));

  expect(await screen.findByText("Lista")).toBeInTheDocument();

  confirmSpy.mockRestore();
});

import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { vi } from "vitest";
import PautaCard from "../components/PautaCard";
import type { Pauta } from "../types";
import { deletePauta, getResultado, openSessao, votar } from "../api/pautas";

vi.mock("../api/pautas", () => ({
  deletePauta: vi.fn(),
  getResultado: vi.fn(),
  openSessao: vi.fn(),
  votar: vi.fn()
}));

const mockGetResultado = vi.mocked(getResultado);
const mockOpenSessao = vi.mocked(openSessao);
const mockVotar = vi.mocked(votar);
const mockDeletePauta = vi.mocked(deletePauta);

const pautaBase: Pauta = {
  id: 1,
  titulo: "Pauta longa",
  descricao: "Descricao extensa",
  createdAt: new Date().toISOString(),
  sessao: null
};

const pautaAberta: Pauta = {
  ...pautaBase,
  sessao: {
    aberta: true,
    abertaEm: new Date().toISOString(),
    fechaEm: new Date().toISOString()
  }
};

const renderCard = (
  showDetailsLink: boolean,
  pauta: Pauta = pautaBase,
  onNotify: ReturnType<typeof vi.fn> = vi.fn(),
  onRefresh: ReturnType<typeof vi.fn> = vi.fn(),
  onDeleted?: ReturnType<typeof vi.fn>
) =>
  render(
    <MemoryRouter>
      <PautaCard
        pauta={pauta}
        onRefresh={onRefresh}
        onNotify={onNotify}
        showDetailsLink={showDetailsLink}
        onDeleted={onDeleted}
      />
    </MemoryRouter>
  );

beforeEach(() => {
  vi.clearAllMocks();
  mockGetResultado.mockResolvedValue({
    pautaId: 1,
    totalVotos: 0,
    votosSim: 0,
    votosNao: 0,
    status: "ABERTA"
  });
});

it("aplica classe de clamp na lista e full no detalhe", async () => {
  const { unmount } = renderCard(true);
  const descricaoLista = await screen.findByText(/Descricao extensa/i);
  expect(descricaoLista).toHaveClass("text-clamp");

  unmount();
  renderCard(false);
  const descricaoDetalhe = await screen.findByText(/Descricao extensa/i);
  expect(descricaoDetalhe).toHaveClass("text-full");
});

it("exibe resultado carregado automaticamente", async () => {
  mockGetResultado.mockResolvedValueOnce({
    pautaId: 1,
    totalVotos: 5,
    votosSim: 3,
    votosNao: 2,
    status: "ABERTA"
  });

  renderCard(true, pautaAberta);

  expect(await screen.findByText(/Total: 5/i)).toBeInTheDocument();
  expect(screen.getByText(/SIM: 3/i)).toBeInTheDocument();
  expect(screen.getByText(/NÃO: 2/i)).toBeInTheDocument();
});

it("exibe loading enquanto busca resultado", async () => {
  let resolveResultado: (value: {
    pautaId: number;
    totalVotos: number;
    votosSim: number;
    votosNao: number;
    status: string;
  }) => void = () => undefined;
  const pending = new Promise<{
    pautaId: number;
    totalVotos: number;
    votosSim: number;
    votosNao: number;
    status: string;
  }>((resolve) => {
    resolveResultado = resolve;
  });
  mockGetResultado.mockReturnValueOnce(pending);

  renderCard(true, pautaAberta);

  expect(screen.getByText(/Carregando resultado/i)).toBeInTheDocument();

  resolveResultado({
    pautaId: 1,
    totalVotos: 0,
    votosSim: 0,
    votosNao: 0,
    status: "ABERTA"
  });

  await waitFor(() => {
    expect(screen.getByText(/Total:/i)).toBeInTheDocument();
  });
});

it("valida duracao antes de abrir sessao", async () => {
  const onNotify = vi.fn();

  renderCard(true, pautaBase, onNotify);

  await userEvent.clear(screen.getByLabelText(/Duração/i));
  await userEvent.type(screen.getByLabelText(/Duração/i), "0");
  await userEvent.click(screen.getByRole("button", { name: /Abrir sessão/i }));

  await waitFor(() => {
    expect(onNotify).toHaveBeenCalledWith("Informe uma duração válida em minutos.", "error");
  });
  expect(mockOpenSessao).not.toHaveBeenCalled();
});

it("abre sessao com sucesso", async () => {
  const onNotify = vi.fn();
  const onRefresh = vi.fn();
  mockOpenSessao.mockResolvedValueOnce();

  renderCard(true, pautaBase, onNotify, onRefresh);

  await userEvent.click(screen.getByRole("button", { name: /Abrir sessão/i }));

  await waitFor(() => {
    expect(mockOpenSessao).toHaveBeenCalledWith(1, { duracaoMinutos: 1 });
  });
  expect(onNotify).toHaveBeenCalledWith("Sessão aberta com sucesso.", "success");
  expect(onRefresh).toHaveBeenCalled();
});

it("exibe erro quando abrir sessao falha", async () => {
  const onNotify = vi.fn();
  mockOpenSessao.mockRejectedValueOnce(new Error("Falha ao abrir"));

  renderCard(true, pautaBase, onNotify);

  await userEvent.click(screen.getByRole("button", { name: /Abrir sessão/i }));

  await waitFor(() => {
    expect(onNotify).toHaveBeenCalledWith("Falha ao abrir", "error");
  });
});

it("registra voto valido", async () => {
  const onNotify = vi.fn();
  mockVotar.mockResolvedValueOnce();

  renderCard(true, pautaAberta, onNotify);

  await userEvent.type(screen.getByLabelText(/Associado ID/i), "12345678901");
  const simButton = screen.getByRole("button", { name: "SIM" });
  expect(simButton).not.toBeDisabled();
  await userEvent.click(simButton);

  await waitFor(() => {
    expect(mockVotar).toHaveBeenCalledWith(1, { associadoId: "12345678901", voto: "SIM" });
  });
  expect(onNotify).toHaveBeenCalledWith("Voto registrado com sucesso.", "success");
});

it("exibe erro quando voto falha", async () => {
  const onNotify = vi.fn();
  mockVotar.mockRejectedValueOnce(new Error("Falha ao votar"));

  renderCard(true, pautaAberta, onNotify);

  await userEvent.type(screen.getByLabelText(/Associado ID/i), "12345678901");
  await userEvent.click(screen.getByRole("button", { name: "NÃO" }));

  await waitFor(() => {
    expect(onNotify).toHaveBeenCalledWith("Falha ao votar", "error");
  });
});

it("nao permite votar sem associado", async () => {
  const onNotify = vi.fn();

  renderCard(true, pautaAberta, onNotify);

  await userEvent.click(screen.getByRole("button", { name: "SIM" }));

  await waitFor(() => {
    expect(onNotify).toHaveBeenCalledWith("Informe o ID do associado antes de votar.", "error");
  });
  expect(mockVotar).not.toHaveBeenCalled();
});

it("nao permite votar com cpf invalido", async () => {
  const onNotify = vi.fn();

  renderCard(true, pautaAberta, onNotify);

  await userEvent.type(screen.getByLabelText(/Associado ID/i), "1234");
  await userEvent.click(screen.getByRole("button", { name: "SIM" }));

  await waitFor(() => {
    expect(onNotify).toHaveBeenCalledWith("O ID deve ter 11 dígitos (formato CPF).", "error");
  });
  expect(mockVotar).not.toHaveBeenCalled();
});

it("exibe erro quando resultado falha", async () => {
  const onNotify = vi.fn();
  mockGetResultado.mockRejectedValueOnce(new Error("Falha ao carregar"));

  renderCard(true, pautaAberta, onNotify);

  await waitFor(() => {
    expect(onNotify).toHaveBeenCalledWith("Falha ao carregar", "error");
  });
});

it("nao exclui quando cancelado", async () => {
  const onNotify = vi.fn();
  const confirmSpy = vi.spyOn(window, "confirm").mockReturnValue(false);

  renderCard(true, pautaBase, onNotify);

  await userEvent.click(screen.getByRole("button", { name: /Excluir/i }));

  expect(mockDeletePauta).not.toHaveBeenCalled();
  expect(onNotify).not.toHaveBeenCalled();

  confirmSpy.mockRestore();
});

it("remove pauta quando confirmado", async () => {
  const onNotify = vi.fn();
  mockDeletePauta.mockResolvedValueOnce();
  const confirmSpy = vi.spyOn(window, "confirm").mockReturnValue(true);

  renderCard(true, pautaBase, onNotify);

  await userEvent.click(screen.getByRole("button", { name: /Excluir/i }));

  expect(mockDeletePauta).toHaveBeenCalledWith(1);
  expect(onNotify).toHaveBeenCalledWith("Pauta excluída.", "success");

  confirmSpy.mockRestore();
});

it("exibe erro quando exclusao falha", async () => {
  const onNotify = vi.fn();
  mockDeletePauta.mockRejectedValueOnce(new Error("Falha ao excluir"));
  const confirmSpy = vi.spyOn(window, "confirm").mockReturnValue(true);

  renderCard(true, pautaBase, onNotify);

  await userEvent.click(screen.getByRole("button", { name: /Excluir/i }));

  await waitFor(() => {
    expect(onNotify).toHaveBeenCalledWith("Falha ao excluir", "error");
  });

  confirmSpy.mockRestore();
});

it("chama onDeleted quando fornecido", async () => {
  const onNotify = vi.fn();
  const onRefresh = vi.fn();
  const onDeleted = vi.fn();
  mockDeletePauta.mockResolvedValueOnce();
  const confirmSpy = vi.spyOn(window, "confirm").mockReturnValue(true);

  renderCard(true, pautaBase, onNotify, onRefresh, onDeleted);

  await userEvent.click(screen.getByRole("button", { name: /Excluir/i }));

  expect(onDeleted).toHaveBeenCalled();
  expect(onRefresh).not.toHaveBeenCalled();

  confirmSpy.mockRestore();
});

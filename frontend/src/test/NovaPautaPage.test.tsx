import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { vi } from "vitest";
import { createPauta } from "../api/pautas";
import NovaPautaPage from "../pages/NovaPautaPage";

vi.mock("../api/pautas", () => ({
  createPauta: vi.fn()
}));

const mockCreatePauta = vi.mocked(createPauta);

const renderWithRoutes = () =>
  render(
    <MemoryRouter initialEntries={["/pautas/nova"]}>
      <Routes>
        <Route path="/pautas/nova" element={<NovaPautaPage />} />
        <Route path="/pautas/:id" element={<div>Destino</div>} />
      </Routes>
    </MemoryRouter>
  );

it("navega para detalhes apos criar pauta", async () => {
  mockCreatePauta.mockResolvedValueOnce({ id: 10, titulo: "Nova", createdAt: new Date().toISOString() });

  renderWithRoutes();

  await userEvent.type(screen.getByLabelText(/Título/i), "Nova pauta");
  await userEvent.type(screen.getByLabelText(/Descrição/i), "Descricao");
  await userEvent.click(screen.getByRole("button", { name: /Criar/i }));

  expect(await screen.findByText("Destino")).toBeInTheDocument();
});

it("exibe erro quando nao consegue criar", async () => {
  mockCreatePauta.mockRejectedValueOnce(new Error("Falha ao criar"));

  renderWithRoutes();

  await userEvent.type(screen.getByLabelText(/Título/i), "Nova pauta");
  await userEvent.click(screen.getByRole("button", { name: /Criar/i }));

  expect(await screen.findByText("Falha ao criar")).toBeInTheDocument();
});

it("ignora notificacao vazia quando erro sem mensagem", async () => {
  mockCreatePauta.mockRejectedValueOnce(new Error(""));

  renderWithRoutes();

  await userEvent.type(screen.getByLabelText(/Título/i), "Nova pauta");
  await userEvent.click(screen.getByRole("button", { name: /Criar/i }));

  expect(screen.queryByRole("status")).toBeNull();
});

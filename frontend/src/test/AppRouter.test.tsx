import { render, screen } from "@testing-library/react";
import { vi } from "vitest";
import AppRouter from "../routes/AppRouter";
import { listPautas } from "../api/pautas";

vi.mock("../api/pautas", () => ({
  listPautas: vi.fn()
}));

const mockListPautas = vi.mocked(listPautas);

it("renderiza a rota inicial com a lista", async () => {
  mockListPautas.mockResolvedValueOnce([]);

  render(<AppRouter />);

  const sessionTexts = await screen.findAllByText(/Sessões/i);
  expect(sessionTexts.length).toBeGreaterThan(0);
  expect(await screen.findByText(/Sem pautas ainda/i)).toBeInTheDocument();
});

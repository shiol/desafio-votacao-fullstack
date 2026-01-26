import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import NotFoundPage from "../pages/NotFoundPage";

it("renderiza texto de pagina nao encontrada", () => {
  render(
    <MemoryRouter>
      <NotFoundPage />
    </MemoryRouter>
  );

  expect(screen.getByText(/Página não encontrada/i)).toBeInTheDocument();
  expect(screen.getByText(/Volte para a lista principal/i)).toBeInTheDocument();
});

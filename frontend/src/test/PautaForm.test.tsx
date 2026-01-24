import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { vi } from "vitest";
import PautaForm from "../components/PautaForm";

it("envia os valores preenchidos", async () => {
  const handleSubmit = vi.fn();
  render(<PautaForm onSubmit={handleSubmit} />);

  await userEvent.type(screen.getByLabelText(/Título/i), " Nova pauta ");
  await userEvent.type(screen.getByLabelText(/Descrição/i), "Descrição teste");
  await userEvent.click(screen.getByRole("button", { name: /Criar/i }));

  expect(handleSubmit).toHaveBeenCalledWith({
    titulo: "Nova pauta",
    descricao: "Descrição teste"
  });
});

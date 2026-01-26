import { fireEvent, render, screen } from "@testing-library/react";
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

it("nao envia quando titulo esta vazio", async () => {
  const handleSubmit = vi.fn();
  const { container } = render(<PautaForm onSubmit={handleSubmit} />);

  const form = container.querySelector("form");
  if (!form) {
    throw new Error("Formulario nao encontrado");
  }
  fireEvent.submit(form);

  expect(handleSubmit).not.toHaveBeenCalled();
});

it("nao envia quando titulo tem apenas espacos", async () => {
  const handleSubmit = vi.fn();
  const { container } = render(<PautaForm onSubmit={handleSubmit} />);

  await userEvent.type(screen.getByLabelText(/Título/i), "   ");

  const form = container.querySelector("form");
  if (!form) {
    throw new Error("Formulario nao encontrado");
  }
  fireEvent.submit(form);

  expect(handleSubmit).not.toHaveBeenCalled();
});

it("mostra estado de envio", () => {
  render(<PautaForm onSubmit={vi.fn()} isSubmitting />);

  expect(screen.getByRole("button", { name: /Criando/i })).toBeDisabled();
});

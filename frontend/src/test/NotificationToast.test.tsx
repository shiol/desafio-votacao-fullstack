import { render, screen } from "@testing-library/react";
import NotificationToast from "../components/NotificationToast";

it("renderiza mensagem quando existe notificacao", () => {
  render(<NotificationToast notification={{ message: "Tudo certo", type: "success" }} />);

  expect(screen.getByRole("status")).toHaveTextContent("Tudo certo");
});

it("renderiza espaco vazio quando nao ha notificacao", () => {
  const { container } = render(<NotificationToast notification={null} />);

  expect(container.querySelector(".toast--empty")).toBeTruthy();
});

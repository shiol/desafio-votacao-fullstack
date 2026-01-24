import { useState } from "react";
import type { FormEvent } from "react";

export type PautaFormValues = {
  titulo: string;
  descricao: string;
};

type PautaFormProps = {
  onSubmit: (values: PautaFormValues) => Promise<void> | void;
  isSubmitting?: boolean;
};

export default function PautaForm({ onSubmit, isSubmitting = false }: PautaFormProps) {
  const [titulo, setTitulo] = useState("");
  const [descricao, setDescricao] = useState("");

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!titulo.trim()) {
      return;
    }
    await onSubmit({ titulo: titulo.trim(), descricao: descricao.trim() });
    setTitulo("");
    setDescricao("");
  };

  return (
    <form onSubmit={handleSubmit} className="stack">
      <label>
        Título
        <input value={titulo} onChange={(event) => setTitulo(event.target.value)} />
      </label>
      <label>
        Descrição
        <textarea value={descricao} onChange={(event) => setDescricao(event.target.value)} />
      </label>
      <button type="submit" disabled={!titulo.trim() || isSubmitting}>
        {isSubmitting ? "Criando..." : "Criar"}
      </button>
    </form>
  );
}

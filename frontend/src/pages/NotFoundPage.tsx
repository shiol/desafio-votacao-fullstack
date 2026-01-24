import { Link } from "react-router-dom";

export default function NotFoundPage() {
  return (
    <section className="section">
      <div className="panel panel--wide">
        <h2>Página não encontrada</h2>
        <p className="muted">A rota solicitada não existe. Volte para a lista principal.</p>
        <Link className="inline-link" to="/">
          Ir para pautas
        </Link>
      </div>
    </section>
  );
}

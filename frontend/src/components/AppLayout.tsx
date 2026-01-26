import { NavLink, Outlet } from "react-router-dom";

export default function AppLayout() {
  const navLinkClass = ({ isActive }: { isActive: boolean }) => (isActive ? "active" : undefined);

  return (
    <div className="app">
      <header className="hero">
        <div>
          <p className="eyebrow">Votação cooperativa</p>
          <h1>Sala de Controle de Votação</h1>
          <p className="lead">
            Crie pautas, abra sessões e colete votos com um fluxo limpo baseado em API.
          </p>
          <nav className="nav">
            <NavLink to="/" end className={navLinkClass}>
              Sessões
            </NavLink>
            <NavLink to="/pautas/nova" className={navLinkClass}>
              Nova pauta
            </NavLink>
          </nav>
        </div>
        <div className="panel">
          <h2>Roteiros do fluxo</h2>
          <p className="muted">
            Acompanhe a lista principal, crie pautas dedicadas e navegue para detalhes de cada pauta.
          </p>
        </div>
      </header>

      <main>
        <Outlet />
      </main>
    </div>
  );
}

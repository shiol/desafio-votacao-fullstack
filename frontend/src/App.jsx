import React, { useEffect, useMemo, useState } from "react";

const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api/v1";

async function fetchJson(path, options = {}) {
  const response = await fetch(`${API_URL}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {})
    },
    ...options
  });

  if (!response.ok) {
    let message = "Request failed";
    try {
      const data = await response.json();
      message = data.message || message;
    } catch {
      message = response.statusText || message;
    }
    throw new Error(message);
  }

  if (response.status === 204) {
    return null;
  }

  const text = await response.text();
  if (!text) {
    return null;
  }

  return JSON.parse(text);
}

export default function App() {
  const [pautas, setPautas] = useState([]);
  const [loading, setLoading] = useState(false);
  const [notification, setNotification] = useState(null);
  const [titulo, setTitulo] = useState("");
  const [descricao, setDescricao] = useState("");
  const [duracaoMinutos, setDuracaoMinutos] = useState("1");
  const [votos, setVotos] = useState({});
  const [resultados, setResultados] = useState({});

  const hasPautas = pautas.length > 0;

  const pushNotification = (message, type = "info") => {
    if (!message) {
      return;
    }
    setNotification({ message, type });
  };

  const loadPautas = async () => {
    setLoading(true);
    try {
      const data = await fetchJson("/pautas");
      const sorted = [...data].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
      setPautas(sorted);
    } catch (error) {
      pushNotification(error.message, "error");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPautas();
  }, []);

  const handleCreate = async (event) => {
    event.preventDefault();
    try {
      await fetchJson("/pautas", {
        method: "POST",
        body: JSON.stringify({ titulo, descricao })
      });
      setTitulo("");
      setDescricao("");
      await loadPautas();
    } catch (error) {
      pushNotification(error.message, "error");
    }
  };

  const handleOpenSession = async (pautaId) => {
    try {
      await fetchJson(`/pautas/${pautaId}/sessoes`, {
        method: "POST",
        body: JSON.stringify({ duracaoMinutos: Number(duracaoMinutos) })
      });
      await loadPautas();
    } catch (error) {
      pushNotification(error.message, "error");
    }
  };

  const handleVoteChange = (pautaId, field, value) => {
    setVotos((prev) => ({
      ...prev,
      [pautaId]: {
        ...prev[pautaId],
        [field]: value
      }
    }));
  };

  const handleVote = async (pautaId, votoValor) => {
    const payload = {
      ...votos[pautaId],
      voto: votoValor
    };
    if (!payload?.associadoId) {
      pushNotification("Informe o ID do associado antes de votar.", "error");
      return;
    }
    if (!/^\d{11}$/.test(payload.associadoId)) {
      pushNotification("O ID deve ter 11 digitos (formato CPF).", "error");
      return;
    }
    try {
      await fetchJson(`/pautas/${pautaId}/votos`, {
        method: "POST",
        body: JSON.stringify({
          associadoId: payload.associadoId,
          voto: payload.voto
        })
      });
      pushNotification("Voto registrado com sucesso.", "success");
      await loadPautas();
    } catch (error) {
      pushNotification(error.message, "error");
    }
  };

  const handleResultado = async (pautaId) => {
    try {
      const data = await fetchJson(`/pautas/${pautaId}/resultado`);
      setResultados((prev) => ({ ...prev, [pautaId]: data }));
    } catch (error) {
      pushNotification(error.message, "error");
    }
  };

  const handleDelete = async (pautaId, titulo) => {
    const ok = window.confirm(`Delete "${titulo}" e todos os votos?`);
    if (!ok) {
      return;
    }
    try {
      await fetchJson(`/pautas/${pautaId}`, {
        method: "DELETE"
      });
      setResultados((prev) => {
        const copy = { ...prev };
        delete copy[pautaId];
        return copy;
      });
      setVotos((prev) => {
        const copy = { ...prev };
        delete copy[pautaId];
        return copy;
      });
      await loadPautas();
    } catch (error) {
      pushNotification(error.message, "error");
    }
  };

  const pautaCards = useMemo(
    () =>
      pautas.map((pauta) => {
        const sessao = pauta.sessao;
        const resultado = resultados[pauta.id];
        return (
          <article className="card" key={pauta.id}>
            <header className="card__header">
              <div>
                <h3>{pauta.titulo}</h3>
                <p className="muted">{pauta.descricao || "No description"}</p>
              </div>
              <div className="card__actions">
                <span className={`badge ${sessao?.aberta ? "badge--open" : "badge--closed"}`}>
                  {sessao ? (sessao.aberta ? "Aberta" : "Fechada") : "No session"}
                </span>
                <button
                  type="button"
                  className="danger"
                  onClick={() => handleDelete(pauta.id, pauta.titulo)}
                >
                  Delete
                </button>
              </div>
            </header>

            <section className="card__section">
              <div className="row">
                <label>
                  Duração (min)
                  <input
                    type="number"
                    min="1"
                    value={duracaoMinutos}
                    onChange={(event) => setDuracaoMinutos(event.target.value)}
                  />
                </label>
                <button type="button" onClick={() => handleOpenSession(pauta.id)} disabled={!!sessao}>
                  Abrir sessao
                </button>
              </div>
            </section>

            <section className="card__section">
              <div className="row">
                <label>
                  Associado ID
                  <input
                    type="text"
                    inputMode="numeric"
                    maxLength={11}
                    placeholder="11 digitos"
                    value={votos[pauta.id]?.associadoId || ""}
                    onChange={(event) =>
                      handleVoteChange(
                        pauta.id,
                        "associadoId",
                        event.target.value.replace(/\D/g, "").slice(0, 11)
                      )
                    }
                  />
                </label>
                <div className="vote-group">
                  <span className="vote-label">Votar</span>
                  <div className="vote-buttons">
                    <button
                      type="button"
                      className="chip"
                      onClick={() => {
                        handleVote(pauta.id, "SIM");
                      }}
                      disabled={!sessao?.aberta}
                    >
                      SIM
                    </button>
                    <button
                      type="button"
                      className="chip"
                      onClick={() => {
                        handleVote(pauta.id, "NAO");
                      }}
                      disabled={!sessao?.aberta}
                    >
                      NAO
                    </button>
                  </div>
                </div>
              </div>
            </section>

            <section className="card__section">
              <div className="row row--between">
                <button type="button" className="ghost" onClick={() => handleResultado(pauta.id)}>
                  Carregar resultado
                </button>
                {resultado && (
                  <div className="result">
                    <span>Total: {resultado.totalVotos}</span>
                    <span>SIM: {resultado.votosSim}</span>
                    <span>NAO: {resultado.votosNao}</span>
                    <span>Status: {resultado.status}</span>
                  </div>
                )}
              </div>
            </section>
          </article>
        );
      }),
    [pautas, resultados, votos, duracaoMinutos]
  );

  return (
    <div className="app">
      <header className="hero">
        <div>
          <p className="eyebrow">Votação cooperativa</p>
          <h1>Sala de Controle de Votação</h1>
          <p className="lead">
            Crie pautas, abra sessões e colete votos com um fluxo limpo baseado em API.
          </p>
        </div>
        <div className="panel">
          <h2>Criar pauta</h2>
          <form onSubmit={handleCreate} className="stack">
            <label>
              Título
              <input value={titulo} onChange={(event) => setTitulo(event.target.value)} />
            </label>
            <label>
              Descrição
              <textarea value={descricao} onChange={(event) => setDescricao(event.target.value)} />
            </label>
            <button type="submit" disabled={!titulo.trim()}>
              Criar
            </button>
          </form>
        </div>
      </header>

      <main>
        <section className="section">
          <div className="section__header">
            <h2>Sessões</h2>
            <div className="toast-container">
              {notification && (
                <div className={`toast toast--${notification.type}`}>
                  {notification.message}
                </div>
              )}
            </div>
            <button type="button" className="ghost" onClick={loadPautas} disabled={loading}>
              {loading ? "Atualizando..." : "Atualizar"}
            </button>
          </div>
          {hasPautas ? <div className="grid">{pautaCards}</div> : <p className="muted">Sem pautas ainda.</p>}
        </section>
      </main>
    </div>
  );
}

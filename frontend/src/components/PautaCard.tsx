import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { deletePauta, getResultado, openSessao, votar } from "../api/pautas";
import type { NotificationType, Pauta, Resultado, VotoValor } from "../types";

const CPF_REGEX = /^\d{11}$/;

type PautaCardProps = {
  pauta: Pauta;
  onRefresh: () => Promise<void> | void;
  onNotify: (message: string, type?: NotificationType) => void;
  showDetailsLink?: boolean;
  onDeleted?: () => void;
};

export default function PautaCard({
  pauta,
  onRefresh,
  onNotify,
  showDetailsLink = true,
  onDeleted
}: PautaCardProps) {
  const [duracaoMinutos, setDuracaoMinutos] = useState("1");
  const [associadoId, setAssociadoId] = useState("");
  const [resultado, setResultado] = useState<Resultado | null>(null);
  const [loadingResultado, setLoadingResultado] = useState(false);

  const sessao = pauta.sessao;

  const handleOpenSessao = async () => {
    const minutos = Number.parseInt(duracaoMinutos, 10);
    if (!Number.isFinite(minutos) || minutos <= 0) {
      onNotify("Informe uma duração válida em minutos.", "error");
      return;
    }

    try {
      await openSessao(pauta.id, { duracaoMinutos: minutos });
      onNotify("Sessão aberta com sucesso.", "success");
      await onRefresh();
    } catch (error) {
      onNotify(error instanceof Error ? error.message : "Erro ao abrir sessão.", "error");
    }
  };

  const handleVote = async (voto: VotoValor) => {
    if (!associadoId) {
      onNotify("Informe o ID do associado antes de votar.", "error");
      return;
    }
    if (!CPF_REGEX.test(associadoId)) {
      onNotify("O ID deve ter 11 dígitos (formato CPF).", "error");
      return;
    }

    try {
      await votar(pauta.id, { associadoId, voto });
      onNotify("Voto registrado com sucesso.", "success");
      await onRefresh();
      await handleResultado();
    } catch (error) {
      onNotify(error instanceof Error ? error.message : "Erro ao registrar voto.", "error");
    }
  };

  const handleResultado = async () => {
    if (!sessao) {
      setResultado(null);
      setLoadingResultado(false);
      return;
    }
    setLoadingResultado(true);
    try {
      const data = await getResultado(pauta.id);
      setResultado(data);
    } catch (error) {
      onNotify(error instanceof Error ? error.message : "Erro ao carregar resultado.", "error");
    } finally {
      setLoadingResultado(false);
    }
  };

  useEffect(() => {
    setResultado(null);
    if (!sessao) {
      setLoadingResultado(false);
      return;
    }
    handleResultado();
  }, [pauta.id, sessao?.aberta, sessao?.abertaEm, sessao?.fechaEm]);

  const handleDelete = async () => {
    const ok = window.confirm(`Excluir "${pauta.titulo}" e todos os votos?`);
    if (!ok) {
      return;
    }
    try {
      await deletePauta(pauta.id);
      onNotify("Pauta excluída.", "success");
      if (onDeleted) {
        onDeleted();
        return;
      }
      await onRefresh();
    } catch (error) {
      onNotify(error instanceof Error ? error.message : "Erro ao excluir pauta.", "error");
    }
  };

  return (
    <article className="card">
      <header className="card__header">
        <div>
          <h3 className={`card__title ${showDetailsLink ? "text-clamp" : "text-full"}`}>{pauta.titulo}</h3>
          <p className={`muted card__description ${showDetailsLink ? "text-clamp" : "text-full"}`}>
            {pauta.descricao || "Sem descrição"}
          </p>
          {showDetailsLink && (
            <Link className="inline-link" to={`/pautas/${pauta.id}`}>
              Abrir detalhes
            </Link>
          )}
        </div>
        <div className="card__actions">
          <span className={`badge ${sessao?.aberta ? "badge--open" : "badge--closed"}`}>
            {sessao ? (sessao.aberta ? "Aberta" : "Fechada") : "Sem sessão"}
          </span>
          <button type="button" className="danger" onClick={handleDelete}>
            Excluir
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
          <button type="button" onClick={handleOpenSessao} disabled={!!sessao}>
            Abrir sessão
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
              placeholder="11 dígitos"
              value={associadoId}
              onChange={(event) => setAssociadoId(event.target.value.replace(/\D/g, "").slice(0, 11))}
            />
          </label>
          <div className="vote-group">
            <span className="vote-label">Votar</span>
            <div className="vote-buttons">
              <button type="button" className="chip" onClick={() => handleVote("SIM")} disabled={!sessao?.aberta}>
                SIM
              </button>
              <button type="button" className="chip" onClick={() => handleVote("NAO")} disabled={!sessao?.aberta}>
                NÃO
              </button>
            </div>
          </div>
        </div>
      </section>

      <section className="card__section">
        <div className="row row--between">
          {loadingResultado && <span className="muted">Carregando resultado...</span>}
          {resultado ? (
            <div className="result">
              <span>Total: {resultado.totalVotos}</span>
              <span>SIM: {resultado.votosSim}</span>
              <span>NÃO: {resultado.votosNao}</span>
              <span>Status: {resultado.status}</span>
            </div>
          ) : (
            !loadingResultado && <span className="muted">Sem resultado disponível.</span>
          )}
        </div>
      </section>
    </article>
  );
}

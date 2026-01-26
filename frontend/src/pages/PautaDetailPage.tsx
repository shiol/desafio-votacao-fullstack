import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { ApiError } from "../api/client";
import { getPauta } from "../api/pautas";
import NotificationToast from "../components/NotificationToast";
import PautaCard from "../components/PautaCard";
import type { Notification, NotificationType, Pauta } from "../types";

export default function PautaDetailPage() {
  const params = useParams();
  const navigate = useNavigate();
  const pautaId = Number(params.id);
  const [pauta, setPauta] = useState<Pauta | null>(null);
  const [loading, setLoading] = useState(false);
  const [notification, setNotification] = useState<Notification | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const pushNotification = (message: string, type: NotificationType = "info") => {
    if (!message) {
      return;
    }
    setNotification({ message, type });
  };

  const loadPauta = async () => {
    if (!Number.isFinite(pautaId)) {
      pushNotification("Pauta inválida.", "error");
      setErrorMessage("Pauta inválida.");
      return;
    }
    setLoading(true);
    setErrorMessage(null);
    try {
      const data = await getPauta(pautaId);
      setPauta(data);
    } catch (error) {
      let message = error instanceof Error ? error.message : "Erro ao carregar pauta.";
      if (error instanceof ApiError && error.status === 404) {
        message = "Pauta não encontrada.";
      }
      setPauta(null);
      setErrorMessage(message);
      pushNotification(message, "error");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPauta();
  }, [pautaId]);

  return (
    <section className="section">
      <div className="section__header">
        <div className="page-header">
          <Link className="inline-link" to="/">
            Voltar
          </Link>
          <h2>Detalhes da pauta</h2>
        </div>
        <div className="toast-container">
          <NotificationToast notification={notification} />
        </div>
        <button type="button" className="ghost" onClick={loadPauta} disabled={loading}>
          {loading ? "Atualizando..." : "Atualizar"}
        </button>
      </div>
      {loading && !pauta ? (
        <p className="muted">Carregando detalhes da pauta...</p>
      ) : pauta ? (
        <PautaCard
          pauta={pauta}
          onRefresh={loadPauta}
          onNotify={pushNotification}
          showDetailsLink={false}
          onDeleted={() => navigate("/")}
        />
      ) : errorMessage ? (
        <div className="panel panel--wide">
          <p className="muted">{errorMessage}</p>
          <button type="button" className="ghost" onClick={() => navigate("/")}>
            Voltar para pautas
          </button>
        </div>
      ) : (
        <p className="muted">Nenhuma pauta encontrada.</p>
      )}
    </section>
  );
}

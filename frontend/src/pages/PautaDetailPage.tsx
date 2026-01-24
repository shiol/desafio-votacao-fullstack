import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
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

  const pushNotification = (message: string, type: NotificationType = "info") => {
    if (!message) {
      return;
    }
    setNotification({ message, type });
  };

  const loadPauta = async () => {
    if (!Number.isFinite(pautaId)) {
      pushNotification("Pauta inválida.", "error");
      return;
    }
    setLoading(true);
    try {
      const data = await getPauta(pautaId);
      setPauta(data);
    } catch (error) {
      pushNotification(error instanceof Error ? error.message : "Erro ao carregar pauta.", "error");
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
      {pauta ? (
        <PautaCard
          pauta={pauta}
          onRefresh={loadPauta}
          onNotify={pushNotification}
          showDetailsLink={false}
          onDeleted={() => navigate("/")}
        />
      ) : (
        <p className="muted">Nenhuma pauta encontrada.</p>
      )}
    </section>
  );
}

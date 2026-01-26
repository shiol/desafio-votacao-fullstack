import { useEffect, useState } from "react";
import { listPautas } from "../api/pautas";
import NotificationToast from "../components/NotificationToast";
import PautaCard from "../components/PautaCard";
import type { Notification, NotificationType, Pauta } from "../types";

export default function DashboardPage() {
  const [pautas, setPautas] = useState<Pauta[]>([]);
  const [loading, setLoading] = useState(false);
  const [notification, setNotification] = useState<Notification | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const pushNotification = (message: string, type: NotificationType = "info") => {
    if (!message) {
      return;
    }
    setNotification({ message, type });
  };

  const loadPautas = async () => {
    setLoading(true);
    setErrorMessage(null);
    try {
      const data = await listPautas();
      const sorted = [...data].sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
      setPautas(sorted);
    } catch (error) {
      const message = error instanceof Error ? error.message : "Erro ao carregar pautas.";
      setErrorMessage(message);
      pushNotification(message, "error");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPautas();
  }, []);

  return (
    <section className="section">
      <div className="section__header">
        <h2>Sessões</h2>
        <div className="toast-container">
          <NotificationToast notification={notification} />
        </div>
        <button type="button" className="ghost" onClick={loadPautas} disabled={loading}>
          {loading ? "Atualizando..." : "Atualizar"}
        </button>
      </div>
      {loading && pautas.length === 0 ? (
        <p className="muted">Carregando pautas...</p>
      ) : errorMessage && pautas.length === 0 ? (
        <div className="panel panel--wide">
          <p className="muted">{errorMessage}</p>
          <button type="button" className="ghost" onClick={loadPautas}>
            Tentar novamente
          </button>
        </div>
      ) : pautas.length > 0 ? (
        <div className="grid">
          {pautas.map((pauta) => (
            <PautaCard
              key={pauta.id}
              pauta={pauta}
              onRefresh={loadPautas}
              onNotify={pushNotification}
            />
          ))}
        </div>
      ) : (
        <p className="muted">Sem pautas ainda. Crie uma nova para iniciar a votação.</p>
      )}
    </section>
  );
}

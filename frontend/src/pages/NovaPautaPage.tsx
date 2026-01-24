import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { createPauta } from "../api/pautas";
import NotificationToast from "../components/NotificationToast";
import PautaForm from "../components/PautaForm";
import type { Notification, NotificationType } from "../types";

export default function NovaPautaPage() {
  const navigate = useNavigate();
  const [notification, setNotification] = useState<Notification | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const pushNotification = (message: string, type: NotificationType = "info") => {
    if (!message) {
      return;
    }
    setNotification({ message, type });
  };

  const handleSubmit = async (values: { titulo: string; descricao: string }) => {
    setIsSubmitting(true);
    try {
      const pauta = await createPauta({ titulo: values.titulo, descricao: values.descricao });
      pushNotification("Pauta criada com sucesso.", "success");
      navigate(`/pautas/${pauta.id}`);
    } catch (error) {
      pushNotification(error instanceof Error ? error.message : "Erro ao criar pauta.", "error");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <section className="section">
      <div className="section__header">
        <h2>Nova pauta</h2>
        <div className="toast-container">
          <NotificationToast notification={notification} />
        </div>
      </div>
      <div className="panel panel--wide">
        <PautaForm onSubmit={handleSubmit} isSubmitting={isSubmitting} />
      </div>
    </section>
  );
}

import type { Notification } from "../types";

type NotificationToastProps = {
  notification: Notification | null;
};

export default function NotificationToast({ notification }: NotificationToastProps) {
  if (!notification) {
    return <div className="toast toast--empty" aria-live="polite" />;
  }

  return (
    <div className={`toast toast--${notification.type}`} role="status" aria-live="polite">
      {notification.message}
    </div>
  );
}

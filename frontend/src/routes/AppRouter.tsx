import { BrowserRouter, Route, Routes } from "react-router-dom";
import AppLayout from "../components/AppLayout";
import DashboardPage from "../pages/DashboardPage";
import NotFoundPage from "../pages/NotFoundPage";
import NovaPautaPage from "../pages/NovaPautaPage";
import PautaDetailPage from "../pages/PautaDetailPage";

export default function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<AppLayout />}>
          <Route index element={<DashboardPage />} />
          <Route path="pautas/nova" element={<NovaPautaPage />} />
          <Route path="pautas/:id" element={<PautaDetailPage />} />
          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

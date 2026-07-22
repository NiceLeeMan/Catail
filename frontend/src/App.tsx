import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AuthInitializer } from './components/AuthInitializer';
import { ProtectedRoute } from './components/ProtectedRoute';
import { LoginPage } from './pages/LoginPage';
import { OAuthCallbackPage } from './pages/OAuthCallbackPage';
import { HomePage } from './pages/HomePage';
import { CatalystListPage } from './pages/CatalystListPage';
import { CatalystCreatePage } from './pages/CatalystCreatePage';
import { CatalystDetailPage } from './pages/CatalystDetailPage';
import { CompanyListPage } from './pages/CompanyListPage';
import { CompanyDetailPage } from './pages/CompanyDetailPage';

function App() {
  return (
    <BrowserRouter>
      <AuthInitializer>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/auth/callback" element={<OAuthCallbackPage />} />
          <Route element={<ProtectedRoute />}>
            <Route path="/" element={<HomePage />} />
            <Route path="/companies" element={<CompanyListPage />} />
            <Route path="/companies/:companyId" element={<CompanyDetailPage />} />
            <Route path="/catalysts" element={<CatalystListPage />} />
            <Route path="/catalysts/new" element={<CatalystCreatePage />} />
            <Route path="/catalysts/:id" element={<CatalystDetailPage />} />
          </Route>
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </AuthInitializer>
    </BrowserRouter>
  );
}

export default App;

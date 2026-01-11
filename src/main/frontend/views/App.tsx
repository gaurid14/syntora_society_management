// src/main/frontend/views/App.tsx
import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
// import HomeView from './HomeView';
import Login from './login';
import SocietyRegistration from './society_register';
import AdminRegistration from './admin_register';
import ForgotPassword from './forgot_password';
import Logout from './logout';
import WebAdminLogin from './webadmin';

export default function App() {
    return (
        <Router>
            <Routes>
                <Route path="/login" element={<Login />} />
                <Route path="/society_register" element={<SocietyRegistration />} />
                <Route path="/admin_register" element={<AdminRegistration />} />
                <Route path="/forgot_password" element={<ForgotPassword />} />
                <Route path="/logout" element={<Logout />} />
                <Route path="/webadmin" element={<WebAdminLogin />} />
            </Routes>
        </Router>
    );
}

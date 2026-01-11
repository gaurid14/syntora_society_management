import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../themes/my-theme/auth.css';

export function WebAdminLogin() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const navigate = useNavigate();

    // Admin login handler
    const handleLogin = async (e: React.FormEvent) => {
        e.preventDefault();

        try {
            const response = await fetch('/api/auth/admin/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });

            if (response.ok) {
//                 const data = await response.json();
//                 console.log('Login successful:', data);
                localStorage.setItem('webAdminUsername', username);
                window.location.href = '/web_admin';
            }
            else {
                const errorData = await response.json();
                console.log('Login failed:', errorData);
                alert('Login failed. Invalid username or password.');
            }
        } catch (error) {
            console.error('Login error:', error);
            alert('An error occurred during login.');
        }
    };

    return (
        <div className="auth-container">
            <div className="society-registration-container">
                <h2>Login</h2>


                    <form noValidate>
                        <input
                            type="text"
                            placeholder="Username"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            required
                        />
                        <input
                            type="password"
                            placeholder="Password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                        />
                        <button type="button" title="Login" onClick={handleLogin}>
                            Web Admin Login
                        </button>
                    </form>
            </div>
        </div>
    );
}

export default WebAdminLogin;

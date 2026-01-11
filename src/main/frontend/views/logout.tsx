import React from 'react';
import { useNavigate } from 'react-router-dom';

export function Logout() {
    const navigate = useNavigate();

    const handleLogout = async () => {
        console.log('Logout button clicked');
        try {
            const response = await fetch('/api/auth/logout', { method: 'POST' });

            if (response.ok) {
                // Clear session on the client side
                localStorage.removeItem('adminEmail'); // Use 'adminEmail' to match login key

                // Show logout success alert
//                 alert('Logout successfully!');

                // Optional: Redirect to home after the alert
                window.location.href = '/'; // Uncomment if you still want redirection
            } else {
                alert('Logout failed.');
            }
        } catch (error) {
            console.error('Logout error:', error);
            alert('An error occurred during logout.');
        }
    };

    return (
        <div>
            <h2>Logout</h2>
            <button onClick={handleLogout}>Logout</button>
        </div>
    );
}

export default Logout;

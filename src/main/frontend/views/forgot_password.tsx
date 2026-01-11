import React from 'react';
// In your React component or entry file
import '../themes/my-theme/auth.css';


function ForgotPassword() {
    return (
        <div className="auth-container">
            <h2>Forgot Password</h2>
            <form className="auth-form">
                <input type="email" placeholder="Enter your email" required />
                <button type="submit">Reset Password</button>
            </form>
            <p>Remembered? <a href="/login">Login</a></p>
        </div>
    );
}

export default ForgotPassword;

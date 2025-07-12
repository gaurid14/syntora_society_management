// src/main/frontend/index.tsx
import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';

export default function HomeView() {
  return (
    <div>
      <h1>Welcome to your new application</h1>
      <p>This is the home view.</p>
      <p>
        You can edit this view in <code>src\main\frontend\views\@index.tsx</code> or by
        activating Copilot by clicking the icon in the lower right corner
      </p>
    </div>
  );
}

ReactDOM.createRoot(document.getElementById('outlet')!).render(
    <React.StrictMode>
        <App />
    </React.StrictMode>
);


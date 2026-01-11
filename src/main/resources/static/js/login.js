document.getElementById('loginForm').addEventListener('submit', function(event) {
    event.preventDefault();

    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    if (username === 'Akanksha' && password === 'ak123') {
        window.location.href = 'admin.html'; // Redirect to admin profile page
    } else {
        document.getElementById('errorMessage').textContent = 'Invalid username or password';
    }
});

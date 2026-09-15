const button = document.getElementById('submit-btn');
    button.addEventListener('click', function(event) {
        event.preventDefault();
        const form = document.getElementById('cadastro-form');
        const formData = new FormData(form);

        for (const [key, value] of formData.entries()) {
            console.log(`${key}: ${value}`);
        }
    });

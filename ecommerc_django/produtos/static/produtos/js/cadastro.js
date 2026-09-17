const button = document.getElementById('submit-btn');

    button.addEventListener('click', async function(event) {
        event.preventDefault();
        const form = document.getElementById('cadastro-form');
        const formData = new FormData(form);
        let html = '';


        try{
            const response = await fetch(form.action, {
                method: form.method,
                body: formData,
            });

            if (response.ok) {
                const data = await response.json();
                
                if (data.valid) {
                    alert('Produto cadastrado com sucesso!');
                    form.reset();

                } else {
                    html += '<p class="text-red-500 text-sm mt-2 mb-2">Erro: Verifique os campos novamente</p>' 
                    document.getElementById('form-submission-feedback').innerHTML = html
                }

            }

            else {
                throw new Error('Erro ao cadastrar produto: ' + response.status);
                
            }

        } catch (error) {
            alert('Erro ao cadastrar produto: ' + error.message);
        }
});

const button = document.getElementById('submit-btn');

    button.addEventListener('click', async function(event) {
        event.preventDefault();
        const form = document.getElementById('cadastro-form');
        const formData = new FormData(form);


        try{
            const response = await fetch(form.action, {
                method: form.method,
                body: formData,
            });

            if (response.ok) {
                const data = await response.json();
                console.log(data);
                
                if (data.valid) {
                    console.log('Produto cadastrado com sucesso!');
                    alert('Produto cadastrado com sucesso!');
                    form.reset();

                } else {
                    console.log('Erro ao cadastrar produto: '+ data.errors);
                    alert('Erro ao cadastrar produto: ' + data.errors);
                }

            }

            else {
                console.log(form.action);
                throw new Error('Erro ao cadastrar produto: ' + response.status);
                
            }

        } catch (error) {
            alert('Erro ao cadastrar produto: ' + error.message);
        }
});

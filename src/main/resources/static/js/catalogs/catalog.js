document.addEventListener("DOMContentLoaded", () => {
    const catalogForm = document.getElementById("catalogForm");
    const errorDiv = document.getElementById("error");

    if (!catalogForm || !errorDiv) {
        console.error("Form or error div not found");
        return;
    }

    catalogForm.addEventListener("submit", async (e) => {
        e.preventDefault();

        const formData = new FormData(catalogForm);
        formData.set("active", catalogForm.querySelector("#active").checked);

        try {
            const response = await fetch(catalogForm.action, {
                method: "POST",
                body: formData
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || `Не удалось обновить каталог: ${response.status}`);
            }

            alert("Каталог успешно обновлён");
            // Перенаправление, если нужно, например:
            // window.location.href = `/projects/${formData.get('projectSlug')}/catalogs`;
        } catch (error) {
            console.error("Ошибка обновления каталога:", error);
            errorDiv.textContent = error.message;
            errorDiv.style.display = "block";
        }
    });
});
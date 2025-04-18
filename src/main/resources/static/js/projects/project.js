document.addEventListener("DOMContentLoaded", () => {
    // Получаем slug проекта из URL
    const projectSlug = window.location.pathname.split("/").pop();

    // Запрос на сервер для получения данных проекта по slug
    fetch(`/api/v3/${projectSlug}`)
        .then(response => {
            if (!response.ok) {
                throw new Error(`Failed to fetch project: ${response.status}`);
            }
            return response.json();
        })
        .then(project => {
            // Заполнение формы данными проекта
            document.getElementById("name").value = project.name || "";
            document.getElementById("description").value = project.description || "";
            document.getElementById("active").checked = project.active;
        })
        .catch(error => {
            console.error("Error fetching project:", error);
            alert("Failed to load project details");
        });
});

window.updateProject = function() {
    const projectSlug = window.location.pathname.split("/").pop(); // получаем slug из URL

    const updatedProject = {
        name: document.getElementById("name").value,
        description: document.getElementById("description").value || null,
        active: document.getElementById("active").checked
    };

    // Отправка запроса на сервер для обновления проекта по slug
    fetch(`/api/v3/${projectSlug}`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(updatedProject)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Failed to update project: ${response.status}`);
            }
            alert("Project updated successfully");
        })
        .catch(error => {
            console.error("Error updating project:", error);
            alert("Failed to update project");
        });
};

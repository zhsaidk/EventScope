window.createProject = function() {
    const newProject = {
        name: document.getElementById("name").value,
        description: document.getElementById("description").value || null,
        active: document.getElementById("active").checked
    };

    // Отправка данных нового проекта на сервер для его создания
    fetch(`/api/v3/projects`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(newProject)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Failed to create project: ${response.status}`);
            }
            alert("Project created successfully");
            window.location.href = "/projects";  // Перенаправление на страницу проектов
        })
        .catch(error => {
            console.error("Error creating project:", error);
            alert("Failed to create project");
        });
};

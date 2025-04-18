document.addEventListener("DOMContentLoaded", () => {
    // Store projects for accessing projectSlug later
    let projects = [];

    // Получаем список проектов
    fetch("/api/v3/projects")
        .then(response => response.json())
        .then(data => {
            projects = data; // Store projects for createEvent
            const tbody = document.getElementById("projectsBody");
            data.forEach(project => {
                const row = document.createElement("tr");
                row.setAttribute("data-slug", project.slug);
                row.innerHTML = `
                    <td>${project.id}</td>
                    <td>${project.name}</td>
                    <td>${project.description || ""}</td>
                    <td>${project.active}</td>
                    <td>${new Date(project.createdAt).toLocaleString()}</td>
                    <td>
                        <button onclick="viewProject('${project.slug}')" class="view-button">View</button>
                        <button onclick="deleteProject('${project.slug}')" class="delete-button">Delete</button>
                    </td>
                    <td>
                        <button onclick="createCatalog('${project.slug}')" class="create-catalog-button">Create Catalog</button>
                    </td>
                `;
                tbody.appendChild(row);
            });
        })
        .catch(error => console.error("Error fetching projects:", error));

    // Переход на страницу проекта по slug
    window.viewProject = function(slug) {
        window.location.href = `/projects/${slug}`;
    };

    // Удаление проекта по slug
    window.deleteProject = function(slug) {
        if (confirm("Are you sure you want to delete this project?")) {
            fetch(`/api/v3/${slug}`, {
                method: "DELETE"
            })
                .then(response => {
                    if (!response.ok) {
                        throw new Error(`Failed to delete project: ${response.status}`);
                    }
                    const row = document.querySelector(`tr[data-slug="${slug}"]`);
                    if (row) row.remove();
                    alert("Project deleted successfully");
                })
                .catch(error => {
                    console.error("Error deleting project:", error);
                    alert("Failed to delete project");
                });
        }
    };

    // Переход на страницу создания каталога с projectSlug
    window.createCatalog = function(projectSlug) {
        window.location.href = `/projects/catalogs/build?projectSlug=${encodeURIComponent(projectSlug)}`;
    };
});
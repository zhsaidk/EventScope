document.addEventListener("DOMContentLoaded", () => {
    fetch("/api/v3/projects")
        .then(response => response.json())
        .then(data => {
            const tbody = document.getElementById("projectsBody");
            data.forEach(project => {
                const row = document.createElement("tr");
                row.setAttribute("data-id", project.id);
                row.innerHTML = `
                    <td>${project.id}</td>
                    <td>${project.name}</td>
                    <td>${project.description || ""}</td>
                    <td>${project.active}</td>
                    <td>${new Date(project.createdAt).toLocaleString()}</td>
                    <td>
                        <button onclick="viewProject(${project.id})" class="view-button">View</button>
                        <button onclick="deleteProject(${project.id})" class="delete-button">Delete</button>
                    </td>
                `;
                tbody.appendChild(row);
            });
        })
        .catch(error => console.error("Error fetching projects:", error));

    window.viewProject = function(id) {
        window.location.href = `/projects/${id}`;
    };

    window.deleteProject = function(id) {
        if (confirm("Are you sure you want to delete this project?")) {
            fetch(`/api/v3/projects/delete/${id}`, {
                method: "DELETE"
            })
                .then(response => {
                    if (!response.ok) {
                        throw new Error(`Failed to delete project: ${response.status}`);
                    }
                    const row = document.querySelector(`tr[data-id="${id}"]`);
                    if (row) row.remove();
                    alert("Project deleted successfully");
                })
                .catch(error => {
                    console.error("Error deleting project:", error);
                    alert("Failed to delete project");
                });
        }
    };
});
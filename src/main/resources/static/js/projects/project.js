document.addEventListener("DOMContentLoaded", () => {
    // Extract project ID from URL (e.g., /projects/123)
    const projectId = window.location.pathname.split("/").pop();

    // Fetch project details
    fetch(`/api/v3/projects/${projectId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error(`Failed to fetch project: ${response.status}`);
            }
            return response.json();
        })
        .then(project => {
            // Populate form fields
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
    const projectId = window.location.pathname.split("/").pop();
    const updatedProject = {
        name: document.getElementById("name").value,
        description: document.getElementById("description").value || null,
        active: document.getElementById("active").checked
    };

    fetch(`/api/v3/projects/update/${projectId}`, {
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
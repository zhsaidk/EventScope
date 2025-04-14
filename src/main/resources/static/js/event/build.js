document.addEventListener("DOMContentLoaded", () => {
    // Set default value for localCreatedAt to current timestamp
    const localCreatedAtInput = document.getElementById("localCreatedAt");
    const now = new Date();
    localCreatedAtInput.value = now.toISOString().slice(0, 16); // Format for datetime-local
});

window.createProject = function() {
    const name = document.getElementById("name").value;
    const catalogId = parseInt(document.getElementById("catalogId").value);
    const parametersInput = document.getElementById("parameters").value.trim();
    const localCreatedAt = document.getElementById("localCreatedAt").value;

    // Validate required inputs
    if (!name || isNaN(catalogId) || !localCreatedAt) {
        alert("Name, Catalog ID, and Created At are required");
        return;
    }

    // Handle parameters: empty input becomes {}
    let parameters = {};
    if (parametersInput) {
        try {
            parameters = JSON.parse(parametersInput);
        } catch (e) {
            alert("Parameters must be valid JSON");
            return;
        }
    }

    const newProject = {
        name,
        catalogId,
        parameters,
        localCreatedAt: new Date(localCreatedAt).toISOString()
    };

    fetch(`/api/v3/projects/catalogs/events/build`, {
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
            // Clear form
            document.getElementById("name").value = "";
            document.getElementById("catalogId").value = "";
            document.getElementById("parameters").value = "";
            const now = new Date();
            document.getElementById("localCreatedAt").value = now.toISOString().slice(0, 16);
        })
        .catch(error => {
            console.error("Error creating project:", error);
            alert("Failed to create project");
        });
};
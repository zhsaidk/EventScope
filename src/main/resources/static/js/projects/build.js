window.createProject = function() {
    const newProject = {
        name: document.getElementById("name").value,
        description: document.getElementById("description").value || null,
        active: document.getElementById("active").checked
    };

    fetch(`/api/v3/projects/build`, {
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
            document.getElementById("description").value = "";
            document.getElementById("active").checked = false;
        })
        .catch(error => {
            console.error("Error creating project:", error);
            alert("Failed to create project");
        });
};